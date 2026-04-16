package org.polyfrost.bettervisuals.utils

import java.lang.reflect.Method

/**
 * Reflection bridge to GuiScaleMod's bypass flag.
 *
 * GuiScaleMod ASM-patches ScaledResolution to divide dimensions by a user-set
 * multiplier. That breaks BetterVisuals' HUD positioning (hotbar/statusbars
 * get computed against shrunken dimensions). We toggle bypass around BV's
 * render so any ScaledResolution it constructs gets vanilla values.
 *
 * No hard dep on GuiScaleMod — reflection lookup fails silently if absent.
 */
object GuiScaleBypass {

    private var initialized = false
    private var setBypass: Method? = null
    private var getMultiplier: Method? = null

    private fun ensureInit() {
        if (initialized) return
        initialized = true
        try {
            val hookCls = Class.forName("com.example.guiscale.GuiScaleHook")
            setBypass = hookCls.getMethod("setBypass", Boolean::class.javaPrimitiveType)
        } catch (_: Throwable) {
            // GuiScaleMod not installed — no-op
        }
        try {
            val modCls = Class.forName("com.example.guiscale.GuiScaleMod")
            getMultiplier = modCls.getMethod("getScaleMultiplier")
        } catch (_: Throwable) {
            // no-op
        }
    }

    /** Returns GuiScaleMod's multiplier, or 1.0 if mod absent. */
    fun multiplier(): Float {
        ensureInit()
        return try {
            (getMultiplier?.invoke(null) as? Float) ?: 1f
        } catch (_: Throwable) {
            1f
        }
    }

    fun enable() {
        ensureInit()
        try { setBypass?.invoke(null, true) } catch (_: Throwable) {}
    }

    fun disable() {
        ensureInit()
        try { setBypass?.invoke(null, false) } catch (_: Throwable) {}
    }

    fun <T> wrap(block: () -> T): T {
        enable()
        try {
            return block()
        } finally {
            disable()
        }
    }
}
