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
