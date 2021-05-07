package com.bomjRogue.world.interactive

abstract class Item: GameObject()

abstract class Weapon(val damage: Int): Item()
class Sword(damage: Int) : Weapon(damage)

class ExitDoor: Item()

abstract class Potion(val healthPoints: Int): Item()
class Health(healthPoints: Int) : Potion(healthPoints)