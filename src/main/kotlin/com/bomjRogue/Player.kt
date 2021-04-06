package com.bomjRogue

import kotlin.math.max
import kotlin.math.min

open class Player(private val name: String, private val characteristics: Characteristics) : GameObject {
    var direction: Direction = Direction.Down

    override fun update() {
//        TODO("Not yet implemented")
    }

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

    fun addHealth(health: Int) {
        characteristics.setCharacteristic(CharacteristicType.Health, min(getHealth() + health, 100))
    }

    fun addForce(force: Int) {
        characteristics.updateCharacteristic(CharacteristicType.Force, force)
    }

    fun hit() {

    }

    fun reset() {
        characteristics.reset()
    }
}
