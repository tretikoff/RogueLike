package com.bomjRogue

enum class ObjectType {
    Npc,
    Player,
    Sword,
    Health,
    ExitDoor,
    Wall,
}

abstract class GameObject(val type: ObjectType) {
    abstract fun update()
}