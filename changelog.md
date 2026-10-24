# 3.0.1.0-beta
* Ported 2.1.0.0 changes from 1.21.1
* The config is now a SERVER config and is synced to clients, so on dedicated servers the stamina bar and sprinting follow the server's values
  * The config file is now `config/stamina-server.toml`. `saves/<world>/serverconfig/stamina-server.toml` can be used as a per-world override
  * The old `stamina-common.toml` is no longer read: if you customized it, copy your values to `stamina-server.toml` manually
* Stamina is now required on both client and server: clients without the mod can no longer join servers with it, and clients with the mod can no longer join servers without it (this fixes a crash when joining a server without Stamina)
* Requires InsaneLib 3.0.1.0-beta or newer
* Removed stamina consumption when mining ("Mine" config option, disabled by default) and the halved mining speed when stamina is locked
* Jumping no longer consumes stamina in creative and spectator mode, or when both sprinting and swimming are disabled
* Stamina consumed by jumping is now affected by the out of combat multiplier, like sprinting
* Fixed players attacking other players not being considered in combat
* Added "In combat time" config option (default 15 seconds) to set for how long after attacking or being hurt the player is considered in combat
* Fixed stamina not being locked when health dropped below the threshold while sprinting
* If max stamina is 0 (e.g. too much armor or Curse of Weariness), stamina is now locked instead of behaving erratically
* Fixed stamina regeneration draining stamina instead with very high armor (or a high "Reduction per armor point")
* Fixed stamina staying locked when full (e.g. after setting it with `/insanestamina set`)
* Config: "Unlock at health ratio" is now "Unlock at stamina ratio", as it's the stamina percentage that unlocks stamina, not health
* Fixed the stamina overlay not shaking like the hearts at low health with odd max health, with "Bound to max health" enabled, or right after max health decreased
* Fixed a crash if Vigour or Curse of Weariness are removed by a datapack
* Fixed sprint/locked slowdown not being updated (and stamina not being synced) on ticks where stamina consumed or regenerated was 0 (e.g. set to 0 by config or by another mod through `StaminaEvent`)
* Fixed slowdowns staying applied if both sprinting and swimming are disabled while the server is running
* `/insanestamina set|regen|consume` now give feedback in chat and return the number of affected players
* Fixed `/insanestamina get` showing the raw `%.1f` in Italian; the stamina is now shown with one decimal
* Config: fixed and added missing descriptions
* "In combat" is now handled by InsaneLib's In Combat feature
* `StaminaEvent.Consumed` is now also fired for jumps
* Slowdown modifiers are now changed only when needed instead of every tick, so the server no longer sends an attributes update every tick for slowed down players
* The Bonus Stamina attribute is now only added to players instead of every entity

# 3.0.0.1-beta
* Fixed the stamina overlay shaking differently from the hearts at low health after a few minutes of playing

# 3.0.0.0-beta
* Ported to Minecraft 26.1.2
* Mobs Properties Randomness and Rune Enchanting integrations temporarily disabled (they're not yet available for 26.1.2)
* The stamina info in the F3 debug screen is now a debug entry (`stamina:stamina`), shown by default and toggleable from the debug options screen
* Lunge now consumes 40 stamina per level and can't be used when stamina is locked or there's not enough stamina
* Fixed Vigour not being obtainable from the enchanting table, villagers and loot

## Data Pack / Modpack creators
* Added `stamina:consume_stamina` enchantment entity effect (`amount`: level based value), consumes the player's stamina
* Added `stamina:has_stamina` loot condition (`amount`: level based value), passes if the player's stamina is not locked and is at least `amount`. Always passes for non-players and players in creative/spectator
* `minecraft:lunge` is overridden to use them
