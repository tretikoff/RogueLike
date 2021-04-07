package com.bomjRogue

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

fun main() {
    val server = getOrCreateServer()
    val client = GameClient(server)
    val config = LwjglApplicationConfiguration().apply {
        width = 1280
        height = 720
    }

    server.join(client.player)
    LwjglApplication(client, config)
}