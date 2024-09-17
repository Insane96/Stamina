# Changelog

## Upcoming
* Config options have been sorted and categorized
  * Stamina regen when locked is now a modifier
* Added experimental config option to consume stamina when mining
  * Disabled by default. When stamina is locked, mining speed is halved
* Changed slowdown mechanic
  * When the stamina is locked the player is now slowed down by 10%, no more thresholds
  * When the player is sprinting and stamina is below 25% or below 25, sprinting will be less effective (33%)
* Fixed stamina not synced when switching dimensions
* Fixed stamina not locked below 2.5 max health

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