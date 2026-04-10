package org.polyfrost.bettervisuals.utils

object MathUtil {

    fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t.coerceIn(0f, 1f)
    }

    /**
     * Frame-rate independent exponential decay.
     * speed: 0.01 = very slow, 0.5 = medium, 0.99 = nearly instant
     */
    fun smoothDamp(current: Float, target: Float, speed: Float, partialTicks: Float): Float {
        if (Math.abs(current - target) < 0.001f) return target
        val s = speed.toDouble().coerceIn(0.01, 0.99)
        val factor = 1.0 - Math.pow(1.0 - s, partialTicks.toDouble())
        return current + (target - current) * factor.toFloat()
    }
}
