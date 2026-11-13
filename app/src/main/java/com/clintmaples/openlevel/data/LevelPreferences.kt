package com.clintmaples.openlevel.data

import android.content.Context
import com.clintmaples.openlevel.level.CalibrationOffsets

class LevelPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun loadCalibration(): CalibrationOffsets {
        return CalibrationOffsets(
            xDegrees = prefs.getFloat(KEY_X, 0f),
            yDegrees = prefs.getFloat(KEY_Y, 0f),
            edgeDegrees = prefs.getFloat(KEY_EDGE, 0f),
        )
    }

    fun saveCalibration(offsets: CalibrationOffsets) {
        prefs.edit()
            .putFloat(KEY_X, offsets.xDegrees)
            .putFloat(KEY_Y, offsets.yDegrees)
            .putFloat(KEY_EDGE, offsets.edgeDegrees)
            .apply()
    }

    fun loadHapticsEnabled(): Boolean = prefs.getBoolean(KEY_HAPTICS, true)

    fun saveHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()
    }

    fun loadUsePercent(): Boolean = prefs.getBoolean(KEY_PERCENT, false)

    fun saveUsePercent(usePercent: Boolean) {
        prefs.edit().putBoolean(KEY_PERCENT, usePercent).apply()
    }

    companion object {
        private const val PREFS = "open_level"
        private const val KEY_X = "cal_x"
        private const val KEY_Y = "cal_y"
        private const val KEY_EDGE = "cal_edge"
        private const val KEY_HAPTICS = "haptics"
        private const val KEY_PERCENT = "use_percent"
    }
}
