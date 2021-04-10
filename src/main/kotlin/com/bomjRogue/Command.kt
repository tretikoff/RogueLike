package com.bomjRogue

import kotlinx.serialization.Serializable

enum class CommandType {
    Move,
    Hit
}

@Serializable
open class Command(val type: CommandType)

@Serializable
class MoveCommand(val playerName: String, val x: Float, val y: Float) : Command(CommandType.Move)

@Serializable
class HitCommand(val playerName: String): Command(CommandType.Hit)