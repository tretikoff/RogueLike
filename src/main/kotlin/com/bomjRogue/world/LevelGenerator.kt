package com.bomjRogue.world

import com.bomjRogue.config.SettingsManager.defaultDamageValue
import com.bomjRogue.config.SettingsManager.defaultHealthBoost
import com.bomjRogue.config.ConfigManager.width
import com.bomjRogue.config.ConfigManager.height
import com.bomjRogue.world.interactive.Health
import com.bomjRogue.world.interactive.Item
import com.bomjRogue.world.interactive.Sword

class LevelGenerator {
    companion object {
        fun generateMap(): Map {
            var mazeStep = 98f
            var mazeWidth = (width / mazeStep).toInt()
            var mazeHeight = (height / mazeStep).toInt()
            return Map(MazeGenerator(mazeWidth, mazeHeight).getMaze(), height, width)
        }

        fun generateSwordItem(damage: Int = defaultDamageValue) : Item = Sword(damage)

        fun generateHealthItem(healthPoints: Int = defaultHealthBoost) = Health(healthPoints)
    }
}