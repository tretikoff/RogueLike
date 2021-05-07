package com.bomjRogue.config

import com.bomjRogue.world.Size
import com.bomjRogue.world.interactive.Sword

object Utils {
    const val fleshHitSoundName = "hit_body.mp3"
    const val swordHitSoundName = "hit.mp3"
    const val itemPickUpSoundName = "sword_get.mp3"
    const val healthPickUpSoundName = "health_up.mp3"
//        const val enemySlayedSoundName = "sword_get.mp3" // todo
}

object SettingsManager {
    const val defaultNpcCount = 5
    const val defaultDetectRadius = 55.0
    const val defaultHealth = 100
    const val defaultArmor = 10
    const val defaultForce = 20
    const val dropChance = 20
    val swordSize = Size(40f, 40f)
    val healthSize = Size(25f, 25f)
    const val defaultHealthBoost = 50
    const val defaultDamageValue = 5
    val defaultSword = Sword(defaultDamageValue)
}

object ConnectionManager {
    const val host = "127.0.0.1"
    const val port = 8084
}

object ConfigManager {
    const val width = 1280f
    const val height = 720f
}