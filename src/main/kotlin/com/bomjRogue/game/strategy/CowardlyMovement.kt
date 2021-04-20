package com.bomjRogue.game.strategy

import com.bomjRogue.character.GameCharacter
import com.bomjRogue.game.Direction
import com.bomjRogue.world.Map
import com.bomjRogue.world.Position
import com.bomjRogue.world.interactive.GameObject

class CowardlyMovement(map: Map) : AbstractMovement(map = map) {
    override fun getDirection(character: GameCharacter, resolvedPlayer: kotlin.collections.Map.Entry<GameObject, Position>): Direction {
        val (toXCoord, toYCoord) = resolvedPlayer.value.coordinates
        val (fromXCoord, fromYCoord) = map.location[character]!!.coordinates
        return map.getDirection(toXCoord + fromXCoord, toYCoord + fromYCoord)
    }
}