package com.bomjRogue

import kotlin.math.abs

class Map: GameObject {
    private val closenessFactor = 15f
    private val location: MutableMap<GameObject, Coordinates> = mutableMapOf()
    fun add(obj: GameObject, position: Coordinates) {
        location[obj] = position
    }

    fun getPosition(obj: GameObject): Coordinates {
        return location[obj]!!
    }

    fun objectsConnect(obj1: GameObject, obj2: GameObject): Boolean {
        return abs(getPosition(obj1).xCoordinate - getPosition(obj2).xCoordinate) < closenessFactor &&
                abs(getPosition(obj1).yCoordinate - getPosition(obj2).yCoordinate) < closenessFactor
    }

    fun move(obj: GameObject, x: Float, y: Float) {
        val old = location[obj]!!
        location[obj] = Coordinates(old.xCoordinate + x, old.yCoordinate + y)
    }

    fun remove(obj: GameObject) {
        location.remove(obj)
    }

    override fun update() {
        TODO("Not yet implemented")
    }
}