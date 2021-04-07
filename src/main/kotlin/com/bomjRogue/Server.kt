package com.bomjRogue

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

var game: GameServer? = null
fun main() {
    getOrCreateServer()
}

fun getOrCreateServer(): GameServer {
    if (game == null) {
        val config = LwjglApplicationConfiguration().apply {
            width = 1280
            height = 720
        }
        game = GameServer()
        LwjglApplication(game, config)
    }
    return game!!
}