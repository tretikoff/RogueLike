package com.bomjRogue.character

import kotlinx.serialization.Serializable

enum class CharacteristicType {
    Health,
    Force,
    Armor,
}
typealias CharacteristicsMap = MutableMap<CharacteristicType, Int>

@Serializable
class Characteristics(private val defaults: CharacteristicsMap) {
    private var characteristics: MutableMap<CharacteristicType, Int> = HashMap(defaults)
    fun reset() {
        characteristics = HashMap(defaults)
    }

    fun updateCharacteristic(type: CharacteristicType, value: Int) {
        characteristics[type] = characteristics[type]!! + value
    }

    fun setCharacteristic(type: CharacteristicType, value: Int) {
        characteristics[type] = value
    }

    fun getCharacteristic(type: CharacteristicType): Int {
        return characteristics[type]!!
    }
}