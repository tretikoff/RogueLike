package com.bomjRogue.game.strategy

import com.bomjRogue.character.Character
import com.bomjRogue.character.Npc
import com.bomjRogue.character.Player
import com.bomjRogue.config.SettingsManager
import com.bomjRogue.game.Direction
import com.bomjRogue.world.Map
import kotlin.random.Random

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

    fun getStrategy(strategyClass: Class<out Strategy> = SettingsManager.defaultStrategy.strategy.java): Strategy {
        return if (strategyClass == RandomMovement::class.java) {
            getRandomMoveStrategy()
        } else {
            getHuntStrategy()
        }
    }

    fun getRandomMoveStrategy(): RandomMovement {
        return RandomMovement(INSTANCE.map!!)
    }

    fun getHuntStrategy(): HuntMovement {
        return HuntMovement(INSTANCE.map!!)
    }
}

//@Serializable
abstract class Strategy internal constructor(protected var map: Map) {

    var randomThreshold = 5

    abstract fun makeMove(character: Character)
}

open class RandomMovement(map: Map) : Strategy(map = map) {
    override fun makeMove(character: Character) {
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


class HuntMovement(map: Map) : RandomMovement(map = map) {
    private var detectRadius = 55.0
        set(value) {
            if (value > map.mapWidth || value <= 0) {
                return
            }
            field = value
        }

    override fun makeMove(character: Character) {
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
        val (toXCoord, toYCoord) = resolvedPlayer.value.coordinates
        val (fromXCoord, fromYCoord) = map.location[character]!!.coordinates
        val moveDirection = map.getDirection(toXCoord - fromXCoord, toYCoord - fromYCoord)

        character.direction = moveDirection
        val (x, y) = character.getCoordinateMoveDirection()
        map.move(character, x, y)
    }

}




