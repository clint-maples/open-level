package com.clintmaples.openlevel.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.clintmaples.openlevel.ui.theme.Amber
import com.clintmaples.openlevel.ui.theme.Bezel
import com.clintmaples.openlevel.ui.theme.Cyan
import com.clintmaples.openlevel.ui.theme.LevelGreen
import com.clintmaples.openlevel.ui.theme.Line
import com.clintmaples.openlevel.ui.theme.Well
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

internal const val VIAL_FULL_SCALE_DEGREES = 10f

@Composable
internal fun BullseyeLevel(
    xDegrees: Float,
    yDegrees: Float,
    level: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
    toleranceDegrees: Float = 0.5f,
    fullScaleDegrees: Float = VIAL_FULL_SCALE_DEGREES,
) {
    Canvas(
        modifier
            .aspectRatio(1f)
            .semantics { this.contentDescription = contentDescription },
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) / 2f
        val bubbleTravel = radius * 0.70f
        val bubbleRadius = radius * 0.16f

        drawCircle(color = Bezel, radius = radius, center = center)
        drawCircle(color = Well, radius = radius * 0.90f, center = center)
        drawCircle(
            color = Line,
            radius = radius * 0.62f,
            center = center,
            style = Stroke(width = 1.5f),
        )
        drawCircle(
            color = Line,
            radius = radius * 0.34f,
            center = center,
            style = Stroke(width = 1.5f),
        )
        val hair = radius * 0.78f
        drawLine(Line, Offset(center.x - hair, center.y), Offset(center.x + hair, center.y), strokeWidth = 1.5f)
        drawLine(Line, Offset(center.x, center.y - hair), Offset(center.x, center.y + hair), strokeWidth = 1.5f)

        val tolerance = (toleranceDegrees / fullScaleDegrees).coerceIn(0f, 1f) * bubbleTravel
        val tick = radius * 0.045f
        drawLine(Cyan, Offset(center.x - tolerance, center.y - tick), Offset(center.x - tolerance, center.y + tick), 2f)
        drawLine(Cyan, Offset(center.x + tolerance, center.y - tick), Offset(center.x + tolerance, center.y + tick), 2f)
        drawLine(Cyan, Offset(center.x - tick, center.y - tolerance), Offset(center.x + tick, center.y - tolerance), 2f)
        drawLine(Cyan, Offset(center.x - tick, center.y + tolerance), Offset(center.x + tick, center.y + tolerance), 2f)
        drawCircle(Cyan, radius = 3.5f, center = center)

        var nx = (xDegrees / fullScaleDegrees).coerceIn(-1f, 1f)
        var ny = (-yDegrees / fullScaleDegrees).coerceIn(-1f, 1f)
        val magnitude = hypot(nx, ny)
        if (magnitude > 1f) {
            nx /= magnitude
            ny /= magnitude
        }
        val bubble = Offset(center.x + nx * bubbleTravel, center.y + ny * bubbleTravel)
        val bubbleColor = if (level) LevelGreen else Amber
        drawCircle(Color.Black.copy(alpha = 0.35f), bubbleRadius, bubble + Offset(0f, bubbleRadius * 0.18f))
        drawCircle(bubbleColor, bubbleRadius, bubble)
        drawCircle(
            Color.White.copy(alpha = 0.55f),
            bubbleRadius * 0.28f,
            bubble + Offset(-bubbleRadius * 0.28f, -bubbleRadius * 0.32f),
        )
        if (level) {
            drawCircle(LevelGreen, radius = radius * 0.96f, center = center, style = Stroke(width = 5f))
        }
    }
}

