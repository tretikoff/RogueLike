package com.bomjRogue

class Wall : GameObject(ObjectType.ExitDoor) {
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


class Map(val walls: MutableMap<Wall, Position>, private val mapHeight: Float, private val mapWidth: Float) {
    private val closenessFactor = 15f
    var location: MutableMap<GameObject, Position> = HashMap(walls)
        private set

    //    private val walls: List<Wall>
    fun add(obj: GameObject, position: Position) {
        location[obj] = position
    }

    fun addRandomPlace(obj: GameObject, size: Size) {
        val (h, w) = size
        do {
            location.remove(obj)
            val coordinates =
                Coordinates((0..1280 - h.toInt()).random().toFloat(), (0..650 - w.toInt()).random().toFloat())
            add(obj, Position(coordinates, size))
        } while (clashesWithWalls(obj))
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
        val oldPosition = location[obj] ?: return
        val (oldX, oldY) = oldPosition.coordinates
        val newCoordinates = Coordinates(oldX + x, oldY + y)
        val newPosition = Position(newCoordinates, oldPosition.size)
        if (newPosition.valid()) {
            location[obj] = newPosition
        }
        if (clashesWithWalls(obj)) {
            location[obj] = oldPosition
        }
    }

    private fun clashesWithWalls(obj: GameObject): Boolean {
        walls.forEach {
            if (objectsConnect(it.key, obj)) {
                return true
            }
        }
        return false
    }

    fun remove(obj: GameObject) {
        location.remove(obj)
    }

    fun reset() {
        location = HashMap(walls)
    }
}