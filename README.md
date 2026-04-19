# BetterVisuals

Minecraft **1.8.9 Forge** client mod: a cleaner, animated HUD with a customizable hotbar and status bars. Settings live in [**OneConfig**](https://github.com/Polyfrost/OneConfig).

Inspired by Polyfrost tooling (credits in `mcmod.info`).

## Requirements

- Minecraft **1.8.9** with **Forge**
- [**OneConfig**](https://github.com/Polyfrost/OneConfig) (loaded with the mod)

## Features

| Area | What you get |
|------|----------------|
| **Hotbar** | Custom bar, animation speed, snapping, colors, corner radius, shadow, position offsets, optional blur behind UI, held-item background |
| **Status bars** | Health, armor, hunger, XP, air, level colors; ghost health bar; spacing, height, animation; glows |
| **Misc** | Disable Hand Item Lighting, Custom Hand Item FOV, Server Preview in Direct Connect, Last Server Joined Button, No Creative Drift, Hide Armor, No Nicknames |
| **SkinForce** | Override your own skin locally with a PNG file, switchable between classic and slim models; auto-refreshes on select and model change |

### Credits

- Disable Hand Item Lighting, Custom Hand Item FOV, Server Preview in Direct Connect, and Last Server Joined Button are ported from [Polyfrost/REDACTION](https://github.com/Polyfrost/REDACTION).
- No Creative Drift is inspired by [darkpred/NoCreativeDrift](https://github.com/darkpred/NoCreativeDrift).
- Hide Armor and No Nicknames are re-implementations of widely available QoL mods of the same name.

Config is saved to disk and synced with OneConfig; changes can be persisted automatically.

## Commands

| Command | Action |
|---------|--------|
| `/bettervisuals` or `/bv` | Open the OneConfig GUI |
| `/bv save` | Save settings to file |
| `/bv load` | Load settings from file |
| `/bv debug` | Print config path and debug info in chat |

## Building

From the project root:

**Windows**

```bat
gradlew.bat build
```

**macOS / Linux**

```bash
./gradlew build
```

The built JAR is under:

`versions/1.8.9-forge/build/libs/`

Output name follows `gradle.properties` (`mod_archives_name` and `mod_version`).

## Install

1. Build or download the release JAR.
2. Put it in your `.minecraft/mods` folder (for 1.8.9) with Forge and OneConfig.

## Repository

[github.com/sc6the/BetterVisuals](https://github.com/sc6the/BetterVisuals)
