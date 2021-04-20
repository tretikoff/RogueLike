package com.bomjRogue.world.interactive

import kotlinx.serialization.Serializable

enum class ObjectType {
    Npc,
    AggressiveNpc,
    CowardNpc,
    Player,
    Sword,
    Health,
    ExitDoor,
    Wall,
}
@Serializable
open class GameObject(val type: ObjectType)