package com.bomjRogue

abstract class Item(val name: String): GameObject {
    override fun update() {
        TODO("Not yet implemented")
    }
}

class Weapon(name: String, val damage: Int) : Item(name) {
}

class Health(val healthPoints: Int) : Item("Health")