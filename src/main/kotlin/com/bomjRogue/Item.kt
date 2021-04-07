package com.bomjRogue


abstract class Item(type: ObjectType): GameObject(type) {
    override fun update() {
        TODO("Not yet implemented")
    }
}

class Sword(val damage: Int) : Item(ObjectType.Sword)

class ExitDoor: Item(ObjectType.ExitDoor)

class Health(val healthPoints: Int) : Item(ObjectType.Health)