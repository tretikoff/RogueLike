package com.bomjRogue.game.strategy

import com.bomjRogue.character.AggressiveNpc
import com.bomjRogue.character.CowardNpc
import com.bomjRogue.character.GameCharacter
import com.bomjRogue.character.RandomNpc
import com.bomjRogue.world.Map
import kotlin.reflect.KClass

class StrategyFactory private constructor() {
    companion object {
        fun getStrategy(type: KClass<*>): MovementStrategy {
            return when (type) {
                AggressiveNpc::class -> getAggressiveStrategy()
                CowardNpc::class -> getCowardlyStrategy()
                RandomNpc::class -> getRandomMoveStrategy()
                else -> throw IllegalArgumentException()
            }
        }

        private fun getRandomMoveStrategy(): MovementStrategy {
            return RandomMovement()
        }

        private fun getAggressiveStrategy(): MovementStrategy {
            return AggressiveMovement()
        }

        private fun getCowardlyStrategy(): MovementStrategy {
            return CowardlyMovement()
        }
    }
}

abstract class MovementStrategy internal constructor() {
    var randomThreshold = 5

    abstract fun makeMove(character: GameCharacter, map: Map)
}
