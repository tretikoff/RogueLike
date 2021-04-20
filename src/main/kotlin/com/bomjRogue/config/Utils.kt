package com.bomjRogue.config

import com.bomjRogue.game.strategy.AggressiveMovement
import com.bomjRogue.game.strategy.RandomMovement
import com.bomjRogue.game.strategy.MovementStrategy
import com.bomjRogue.world.interactive.Sword
import kotlin.reflect.KClass

class Utils {
    companion object {
        const val fleshHitSoundName = "hit_body.mp3"
        const val swordHitSoundName = "hit.mp3"
        const val itemPickUpSoundName = "sword_get.mp3"
        const val healthPickUpSoundName = "health_up.mp3"
//        const val enemySlayedSoundName = "sword_get.mp3" // todo
    }
}

enum class Strategies(val strategy: KClass<out MovementStrategy>) {
    DEFAULT(RandomMovement::class),
    HUNT(AggressiveMovement::class)
}

class SettingsManager {
    companion object {
        const val defaultNpcCount = 5
        val defaultStrategy = Strategies.DEFAULT
        const val defaultDetectRadius = 55.0
        const val defaultHealth = 100
        const val defaultArmor = 10
        const val defaultForce = 20
        val defaultSword = Sword(5)
    }
}