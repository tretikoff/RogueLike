package com.bomjRogue

class Wall: GameObject {
    override fun update() {
        TODO("Not yet implemented")
    }
}

data class Coordinates(val xCoordinate: Float, val yCoordinate: Float)
data class Size(val height: Float, val width: Float)


class Position(val coordinates: Coordinates, val size: Size) {
    fun valid(): Boolean {
        val (x, y) = coordinates
        val (height, width) = size
        return x > 0 && y > 0 && x + width < 1280 && y + height < 650
    }
}


class Map(val walls: MutableMap<Wall, Position>, private val mapHeight: Float, private val mapWidth: Float) : GameObject {
    private val closenessFactor = 15f
    private val location: MutableMap<GameObject, Position> = mutableMapOf()

    //    private val walls: List<Wall>
    fun add(obj: GameObject, position: Position) {
        location[obj] = position
    }

    fun addRandomPlace(obj: GameObject, size: Size) {
        val (h, w) = size
        val coordinates =
            Coordinates((0..1280 - h.toInt()).random().toFloat(), (0..650 - w.toInt()).random().toFloat())
        add(obj, Position(coordinates, size))
    }

    fun getPosition(obj: GameObject): Position {
        return location[obj]!!
    }

    fun objectsConnect(obj1: GameObject, obj2: GameObject): Boolean {
        val (x, y) = getPosition(obj1).coordinates
        val (h, w) = getPosition(obj1).size
        val (x2, y2) = getPosition(obj2).coordinates
        val (h2, w2) = getPosition(obj2).size
        return x < x2 + w2 && x + w > x2 && y < y2 + h2 && y + h > y2;
    }

    fun move(obj: GameObject, x: Float, y: Float) {
        val old = location[obj] ?: return
        val (oldX, oldY) = old.coordinates
        val newCoordinates = Coordinates(oldX + x, oldY + y)
        val newPosition = Position(newCoordinates, old.size)
        if (newPosition.valid()) {
            location[obj] = newPosition
        }
    }

    fun remove(obj: GameObject) {
        location.remove(obj)
    }

    fun reset() {
        location.clear()
    }

    override fun update() {
        TODO("Not yet implemented")
    }
}