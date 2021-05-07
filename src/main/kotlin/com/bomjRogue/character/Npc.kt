package com.bomjRogue.character

abstract class Npc(name: String, characteristics: Characteristics) : GameCharacter(name, characteristics)

class RandomNpc(name: String, characteristics: Characteristics) : Npc(name, characteristics)
class CowardNpc(name: String, characteristics: Characteristics) : Npc(name, characteristics)
class AggressiveNpc(name: String, characteristics: Characteristics) : Npc(name, characteristics)