package org.polyfrost.bettervisuals.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Checkbox
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import org.polyfrost.bettervisuals.BetterVisuals
import org.polyfrost.bettervisuals.utils.ConfigPersistence

object BetterVisualsConfig : Config(
    Mod(BetterVisuals.NAME, ModType.UTIL_QOL),
    "${BetterVisuals.ID}.json"
) {

    // ===================== Hotbar =====================

    @JvmField
    @Switch(name = "Custom Hotbar", category = "Hotbar")
    var enabled = true

    @JvmField
    @Slider(name = "Animation Speed", category = "Hotbar", min = 1F, max = 100F)
    var animationSpeed = 10

    @JvmField
    @Slider(name = "Snap Distance", category = "Hotbar", subcategory = "Snapping",
        description = "Pixels from target to snap. 0 = no snap.", min = 0F, max = 10F)
    var snapDistance = 2f

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(name = "Bar Color", category = "Hotbar")
    var barColor: OneColor = OneColor(0, 0, 0, 200)

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(name = "Highlight Color", category = "Hotbar")
    var highlightColor: OneColor = OneColor(60, 170, 255, 80)

    @JvmField
    @Slider(name = "Corner Radius", category = "Hotbar", min = 0F, max = 10F)
    var cornerRadius = 4

    @JvmField
    @Switch(name = "Shadow", category = "Hotbar")
    var glowEnabled = true

    @JvmField
    @Slider(name = "Shadow Opacity", category = "Hotbar", min = 0F, max = 100F)
    var glowOpacity = 40

    @JvmField
    @Slider(name = "Shadow Spread", category = "Hotbar", min = 4F, max = 40F)
    var glowSpread = 16

    @JvmField
    @Slider(name = "Bottom Offset", category = "Hotbar", min = 0F, max = 40F)
    var bottomOffset = 4

    @JvmField
    @Slider(name = "HUD Size", category = "Hotbar", subcategory = "Size",
        description = "Overall scale for hotbar + status bars. 1.0 = vanilla.",
        min = 0.5F, max = 3.0F)
    var hudScale = 1.0f

    // Slot Numbers
    @JvmField
    @Switch(name = "Slot Numbers", category = "Hotbar", subcategory = "Slot Numbers",
        description = "Show 1-9 in the top-right corner of each hotbar slot.")
    var slotNumbersEnabled = false

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(
        name = "Number Color", category = "Hotbar", subcategory = "Slot Numbers")
    var slotNumberColor: OneColor = OneColor(255, 255, 255, 255)

    @JvmField
    @Switch(name = "Number Shadow", category = "Hotbar", subcategory = "Slot Numbers")
    var slotNumberGlow = false

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(
        name = "Shadow Color", category = "Hotbar", subcategory = "Slot Numbers")
    var slotNumberGlowColor: OneColor = OneColor(60, 170, 255, 200)

    @JvmField
    @Slider(name = "Number Scale", category = "Hotbar", subcategory = "Slot Numbers",
        min = 0.3F, max = 1.5F)
    var slotNumberScale = 0.6f

    @JvmField
    @Slider(name = "Number X Offset", category = "Hotbar", subcategory = "Slot Numbers",
        min = -16F, max = 16F)
    var slotNumberX = 0

    @JvmField
    @Slider(name = "Number Y Offset", category = "Hotbar", subcategory = "Slot Numbers",
        min = -16F, max = 16F)
    var slotNumberY = 0

    @JvmField
    @Slider(name = "X Offset", category = "Hotbar", subcategory = "Position", min = -960F, max = 960F)
    var hotbarX = 0

    @JvmField
    @Slider(name = "Y Offset", category = "Hotbar", subcategory = "Position", min = -540F, max = 540F)
    var hotbarY = 0

    // ===================== Status Bars =====================

    @JvmField
    @Switch(name = "Custom Status Bars", category = "Status Bars")
    var statusBarsEnabled = true

    @JvmField
    @Slider(name = "Bar Height", category = "Status Bars", min = 6F, max = 16F)
    var statusBarHeight = 10

    @JvmField
    @Slider(name = "Corner Radius", category = "Status Bars", min = 0F, max = 8F)
    var statusBarRadius = 3f

    @JvmField
    @Slider(name = "Spacing", category = "Status Bars", min = 1F, max = 8F)
    var statusBarSpacing = 2

    @JvmField
    @Slider(name = "Gap Between Health/Hunger", category = "Status Bars", min = 1F, max = 10F)
    var statusBarGap = 4

    @JvmField
    @Slider(name = "Gap from Hotbar", category = "Status Bars", min = 1F, max = 20F)
    var statusBarGapFromHotbar = 2f

    @JvmField
    @Slider(name = "Animation Speed", category = "Status Bars", min = 1F, max = 50F)
    var statusBarAnimSpeed = 15

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(name = "Track Color", category = "Status Bars")
    var trackColor: OneColor = OneColor(80, 80, 80, 200)

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(name = "Health Color", category = "Status Bars", subcategory = "Colors")
    var healthColor: OneColor = OneColor(231, 76, 60, 255)

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(name = "Armor Color", category = "Status Bars", subcategory = "Colors")
    var armorColor: OneColor = OneColor(169, 169, 169, 255)

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(name = "Hunger Color", category = "Status Bars", subcategory = "Colors")
    var hungerColor: OneColor = OneColor(230, 126, 34, 255)

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(name = "XP Color", category = "Status Bars", subcategory = "Colors")
    var xpColor: OneColor = OneColor(126, 211, 33, 255)

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(name = "Air Color", category = "Status Bars", subcategory = "Colors")
    var airColor: OneColor = OneColor(100, 180, 255, 255)

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(name = "Level Text Color", category = "Status Bars", subcategory = "Colors")
    var levelColor: OneColor = OneColor(255, 255, 255, 255)

    @JvmField
    @Switch(name = "Health Ghost Bar", category = "Status Bars", subcategory = "Ghost")
    var healthGhostEnabled = true

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(name = "Ghost Color", category = "Status Bars", subcategory = "Ghost")
    var healthGhostColor: OneColor = OneColor(255, 255, 255, 120)

    @JvmField
    @Switch(name = "Glow", category = "Status Bars", subcategory = "Glow")
    var statusBarGlow = true

    @JvmField
    @Slider(name = "Glow Opacity", category = "Status Bars", subcategory = "Glow", min = 0F, max = 100F)
    var statusBarGlowOpacity = 20

    // ===================== Blur =====================

    @JvmField
    @Switch(name = "Blur Behind Elements", category = "Hotbar", subcategory = "Blur")
    var blurEnabled = false

    @JvmField
    @Slider(name = "Blur Radius", category = "Hotbar", subcategory = "Blur", min = 2F, max = 20F)
    var blurRadius = 8f

    // ===================== Held Item =====================

    @JvmField
    @Switch(name = "Held Item Background", category = "Hotbar", subcategory = "Held Item")
    var heldItemBg = true

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(name = "Held Item BG Color", category = "Hotbar", subcategory = "Held Item")
    var heldItemBgColor: OneColor = OneColor(0, 0, 0, 120)

    @JvmField
    @Slider(name = "Held Item Radius", category = "Hotbar", subcategory = "Held Item", min = 0F, max = 8F)
    var heldItemRadius = 3f

    @JvmField
    @Slider(name = "Held Item X Offset", category = "Hotbar", subcategory = "Held Item", min = -200F, max = 200F)
    var heldItemX = 0

    @JvmField
    @Slider(name = "Held Item Y Offset", category = "Hotbar", subcategory = "Held Item", min = -100F, max = 100F)
    var heldItemY = 0

    fun saveSettings(): Boolean {
        return ConfigPersistence.saveConfig(this)
    }

    fun loadSettings(): Int {
        return ConfigPersistence.loadConfig(this)
    }

    private fun registerSaveListeners() {
        val saveRunnable = Runnable {
            BetterVisuals.markDirty()
            saveSettings()
        }
        for ((name, _) in optionNames) {
            addListener(name, saveRunnable)
        }
    }

    init {
        initialize()
        registerSaveListeners()

        // Hotbar
        addDependency("animationSpeed", "enabled")
        addDependency("snapDistance", "enabled")
        addDependency("barColor", "enabled")
        addDependency("highlightColor", "enabled")
        addDependency("cornerRadius", "enabled")
        addDependency("glowEnabled", "enabled")
        addDependency("glowOpacity", "glowEnabled")
        addDependency("glowSpread", "glowEnabled")
        addDependency("bottomOffset", "enabled")
        addDependency("hotbarX", "enabled")
        addDependency("hotbarY", "enabled")
        addDependency("hudScale", "enabled")

        addDependency("slotNumberColor", "slotNumbersEnabled")
        addDependency("slotNumberGlow", "slotNumbersEnabled")
        addDependency("slotNumberGlowColor", "slotNumberGlow")
        addDependency("slotNumberScale", "slotNumbersEnabled")
        addDependency("slotNumberX", "slotNumbersEnabled")
        addDependency("slotNumberY", "slotNumbersEnabled")

        // Status Bars
        addDependency("statusBarHeight", "statusBarsEnabled")
        addDependency("statusBarRadius", "statusBarsEnabled")
        addDependency("statusBarSpacing", "statusBarsEnabled")
        addDependency("statusBarGap", "statusBarsEnabled")
        addDependency("statusBarGapFromHotbar", "statusBarsEnabled")
        addDependency("statusBarAnimSpeed", "statusBarsEnabled")
        addDependency("trackColor", "statusBarsEnabled")
        addDependency("healthColor", "statusBarsEnabled")
        addDependency("armorColor", "statusBarsEnabled")
        addDependency("hungerColor", "statusBarsEnabled")
        addDependency("xpColor", "statusBarsEnabled")
        addDependency("airColor", "statusBarsEnabled")
        addDependency("levelColor", "statusBarsEnabled")
        addDependency("healthGhostEnabled", "statusBarsEnabled")
        addDependency("healthGhostColor", "healthGhostEnabled")
        addDependency("statusBarGlow", "statusBarsEnabled")
        addDependency("statusBarGlowOpacity", "statusBarGlow")

        // Blur
        addDependency("blurRadius", "blurEnabled")

        // Held Item
        addDependency("heldItemBgColor", "heldItemBg")
        addDependency("heldItemRadius", "heldItemBg")
        addDependency("heldItemX", "enabled")
        addDependency("heldItemY", "enabled")
    }
}
