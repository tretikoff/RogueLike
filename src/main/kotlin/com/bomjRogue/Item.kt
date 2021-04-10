package com.bomjRogue

abstract class Item(type: ObjectType): GameObject(type)

class Sword(val damage: Int) : Item(ObjectType.Sword)

class ExitDoor: Item(ObjectType.ExitDoor)

class Health(val healthPoints: Int) : Item(ObjectType.Health)