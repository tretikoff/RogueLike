package com.bomjRogue

class LevelGenerator {

    companion object {
        val width = 1280f
        val height = 720f
        fun generateMap(): Map {
            return Map(width, height)
        }
    }
}