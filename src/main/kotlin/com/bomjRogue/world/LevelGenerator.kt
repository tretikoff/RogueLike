package com.bomjRogue.world

import com.bomjRogue.config.SettingsManager.Companion.defaultDamageValue
import com.bomjRogue.config.SettingsManager.Companion.defaultHealthBoost
import com.bomjRogue.world.interactive.Health
import com.bomjRogue.world.interactive.Item
import com.bomjRogue.world.interactive.Sword

class LevelGenerator {
    companion object {
        fun generateMap(): Map {
            return Map(MazeGenerator(13, 7).getMaze(), 720f, 1280f)
        }

        fun generateSwordItem(damage: Int = defaultDamageValue) : Item = Sword(damage)

        fun generateHealthItem(healthPoints: Int = defaultHealthBoost) = Health(healthPoints)
    }
}