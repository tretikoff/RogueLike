package com.bomjRogue.character

import com.bomjRogue.game.Direction
import com.bomjRogue.world.interactive.GameObject
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

class Player(val myName: String, myCharacteristics: Characteristics) : GameCharacter(myName, myCharacteristics)


@Serializable
open class GameCharacter(val name: String, private val characteristics: Characteristics) : GameObject() {
    var direction: Direction = Direction.Down

    fun takeDamage(damage: Int) {
        characteristics.updateCharacteristic(CharacteristicType.Health, -max(damage - getArmor(), 0))
    }

    private fun getArmor(): Int {
        return characteristics.getCharacteristic(CharacteristicType.Armor)
    }

    fun getForce(): Int {
        return characteristics.getCharacteristic(CharacteristicType.Force)
    }

    fun getHealth(): Int {
        return characteristics.getCharacteristic(CharacteristicType.Health)
    }

    fun isDead(): Boolean = characteristics.getCharacteristic(CharacteristicType.Health) <= 0

    fun addHealth(health: Int) {
        characteristics.setCharacteristic(CharacteristicType.Health, min(getHealth() + health, 100))
    }

    fun addForce(force: Int) {
        characteristics.updateCharacteristic(CharacteristicType.Force, force)
    }

    fun reset() {
        characteristics.reset()
    }

    fun getCoordinateMoveDirection(directionParam: Direction = direction): Pair<Float, Float> {
        val x = when (directionParam) {
            Direction.Left -> -1f
            Direction.Right -> +1f
            else -> 0f
        }
        val y = when (directionParam) {
            Direction.Down -> +1f
            Direction.Up -> -1f
            else -> 0f
        }
        return Pair(x, y)
    }

}
