# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Minecraft/Forge Sources

The decompiled Java sources for Minecraft/Forge (1.20.1, ForgeGradle) are already extracted to `C:\Users\delvi\.gradle\mc-sources\1.20.1-forge\` (normal package layout, e.g. `net/minecraft/world/entity/LivingEntity.java`) — read directly from there with Read/Grep/Glob instead of asking the user.

If missing or needing regeneration, the source jar is at `~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/<version>_mapped_parchment_<version>/forge-*-sources.jar` — extract it with `unzip` into the folder above, discarding the `.class` files.
