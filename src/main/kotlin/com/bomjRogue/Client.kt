@file:JvmName("Client")

package com.bomjRogue

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.bomjRogue.config.ConfigManager
import com.bomjRogue.game.GameClient

fun main(args: Array<String>) {
    val client = GameClient(args[0], args[1].toInt())
    val config = LwjglApplicationConfiguration().apply {
        width = ConfigManager.width.toInt()
        height = ConfigManager.height.toInt()
    }

    LwjglApplication(client, config)
}