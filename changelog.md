# Changelog

## 1.5.0.1
* Fixed overlay rendering 1 pixel too low with absorption and without Mantle installed

## 1.5.0.0
* Added `/insanestamina` command
  * Lets you get, set, regen and consume the stamina of players
* Added `stamina:stamina` condition and `stamina:set` and `stamina:add` properties for Mobs Properties Randomness

## 1.4.3
* Halved hunger consumed when sprinting with stamina locked
* Added config option to lock stamina if player's max health is below a certain value (4 by default)

## 1.4.2
* Fixed overlay rendering over absorption

## 1.4.1
* Added config option to change regeneration when in water
* Fixed Vigour enchantment description

## 1.4.0
Requires InsaneLib 1.21.12 and forge 47.4.0  
**Config options might have been reset**
* Stamina consumption is now reduced by 20% when out of combat
  * Out of combat = not attacked or hurt in the last 15 seconds
* Fixed empty stamina on respawn

## Beta 1.3.1
* Stamina overlay changed again
  * Reverted to original texture, more or less
  * Locked and unlocked overlay are now two separate textures instead of being hardcoded

## Beta 1.3.0
* New feature: Consume hunger on locked stamina
  * If stamina is locked you can still run now, at the cost of great amounts of hunger
  * You consume 1 hunger / saturation every 2 seconds of sprinting
  * This disables itself if No Hunger mod is installed
* New feature (disabled by default): Bound to Max Health
  * Max stamina is now bound to max health instead of current health
* Changed the rendering of stamina overlay
  * New texture
  * Fixes compatibility with Mantle hearts display
* Removed Regen.Increased above health

## 1.2.2
* Fixed max Vigour modifier level
* Hopefully fixed stamina overlay going over health bar

## 1.2.1
* Added Tinkers' Construct integration
  * Added Vigour modifier
* Added Italian Translation

## 1.2.0
* Stamina and regen is now reduced by armor (2.5% per armor point)
* Slowdown due to low / locked stamina is now applied to swimming too

## 1.1.3
* Increased default stamina
  * Stamina per half-heart (5 -> 10)
  * Bonus stamina per level of Vigour Enchantment (25 -> 40)
  * Bonus stamina per level of Vigour effect (25 -> 40)
  * Stamina consumed on jump (5 -> 10)
  * Regen stamina per tick (0.6 -> 1.0)
  * Lock stamina below (25% -> 20%)
  * Unlock at (50% -> 40%)
  * Slowdown sprinting below (25% -> 20%)
  * Slowdown sprinting below (25 -> 40)

## 1.1.2
* Fixed stamina not starting to regenerate on respawn until you mined a block

## 1.1.1
* Now requires InsaneLib 1.15.0

## 1.1.0
* Config options have been sorted and categorized
  * Stamina regen when locked is now a modifier
* Added experimental config option to consume stamina when mining
  * Disabled by default. When stamina is locked, mining speed is halved
* Changed slowdown mechanic
  * When the stamina is locked the player is now slowed down by 10%, no more thresholds
  * When the player is sprinting and stamina is below 25% or below 25, sprinting will be less effective (33%)
* Fixed stamina not synced when switching dimensions
* Fixed stamina not locked below 5 max health

## 1.0.2
* Added a config option to disable swimming (split from sprinting)

## 1.0.1
* Fixed startup crash

## 1.0.0
* Ported from IguanaTweaks Reborn
* Added `stamina:bonus_stamina` attribute
* Vigour enchantment and effect now increase max stamina by 25 per level instead of reducing consumption
* Added more config options
  * 'Bonus stamina per level of Vigour Enchantment', 'Bonus stamina per level of Vigour Effect', 'Conduit swimming modifier', 'Increased regen above health', 'Lock Stamina below health ratio'
* Fixed stamina overlay not bobbing up and down with regeneration or when low health