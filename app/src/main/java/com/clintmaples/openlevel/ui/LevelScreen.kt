package com.clintmaples.openlevel.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clintmaples.openlevel.R
import com.clintmaples.openlevel.level.AngleMath
import com.clintmaples.openlevel.level.LevelTolerance
import com.clintmaples.openlevel.ui.theme.Cyan
import com.clintmaples.openlevel.ui.theme.Foam
import com.clintmaples.openlevel.ui.theme.Ink
import com.clintmaples.openlevel.ui.theme.InkCard
import com.clintmaples.openlevel.ui.theme.LevelGreen
import com.clintmaples.openlevel.ui.theme.Mist

@Composable
fun LevelRoute(viewModel: LevelViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startSensors()
                Lifecycle.Event.ON_PAUSE -> viewModel.stopSensors()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            viewModel.startSensors()
        }
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val haptics = remember { HapticTick(context) }
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message -> snackbarHostState.showSnackbar(message) }
    }
    LaunchedEffect(viewModel) {
        viewModel.hapticTicks.collect { haptics.tick() }
    }

    LevelScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onMode = viewModel::setMode,
        onCalibrate = viewModel::calibrate,
        onReset = viewModel::resetCalibration,
        onToggleHold = viewModel::toggleHold,
        onUsePercent = viewModel::setUsePercent,
        onHaptics = viewModel::setHapticsEnabled,
    )
}

