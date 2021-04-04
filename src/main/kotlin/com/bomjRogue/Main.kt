package com.bomjRogue

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration

fun main() {
    val config = LwjglApplicationConfiguration().apply {
        width = 1280
        height = 720
    }

    LwjglApplication(Game(), config)
}