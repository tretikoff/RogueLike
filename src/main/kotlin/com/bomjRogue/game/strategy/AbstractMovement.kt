package com.bomjRogue.game.strategy

import com.bomjRogue.character.GameCharacter
import com.bomjRogue.character.Player
import com.bomjRogue.config.SettingsManager
import com.bomjRogue.game.Direction
import com.bomjRogue.world.Map
import com.bomjRogue.world.Position
import com.bomjRogue.world.interactive.GameObject
import kotlin.collections.Map.Entry

abstract class AbstractMovement(map: Map): RandomMovement(map = map) {
    private var detectRadius = SettingsManager.defaultDetectRadius
        set(value) {
            if (value > map.mapWidth || value <= 0) {
                return
            }
            field = value
        }

    abstract fun getDirection(character: GameCharacter, resolvedPlayer: Entry<GameObject, Position>): Direction

    override fun makeMove(character: GameCharacter) {
        val players = map.location.asSequence().filter {
            it.key is Player
        }.toList()

        val resolvedPlayer = players.find {
            map.isClose(character, it.key, detectRadius)
        }
        if (resolvedPlayer == null) {
            super.makeMove(character)
            return
        }

        val newDirection = getDirection(character, resolvedPlayer)
        if (map.isIntersectWithWalls(character, newDirection)) {
            super.makeMove(character)
            return
        }
        character.direction = newDirection
        val (x, y) = character.getCoordinateMoveDirection()
        map.move(character, x, y)
    }
}