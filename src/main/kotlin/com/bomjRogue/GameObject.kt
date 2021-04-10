package com.bomjRogue

import kotlinx.serialization.Serializable

enum class ObjectType {
    Npc,
    Player,
    Sword,
    Health,
    ExitDoor,
    Wall,
}
@Serializable
open class GameObject(val type: ObjectType)