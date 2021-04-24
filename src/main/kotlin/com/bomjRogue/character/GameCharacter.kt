package com.bomjRogue.character

import com.bomjRogue.game.Direction
import com.bomjRogue.world.interactive.GameObject
import com.bomjRogue.world.interactive.ObjectType
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

/**
 * This was unfortunate: for some I reason there were troubles with passing this data to client and back during pickup/hit events
 */
//@Serializable
//class Player(val myName: String, val myCharacteristics: Characteristics, private val inventoryManager: HeroInventoryManager) : Character(myName, myCharacteristics, ObjectType.Player) {
//
//    fun canPickUp(): Boolean = inventoryManager.isNotFull()
//
//    fun takeItem(item: Item) {
//        inventoryManager.takeItem(item)
//    }
//
//    fun itemDamage() = inventoryManager.getActiveItemDamage()
//
//    fun switchActiveItem() {
//        inventoryManager.switchActiveItem()
//    }
//
//    fun resetInventory() {
//        inventoryManager.dropAll()
//    }
//
//}

class Player(val myName: String, myCharacteristics: Characteristics) : GameCharacter(myName, myCharacteristics, ObjectType.Player)


@Serializable
open class GameCharacter(val name: String, private val characteristics: Characteristics, private val tp: ObjectType) : GameObject(tp) {
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
