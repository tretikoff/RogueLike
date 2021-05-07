package com.bomjRogue.game.strategy

import com.bomjRogue.character.AggressiveNpc
import com.bomjRogue.character.CowardNpc
import com.bomjRogue.character.GameCharacter
import com.bomjRogue.character.RandomNpc
import com.bomjRogue.world.Map
import kotlin.reflect.KClass

enum class StrategyType {
    Passive,
    Aggressive,
    Coward,
}

class StrategyFactory private constructor(private var map: Map? = null) {
    companion object {
        var INSTANCE: StrategyFactory = StrategyFactory()

        fun init(map: Map): StrategyFactory {
            if (INSTANCE.map == null || INSTANCE.map != map) {
                INSTANCE.updateMap(map)
            }
            return INSTANCE
        }
    }

    fun updateMap(map: Map) {
        INSTANCE.map = map
    }

    fun getRandomMoveStrategy(): MovementStrategy {
        return RandomMovement(INSTANCE.map!!)
    }

    fun getStrategy(type: KClass<*>): MovementStrategy {
        return when (type) {
            AggressiveNpc::class -> getAggressiveStrategy()
            CowardNpc::class -> getCowardlyStrategy()
            RandomNpc::class -> getRandomMoveStrategy()
            else -> throw IllegalArgumentException()
        }
    }

    fun getAggressiveStrategy(): MovementStrategy {
        return AggressiveMovement(INSTANCE.map!!)
    }

    fun getCowardlyStrategy(): MovementStrategy {
        return CowardlyMovement(INSTANCE.map!!)
    }
}

//@Serializable
abstract class MovementStrategy internal constructor(protected var map: Map) {
    var randomThreshold = 5

    abstract fun makeMove(character: GameCharacter)
}
