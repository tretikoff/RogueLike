package com.bomjRogue

enum class CharacteristicType {
    Health,
    Force,
    Armor
}
typealias CharacteristicsMap = MutableMap<CharacteristicType, Int>

class Characteristics(private val defaults: CharacteristicsMap) {
    private var characteristics: MutableMap<CharacteristicType, Int> = defaults
    fun reset() {
        characteristics = defaults
    }

    fun updateCharacteristic(type: CharacteristicType, value: Int) {
        characteristics[type] = characteristics[type]!! + value
    }

    fun getCharacteristic(type: CharacteristicType): Int {
        return characteristics[type]!!
    }
}