@Composable
internal fun TubeVial(
    angleDegrees: Float,
    level: Boolean,
    vertical: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
    toleranceDegrees: Float = 0.5f,
    fullScaleDegrees: Float = VIAL_FULL_SCALE_DEGREES,
) {
    Canvas(modifier.semantics { this.contentDescription = contentDescription }) {
        val stroke = 2f
        val corner = min(size.width, size.height) / 2f
        drawRoundRect(
            color = Bezel,
            size = size,
            cornerRadius = CornerRadius(corner, corner),
        )
        val inset = stroke + 2f
        val inner = Size(size.width - inset * 2f, size.height - inset * 2f)
        drawRoundRect(
            color = Well,
            topLeft = Offset(inset, inset),
            size = inner,
            cornerRadius = CornerRadius(corner, corner),
        )

        val longSpan = if (vertical) inner.height else inner.width
        val shortSpan = if (vertical) inner.width else inner.height
        val bubbleLong = (longSpan * 0.22f).coerceAtLeast(shortSpan * 0.72f).coerceAtMost(longSpan * 0.46f)
        val bubbleShort = shortSpan * 0.62f
        val travel = (longSpan - bubbleLong) / 2f
        val fraction = (angleDegrees / fullScaleDegrees).coerceIn(-1f, 1f)
        val directed = if (vertical) -fraction else fraction
        val along = (if (vertical) inner.height else inner.width) / 2f + directed * travel
        val bubbleLeft: Float
        val bubbleTop: Float
        val bubbleWidth: Float
        val bubbleHeight: Float
        if (vertical) {
            bubbleWidth = bubbleShort
            bubbleHeight = bubbleLong
            bubbleLeft = inset + (inner.width - bubbleWidth) / 2f
            bubbleTop = inset + along - bubbleHeight / 2f
        } else {
            bubbleWidth = bubbleLong
            bubbleHeight = bubbleShort
            bubbleTop = inset + (inner.height - bubbleHeight) / 2f
            bubbleLeft = inset + along - bubbleWidth / 2f
        }
        val bubbleColor = if (level) LevelGreen else Amber
        val bubbleRadius = min(bubbleWidth, bubbleHeight) / 2f
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.28f),
            topLeft = Offset(bubbleLeft, bubbleTop + 2f),
            size = Size(bubbleWidth, bubbleHeight),
            cornerRadius = CornerRadius(bubbleRadius, bubbleRadius),
        )
        drawRoundRect(
            color = bubbleColor,
            topLeft = Offset(bubbleLeft, bubbleTop),
            size = Size(bubbleWidth, bubbleHeight),
            cornerRadius = CornerRadius(bubbleRadius, bubbleRadius),
        )

        val markReach = shortSpan * 0.34f
        val tolerancePx = (abs(toleranceDegrees) / fullScaleDegrees).coerceIn(0f, 1f) * travel
        val midX = size.width / 2f
        val midY = size.height / 2f
        if (vertical) {
            listOf(-tolerancePx, tolerancePx).forEach { offset ->
                drawLine(
                    Cyan,
                    Offset(midX - markReach, midY + offset),
                    Offset(midX + markReach, midY + offset),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round,
                )
            }
        } else {
            listOf(-tolerancePx, tolerancePx).forEach { offset ->
                drawLine(
                    Cyan,
                    Offset(midX + offset, midY - markReach),
                    Offset(midX + offset, midY + markReach),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round,
                )
            }
        }
        if (level) {
            drawRoundRect(
                color = LevelGreen,
                size = size,
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(width = 3f),
            )
        }
    }
}

@Composable
internal fun EdgeDial(
    deviationDegrees: Float,
    level: Boolean,
    referenceVertical: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .semantics { this.contentDescription = contentDescription },
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) / 2f
        drawCircle(Bezel, radius, center)
        drawCircle(Well, radius * 0.90f, center)
        val hair = radius * 0.72f
        drawLine(Line, Offset(center.x - hair, center.y), Offset(center.x + hair, center.y), 1.5f)
        drawLine(Line, Offset(center.x, center.y - hair), Offset(center.x, center.y + hair), 1.5f)
        val base = if (referenceVertical) -90f else 0f
        val shown = deviationDegrees.coerceIn(-45f, 45f)
        rotate(base + shown, center) {
            drawLine(
                color = if (level) LevelGreen else Amber,
                start = center,
                end = Offset(center.x + radius * 0.62f, center.y),
                strokeWidth = 8f,
                cap = StrokeCap.Round,
            )
        }
        drawCircle(if (level) LevelGreen else Amber, radius * 0.06f, center)
        drawCircle(Color(0xFF090C11), radius * 0.028f, center)
        if (level) {
            drawCircle(LevelGreen, radius * 0.96f, center, style = Stroke(5f))
        }
    }
}
