package com.bomjRogue.game.strategy

import com.bomjRogue.character.Npc
import com.bomjRogue.game.Direction
import com.bomjRogue.world.Map
import com.bomjRogue.character.GameCharacter
import kotlin.random.Random

open class RandomMovement() : MovementStrategy() {
    override fun makeMove(character: GameCharacter, map: Map) {
        if (character is Npc) {
            val randValue = Random.nextInt(100)
            if (randValue < randomThreshold) {
                character.direction = Direction.values().random()
                return
            }

            val (x, y) = character.getCoordinateMoveDirection()
            map.move(character, x, y)
        }
    }
}