@Composable
fun LevelScreen(
    state: LevelUiState,
    snackbarHostState: SnackbarHostState,
    onMode: (LevelMode) -> Unit,
    onCalibrate: () -> Unit,
    onReset: () -> Unit,
    onToggleHold: () -> Unit,
    onUsePercent: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
) {
    var confirmReset by remember { mutableStateOf(false) }
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Scaffold(
        containerColor = Ink,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Ink)
                .padding(padding),
        ) {
            Header(state = state, onMode = onMode)
            when {
                !state.sensorAvailable -> SensorUnavailable(Modifier.weight(1f))
                state.mode == LevelMode.SURFACE -> SurfaceLevel(state, landscape, Modifier.weight(1f))
                else -> EdgeLevel(state, landscape, Modifier.weight(1f))
            }
            Controls(
                state = state,
                onCalibrate = onCalibrate,
                onRequestReset = { confirmReset = true },
                onToggleHold = onToggleHold,
                onUsePercent = onUsePercent,
                onHaptics = onHaptics,
            )
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.reset_title)) },
            text = { Text(stringResource(R.string.reset_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmReset = false
                        onReset()
                    },
                ) { Text(stringResource(R.string.reset_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun Header(state: LevelUiState, onMode: (LevelMode) -> Unit) {
    val sensor = if (state.usingGravitySensor) {
        stringResource(R.string.using_gravity)
    } else {
        stringResource(R.string.using_accelerometer)
    }
    val tolerance = stringResource(R.string.tolerance_caption, LevelTolerance.DEGREES)
    val zeroedLabel = stringResource(R.string.zeroed)
    val heldLabel = stringResource(R.string.held_banner)
    val unavailable = stringResource(R.string.sensor_unavailable_title)
    val zeroed = when (state.mode) {
        LevelMode.SURFACE -> state.surfaceCalibrated
        LevelMode.EDGE -> state.edgeCalibrated
    }
    Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Foam,
            )
            ModeToggle(state.mode, onMode)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (!state.sensorAvailable) {
                unavailable
            } else {
                buildString {
                    append(sensor)
                    append("  ·  ")
                    append(tolerance)
                    if (zeroed) {
                        append("  ·  ")
                        append(zeroedLabel)
                    }
                    if (state.held) {
                        append("  ·  ")
                        append(heldLabel)
                    }
                }
            },
            color = Mist,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ModeToggle(mode: LevelMode, onMode: (LevelMode) -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(InkCard)
            .padding(3.dp),
    ) {
        ModeChip(
            label = stringResource(R.string.mode_surface),
            selected = mode == LevelMode.SURFACE,
            onClick = { onMode(LevelMode.SURFACE) },
        )
        ModeChip(
            label = stringResource(R.string.mode_edge),
            selected = mode == LevelMode.EDGE,
            onClick = { onMode(LevelMode.EDGE) },
        )
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primary else InkCard
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else Mist
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(background),
    ) {
        Text(label, color = content, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SurfaceLevel(state: LevelUiState, landscape: Boolean, modifier: Modifier) {
    val xText = reading(state.hasSample, state.xDegrees, state.usePercent)
    val yText = reading(state.hasSample, state.yDegrees, state.usePercent)
    val xHint = directionHint(state.hasSample, state.xDegrees, state.xLevel, R.string.right_high, R.string.left_high)
    val yHint = directionHint(state.hasSample, state.yDegrees, state.yLevel, R.string.top_high, R.string.bottom_high)
    val levelWord = stringResource(if (state.surfaceLevel) R.string.level_word else R.string.not_level)
    if (landscape) {
        Row(
            modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BoxWithConstraints(Modifier.weight(1.15f)) {
                val side = minOf(maxWidth - 64.dp, maxHeight).coerceAtLeast(120.dp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                    BullseyeLevel(
                        xDegrees = state.xDegrees,
                        yDegrees = state.yDegrees,
                        level = state.surfaceLevel && state.hasSample,
                        contentDescription = stringResource(R.string.bullseye_description, xText, yText, levelWord),
                        modifier = Modifier.size(side),
                    )
                    TubeVial(
                        angleDegrees = state.yDegrees,
                        level = state.yLevel && state.hasSample,
                        vertical = true,
                        contentDescription = stringResource(R.string.vial_vertical_description, yText),
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .width(44.dp)
                            .height(side * 0.78f),
                    )
                }
            }
            Column(
                Modifier
                    .weight(0.85f)
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AngleReadout(stringResource(R.string.axis_x), xText, xHint, state.xLevel && state.hasSample, Modifier.fillMaxWidth())
                AngleReadout(stringResource(R.string.axis_y), yText, yHint, state.yLevel && state.hasSample, Modifier.fillMaxWidth())
                TubeVial(
                    angleDegrees = state.xDegrees,
                    level = state.xLevel && state.hasSample,
                    vertical = false,
                    contentDescription = stringResource(R.string.vial_horizontal_description, xText),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                )
            }
        }
    } else {
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AngleReadout(stringResource(R.string.axis_x), xText, xHint, state.xLevel && state.hasSample, Modifier.weight(1f))
                AngleReadout(stringResource(R.string.axis_y), yText, yHint, state.yLevel && state.hasSample, Modifier.weight(1f))
            }
            BoxWithConstraints(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                val side = minOf(maxWidth - 80.dp, maxHeight - 8.dp).coerceAtLeast(96.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BullseyeLevel(
                        xDegrees = state.xDegrees,
                        yDegrees = state.yDegrees,
                        level = state.surfaceLevel && state.hasSample,
                        contentDescription = stringResource(R.string.bullseye_description, xText, yText, levelWord),
                        modifier = Modifier.size(side),
                    )
                    TubeVial(
                        angleDegrees = state.yDegrees,
                        level = state.yLevel && state.hasSample,
                        vertical = true,
                        contentDescription = stringResource(R.string.vial_vertical_description, yText),
                        modifier = Modifier
                            .padding(start = 14.dp)
                            .width(48.dp)
                            .height(side * 0.82f),
                    )
                }
            }
            TubeVial(
                angleDegrees = state.xDegrees,
                level = state.xLevel && state.hasSample,
                vertical = false,
                contentDescription = stringResource(R.string.vial_horizontal_description, xText),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp)
                    .height(56.dp),
            )
        }
    }
}

@Composable
private fun EdgeLevel(state: LevelUiState, landscape: Boolean, modifier: Modifier) {
    val ready = state.hasSample && state.edgePoseValid
    val number = if (ready) {
        AngleMath.formatMeasurement(state.edgeDeviationDegrees, state.usePercent)
    } else {
        AngleMath.UNAVAILABLE
    }
    val reference = stringResource(
        if (state.edgeReferenceVertical) R.string.from_vertical else R.string.from_horizontal,
    )
    val hint = if (ready) {
        stringResource(R.string.edge_ready_hint)
    } else {
        stringResource(R.string.edge_need_pose)
    }
    val description = stringResource(R.string.edge_description, number, reference)
    val numberSize = when {
        landscape -> 64.sp
        state.usePercent -> 60.sp
        else -> 84.sp
    }
    if (landscape) {
        Row(modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                EdgeReadout(reference, number, numberSize, ready && state.edgeLevel, hint)
            }
            EdgeDial(
                deviationDegrees = if (ready) state.edgeDeviationDegrees else 0f,
                level = ready && state.edgeLevel,
                referenceVertical = state.edgeReferenceVertical,
                contentDescription = description,
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
            )
        }
    } else {
        Column(
            modifier.padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EdgeReadout(reference, number, numberSize, ready && state.edgeLevel, hint)
            EdgeDial(
                deviationDegrees = if (ready) state.edgeDeviationDegrees else 0f,
                level = ready && state.edgeLevel,
                referenceVertical = state.edgeReferenceVertical,
                contentDescription = description,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun EdgeReadout(
    reference: String,
    number: String,
    numberSize: androidx.compose.ui.unit.TextUnit,
    level: Boolean,
    hint: String,
) {
    Text(
        text = reference.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Mist,
    )
    Text(
        text = number,
        color = if (level) LevelGreen else Foam,
        fontSize = numberSize,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        maxLines = 1,
        style = TextStyle(fontFeatureSettings = "tnum"),
    )
    Text(
        text = hint,
        color = Mist,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp, start = 12.dp, end = 12.dp),
    )
}

@Composable
private fun AngleReadout(
    label: String,
    value: String,
    hint: String,
    level: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(InkCard)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Mist)
        Text(
            text = value,
            color = if (level) LevelGreen else Foam,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
        Text(hint, color = if (level) LevelGreen else Mist, fontSize = 12.sp)
    }
}

@Composable
private fun SensorUnavailable(modifier: Modifier) {
    Column(
        modifier.padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.sensor_unavailable_title),
            color = Foam,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.sensor_unavailable_body),
            color = Mist,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Controls(
    state: LevelUiState,
    onCalibrate: () -> Unit,
    onRequestReset: () -> Unit,
    onToggleHold: () -> Unit,
    onUsePercent: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
) {
    val canCalibrate = state.sensorAvailable && state.hasSample &&
        (state.mode == LevelMode.SURFACE || state.edgePoseValid)
    val canReset = state.surfaceCalibrated || state.edgeCalibrated
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onCalibrate, enabled = canCalibrate) {
                Text(stringResource(R.string.calibrate))
            }
            OutlinedButton(onClick = onToggleHold, enabled = state.hasSample) {
                Text(stringResource(if (state.held) R.string.holding else R.string.hold))
            }
            OutlinedButton(onClick = { onUsePercent(!state.usePercent) }) {
                Text(stringResource(if (state.usePercent) R.string.show_percent else R.string.show_degrees))
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = onRequestReset, enabled = canReset) {
                Text(stringResource(R.string.reset))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.haptics), color = Mist, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Switch(checked = state.hapticsEnabled, onCheckedChange = onHaptics)
            }
        }
    }
}

@Composable
private fun reading(hasSample: Boolean, degrees: Float, usePercent: Boolean): String {
    if (!hasSample) return AngleMath.UNAVAILABLE
    return AngleMath.formatMeasurement(degrees, usePercent)
}

@Composable
private fun directionHint(
    hasSample: Boolean,
    degrees: Float,
    level: Boolean,
    positiveRes: Int,
    negativeRes: Int,
): String {
    if (!hasSample) return AngleMath.UNAVAILABLE
    if (level || AngleMath.roundedTenths(degrees) == 0f) return stringResource(R.string.level_word)
    return if (AngleMath.roundedTenths(degrees) > 0f) {
        stringResource(positiveRes)
    } else {
        stringResource(negativeRes)
    }
}
