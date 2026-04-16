# BetterVisuals

Minecraft **1.8.9 Forge** client mod: a cleaner, animated HUD with a customizable hotbar and status bars. Settings live in [**OneConfig**](https://github.com/Polyfrost/OneConfig).

Inspired by Polyfrost tooling (credits in `mcmod.info`).

## Requirements

- Minecraft **1.8.9** with **Forge**
- [**OneConfig**](https://github.com/Polyfrost/OneConfig) (loaded with the mod)

## Features

| Area | What you get |
|------|----------------|
| **Hotbar** | Custom bar, animation speed, snapping, colors, corner radius, glow, position offsets, optional blur behind UI, held-item background |
| **Status bars** | Health, armor, hunger, XP, air, level colors; ghost health bar; spacing, height, animation; glows |

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
