# 2.1.0.0
* The config is now a SERVER config and is synced to clients, so on dedicated servers the stamina bar and sprinting follow the server's values
  * The config file is now `config/stamina-server.toml`. `saves/<world>/serverconfig/stamina-server.toml` can be used as a per-world override
  * The old `stamina-common.toml` is no longer read: if you customized it, copy your values to `stamina-server.toml` manually
* Stamina is now required on both client and server: clients without the mod can no longer join servers with it, and clients with the mod can no longer join servers without it (this fixes a crash when joining a server without Stamina)
* Fixed a crash if Vigour or Curse of Weariness are removed by a datapack
* Jumping no longer consumes stamina in creative and spectator mode, or when both sprinting and swimming are disabled
* Stamina consumed by jumping is now affected by the out of combat multiplier, like sprinting
  * `StaminaEvent.Consumed` is now also fired for jumps
* Fixed the stamina overlay not shaking like the hearts at low health with odd max health, with "Bound to max health" enabled, or right after max health decreased
* Fixed stamina regeneration draining stamina instead with very high armor (or a high "Reduction per armor point")
* If max stamina is 0 (e.g. too much armor or Curse of Weariness), stamina is now locked instead of behaving erratically
* "In combat" is now handled by InsaneLib's In Combat feature
  * Fixed players attacking other players not being considered in combat
  * Added "In combat time" config option (default 15 seconds) to set for how long after attacking or being hurt the player is considered in combat
* Slowdown modifiers are now changed only when needed instead of every tick, so the server no longer sends an attributes update every tick for slowed down players
* Removed stamina consumption when mining ("Mine" config option, disabled by default) and the halved mining speed when stamina is locked
* Mobs Properties Randomness: the `stamina:stamina` condition (non flat) now uses the current max stamina, like `stamina:set` and `stamina:add`, instead of the max stamina at full health
* Config: "Unlock at health ratio" is now "Unlock at stamina ratio", as it's the stamina percentage that unlocks stamina, not health
* Config: fixed and added missing descriptions
* `/insanestamina set|regen|consume` now give feedback in chat and return the number of affected players
* Fixed `/insanestamina get` showing the raw `%.1f` in Italian; the stamina is now shown with one decimal
* The Bonus Stamina attribute is now only added to players instead of every entity
* Fixed stamina not being locked when health dropped below the threshold while sprinting
* Fixed slowdowns staying applied if both sprinting and swimming are disabled while the server is running
* Fixed stamina staying locked when full (e.g. after setting it with `/insanestamina set`)
* Fixed sprint/locked slowdown not being updated (and stamina not being synced) on ticks where stamina consumed or regenerated was 0 (e.g. set to 0 by config or by another mod through `StaminaEvent`)
* Requires InsaneLib 2.4.34.0 or newer

# 2.0.0.4
* Fixed the stamina overlay shaking differently from the hearts at low health after a few minutes of playing

# 2.0.0.3
* Fixed Vigour not being obtainable from the enchanting table, villagers and loot

# 2.0.0.2
* Server startup crash fix

# 2.0.0.1
* Fixed rune enchanting curse not being a curse

# 2.0.0.0
Port to 1.21.1 (NeoForge)
* Vigour enchantment is now data-driven
  * Now increases max stamina by 20% per level instead of a flat amount
* Tinkers' Construct integration removed
* Added Curse of Weariness, a curse enchantment that reduces max stamina by 40% (opposite of Vigour)
  * Like other curses, it's not obtainable from the enchanting table or villagers, only from loot and fishing
  * Can't be on the same piece as Vigour
* If Rune Enchanting is installed, Vigour and Curse of Weariness are also available as chestplate runes