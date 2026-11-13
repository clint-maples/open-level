package com.clintmaples.openlevel.ui

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import com.clintmaples.openlevel.R
import com.clintmaples.openlevel.data.LevelPreferences
import com.clintmaples.openlevel.level.CalibrationOffsets
import com.clintmaples.openlevel.level.LevelMath
import com.clintmaples.openlevel.level.LevelSnapshot
import com.clintmaples.openlevel.level.VectorLowPass
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicBoolean

enum class LevelMode { SURFACE, EDGE }

data class LevelUiState(
    val sensorAvailable: Boolean = true,
    val usingGravitySensor: Boolean = false,
    val hasSample: Boolean = false,
    val edgePoseValid: Boolean = false,
    val xDegrees: Float = 0f,
    val yDegrees: Float = 0f,
    val edgeDeviationDegrees: Float = 0f,
    val edgeReferenceVertical: Boolean = true,
    val xLevel: Boolean = false,
    val yLevel: Boolean = false,
    val surfaceLevel: Boolean = false,
    val edgeLevel: Boolean = false,
    val held: Boolean = false,
    val usePercent: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val surfaceCalibrated: Boolean = false,
    val edgeCalibrated: Boolean = false,
    val mode: LevelMode = LevelMode.SURFACE,
)

class LevelViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    private val preferences = LevelPreferences(application)
    private val sensorManager = application.getSystemService(SensorManager::class.java)
    private val gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val sensor = gravitySensor ?: accelerometer
    private val usingGravity = gravitySensor != null && sensor != null
    private val filter = VectorLowPass()
    private val listening = AtomicBoolean(false)
    private val lock = Any()

    private var session = Session()
    private var lastFiltered: FloatArray? = null
    private var wasLevel = false

    private val ui = MutableStateFlow(LevelUiState(sensorAvailable = sensor != null))
    val uiState: StateFlow<LevelUiState> = ui

    private val messageFlow = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = messageFlow.asSharedFlow()

    private val hapticFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val hapticTicks: SharedFlow<Unit> = hapticFlow.asSharedFlow()

    init {
        val calibration = preferences.loadCalibration()
        val hapticsEnabled = preferences.loadHapticsEnabled()
        val usePercent = preferences.loadUsePercent()
        synchronized(lock) {
            session = Session(
                calibration = calibration,
                hapticsEnabled = hapticsEnabled,
                usePercent = usePercent,
            )
            publishLocked()
        }
    }

    fun startSensors() {
        val manager = sensorManager ?: return
        val active = sensor ?: return
        if (listening.compareAndSet(false, true)) {
            synchronized(lock) { filter.reset() }
            manager.registerListener(this, active, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopSensors() {
        if (listening.compareAndSet(true, false)) {
            sensorManager?.unregisterListener(this)
        }
    }

    fun setMode(mode: LevelMode) {
        synchronized(lock) {
            if (session.mode == mode) return@synchronized
            session = session.copy(mode = mode)
            wasLevel = false
            publishLocked()
        }
    }

    fun toggleHold() {
        synchronized(lock) {
            session = if (session.held) {
                session.copy(held = false, frozen = null)
            } else {
                session.copy(held = true, frozen = currentLiveLocked())
            }
            publishLocked()
        }
    }

    fun setUsePercent(usePercent: Boolean) {
        synchronized(lock) {
            session = session.copy(usePercent = usePercent)
            preferences.saveUsePercent(usePercent)
            publishLocked()
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        synchronized(lock) {
            session = session.copy(hapticsEnabled = enabled)
            preferences.saveHapticsEnabled(enabled)
            publishLocked()
        }
    }

    fun calibrate() {
        synchronized(lock) {
            val live = currentLiveLocked() ?: return@synchronized
            if (session.mode == LevelMode.EDGE && !live.edgePoseValid) return@synchronized
            val updated = when (session.mode) {
                LevelMode.SURFACE -> session.calibration.withSurface(live.rawX, live.rawY)
                LevelMode.EDGE -> session.calibration.withEdge(live.rawEdgeDeviation)
            }
            session = session.copy(calibration = updated, held = false, frozen = null)
            preferences.saveCalibration(updated)
            wasLevel = false
            publishLocked()
            val message = when (session.mode) {
                LevelMode.SURFACE -> getApplication<Application>().getString(R.string.calibrated_surface)
                LevelMode.EDGE -> getApplication<Application>().getString(R.string.calibrated_edge)
            }
            messageFlow.tryEmit(message)
        }
    }

    fun resetCalibration() {
        synchronized(lock) {
            session = session.copy(calibration = CalibrationOffsets(), held = false, frozen = null)
            preferences.saveCalibration(session.calibration)
            wasLevel = false
            publishLocked()
            messageFlow.tryEmit(getApplication<Application>().getString(R.string.calibration_reset))
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.values.size < 3) return
        synchronized(lock) {
            lastFiltered = filter.filter(
                event.values[0],
                event.values[1],
                event.values[2],
                event.timestamp,
            )
            considerHapticLocked(currentLiveLocked())
            publishLocked()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onCleared() {
        stopSensors()
    }

    private fun currentLiveLocked(): LevelSnapshot? {
        val sample = lastFiltered ?: return null
        return LevelMath.snapshot(sample[0], sample[1], sample[2], session.calibration)
    }

    private fun considerHapticLocked(live: LevelSnapshot?) {
        if (session.held || live == null) return
        val nowLevel = when (session.mode) {
            LevelMode.SURFACE -> live.surfaceLevel
            LevelMode.EDGE -> live.edgePoseValid && live.edgeLevel
        }
        if (nowLevel && !wasLevel && session.hapticsEnabled) {
            hapticFlow.tryEmit(Unit)
        }
        wasLevel = nowLevel
    }

    private fun publishLocked() {
        val live = currentLiveLocked()
        val shown = if (session.held && session.frozen != null) session.frozen else live
        ui.value = LevelUiState(
            sensorAvailable = sensor != null,
            usingGravitySensor = usingGravity,
            hasSample = shown != null,
            edgePoseValid = shown?.edgePoseValid == true,
            xDegrees = shown?.xDegrees ?: 0f,
            yDegrees = shown?.yDegrees ?: 0f,
            edgeDeviationDegrees = shown?.edgeDeviationDegrees ?: 0f,
            edgeReferenceVertical = shown?.edgeReferenceVertical != false,
            xLevel = shown?.xLevel == true,
            yLevel = shown?.yLevel == true,
            surfaceLevel = shown?.surfaceLevel == true,
            edgeLevel = shown?.edgeLevel == true,
            held = session.held,
            usePercent = session.usePercent,
            hapticsEnabled = session.hapticsEnabled,
            surfaceCalibrated = session.calibration.surfaceCalibrated,
            edgeCalibrated = session.calibration.edgeCalibrated,
            mode = session.mode,
        )
    }

    private data class Session(
        val calibration: CalibrationOffsets = CalibrationOffsets(),
        val held: Boolean = false,
        val mode: LevelMode = LevelMode.SURFACE,
        val hapticsEnabled: Boolean = true,
        val usePercent: Boolean = false,
        val frozen: LevelSnapshot? = null,
    )
}
