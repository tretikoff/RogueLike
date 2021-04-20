package com.bomjRogue.game.strategy

import com.bomjRogue.character.GameCharacter
import com.bomjRogue.world.Map

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

    fun getRandomMoveStrategy(): RandomMovement {
        return RandomMovement(INSTANCE.map!!)
    }

    fun getStrategy(type: StrategyType): RandomMovement {
        return when (type) {
            StrategyType.Aggressive -> getAggressiveStrategy()
            StrategyType.Coward -> getCowardlyStrategy()
            StrategyType.Passive -> getRandomMoveStrategy()
        }
    }

    fun getAggressiveStrategy(): AggressiveMovement {
        return AggressiveMovement(INSTANCE.map!!)
    }

    fun getCowardlyStrategy(): CowardlyMovement {
        return CowardlyMovement(INSTANCE.map!!)
    }
}

//@Serializable
abstract class MovementStrategy internal constructor(protected var map: Map) {
    var randomThreshold = 5

    abstract fun makeMove(character: GameCharacter)
}
