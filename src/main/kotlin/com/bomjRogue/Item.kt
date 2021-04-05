package com.bomjRogue

abstract class Item(val name: String) {

}

class Weapon(name: String, val damage: Int) : Item(name)