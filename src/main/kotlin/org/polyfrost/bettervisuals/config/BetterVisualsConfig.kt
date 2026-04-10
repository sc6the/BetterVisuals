package org.polyfrost.bettervisuals.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Checkbox
import cc.polyfrost.oneconfig.config.annotations.Dropdown
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
    var shadowEnabled = true

    @JvmField
    @Slider(name = "Shadow Opacity", category = "Hotbar", min = 0F, max = 100F)
    var shadowOpacity = 40

    @JvmField
    @Slider(name = "Shadow Spread", category = "Hotbar", min = 4F, max = 40F)
    var shadowSpread = 16

    @JvmField
    @Slider(name = "Bottom Offset", category = "Hotbar", min = 0F, max = 40F)
    var bottomOffset = 4

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
    @Switch(name = "Shadow", category = "Status Bars", subcategory = "Shadow")
    var statusBarShadow = true

    @JvmField
    @Slider(name = "Shadow Opacity", category = "Status Bars", subcategory = "Shadow", min = 0F, max = 100F)
    var statusBarShadowOpacity = 20

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

    // ===================== Chat =====================

    @JvmField
    @Switch(name = "Custom Chat", category = "Chat")
    var chatEnabled = true

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(name = "Background Color", category = "Chat")
    var chatBgColor: OneColor = OneColor(0, 0, 0, 128)

    @JvmField
    @Slider(name = "Corner Radius", category = "Chat", min = 0F, max = 8F)
    var chatRadius = 3f

    @JvmField
    @Switch(name = "Shadow", category = "Chat")
    var chatShadow = false

    @JvmField
    @Slider(name = "Shadow Opacity", category = "Chat", min = 0F, max = 100F)
    var chatShadowOpacity = 20

    // ===================== Tab List =====================

    @JvmField
    @Switch(name = "Custom Tab List", category = "Tab List")
    var tabEnabled = true

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(name = "Background Color", category = "Tab List")
    var tabBgColor: OneColor = OneColor(0, 0, 0, 80)

    @JvmField
    @Slider(name = "Corner Radius", category = "Tab List", min = 0F, max = 8F)
    var tabRadius = 4f

    @JvmField
    @Switch(name = "Shadow", category = "Tab List")
    var tabShadow = true

    @JvmField
    @Slider(name = "Shadow Opacity", category = "Tab List", min = 0F, max = 100F)
    var tabShadowOpacity = 20

    // ===================== Scoreboard =====================

    @JvmField
    @Switch(name = "Custom Scoreboard", category = "Scoreboard")
    var scoreboardEnabled = true

    @JvmField
    @Dropdown(name = "Side", category = "Scoreboard", options = ["Right", "Left"])
    var scoreboardSide = 0

    @JvmField
    @cc.polyfrost.oneconfig.config.annotations.Color(name = "Background Color", category = "Scoreboard")
    var scoreboardBgColor: OneColor = OneColor(0, 0, 0, 80)

    @JvmField
    @Slider(name = "Corner Radius", category = "Scoreboard", min = 0F, max = 8F)
    var scoreboardRadius = 4f

    @JvmField
    @Switch(name = "Shadow", category = "Scoreboard")
    var scoreboardShadow = true

    @JvmField
    @Slider(name = "Shadow Opacity", category = "Scoreboard", min = 0F, max = 100F)
    var scoreboardShadowOpacity = 30

    @JvmField
    @Switch(name = "Show Score Numbers", category = "Scoreboard")
    var scoreboardNumbers = false

    @JvmField
    @Slider(name = "X Offset", category = "Scoreboard", subcategory = "Position", min = -960F, max = 960F)
    var scoreboardX = 0

    @JvmField
    @Slider(name = "Y Offset", category = "Scoreboard", subcategory = "Position", min = -540F, max = 540F)
    var scoreboardY = 0

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
        addDependency("shadowEnabled", "enabled")
        addDependency("shadowOpacity", "shadowEnabled")
        addDependency("shadowSpread", "shadowEnabled")
        addDependency("bottomOffset", "enabled")
        addDependency("hotbarX", "enabled")
        addDependency("hotbarY", "enabled")

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
        addDependency("statusBarShadow", "statusBarsEnabled")
        addDependency("statusBarShadowOpacity", "statusBarShadow")

        // Blur
        addDependency("blurRadius", "blurEnabled")

        // Held Item
        addDependency("heldItemBgColor", "heldItemBg")
        addDependency("heldItemRadius", "heldItemBg")
        addDependency("heldItemX", "enabled")
        addDependency("heldItemY", "enabled")

        // Chat
        addDependency("chatBgColor", "chatEnabled")
        addDependency("chatRadius", "chatEnabled")
        addDependency("chatShadow", "chatEnabled")
        addDependency("chatShadowOpacity", "chatShadow")

        // Tab List
        addDependency("tabBgColor", "tabEnabled")
        addDependency("tabRadius", "tabEnabled")
        addDependency("tabShadow", "tabEnabled")
        addDependency("tabShadowOpacity", "tabShadow")

        // Scoreboard
        addDependency("scoreboardSide", "scoreboardEnabled")
        addDependency("scoreboardBgColor", "scoreboardEnabled")
        addDependency("scoreboardRadius", "scoreboardEnabled")
        addDependency("scoreboardShadow", "scoreboardEnabled")
        addDependency("scoreboardShadowOpacity", "scoreboardShadow")
        addDependency("scoreboardNumbers", "scoreboardEnabled")
        addDependency("scoreboardX", "scoreboardEnabled")
        addDependency("scoreboardY", "scoreboardEnabled")
    }
}
