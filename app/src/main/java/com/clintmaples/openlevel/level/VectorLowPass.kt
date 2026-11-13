package com.clintmaples.openlevel.level

/**
 * Time-based low-pass on a 3-axis sensor vector.
 *
 * The first sample is passed through unchanged so the level shows a real
 * reading immediately. Later samples ease toward the input with
 * alpha = dt / (tau + dt). A repeated or backwards timestamp uses a 20 ms
 * step, about one game-rate sensor period, instead of freezing the filter.
 */
class VectorLowPass(
    private val timeConstantSeconds: Float = DEFAULT_TIME_CONSTANT_SECONDS,
) {
    private var x = 0f
    private var y = 0f
    private var z = 0f
    private var initialized = false
    private var lastTimestampNanos = 0L

    fun filter(x: Float, y: Float, z: Float, timestampNanos: Long): FloatArray {
        if (!initialized) {
            this.x = x
            this.y = y
            this.z = z
            initialized = true
            lastTimestampNanos = timestampNanos
            return floatArrayOf(x, y, z)
        }
        val dt = if (timestampNanos > lastTimestampNanos) {
            (timestampNanos - lastTimestampNanos) / 1_000_000_000f
        } else {
            FALLBACK_DT_SECONDS
        }
        lastTimestampNanos = timestampNanos
        val alpha = lowPassAlpha(dt, timeConstantSeconds)
        this.x += alpha * (x - this.x)
        this.y += alpha * (y - this.y)
        this.z += alpha * (z - this.z)
        return floatArrayOf(this.x, this.y, this.z)
    }

    fun reset() {
        initialized = false
        lastTimestampNanos = 0L
    }

    companion object {
        const val DEFAULT_TIME_CONSTANT_SECONDS = 0.10f
        const val FALLBACK_DT_SECONDS = 0.02f
    }
}

fun lowPassAlpha(dtSeconds: Float, timeConstantSeconds: Float): Float {
    if (dtSeconds <= 0f) return 0f
    if (timeConstantSeconds <= 0f) return 1f
    return (dtSeconds / (timeConstantSeconds + dtSeconds)).coerceIn(0f, 1f)
}
