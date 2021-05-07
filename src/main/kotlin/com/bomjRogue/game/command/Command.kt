package com.bomjRogue.game.command

import kotlinx.serialization.Serializable

@Serializable
open class Command

@Serializable
class MoveCommand(val playerName: String, val x: Float, val y: Float) : Command()

@Serializable
class HitCommand(val playerName: String): Command()

@Serializable
class DeathCommand(val playerName: String): Command()