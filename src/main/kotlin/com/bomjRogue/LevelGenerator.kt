package com.bomjRogue

class LevelGenerator {
    companion object {
        fun generateMap(): Map {
            return Map(MazeGenerator(13, 7).getMaze(), 720f, 1280f)
        }
    }
}