package com.bomjRogue.world.interactive

abstract class Item: GameObject()

class Sword(val damage: Int) : Item()

class ExitDoor: Item()

class Health(val healthPoints: Int) : Item()