package com.bomjRogue

class Npc(name: String,characteristics: Characteristics) : Player(name, characteristics) {
    var direction: Direction = Direction.Down
}