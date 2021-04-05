package com.bomjRogue

import kotlin.math.max

open class Player(private val name: String, private val characteristics: Characteristics) : GameObject {
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

    fun hit() {

    }

    fun reset() {
        characteristics.reset()
    }
}
