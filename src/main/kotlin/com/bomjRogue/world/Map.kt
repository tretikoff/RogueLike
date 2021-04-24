package com.bomjRogue.world

import com.bomjRogue.character.GameCharacter
import com.bomjRogue.game.Direction
import com.bomjRogue.world.interactive.GameObject
import com.bomjRogue.world.interactive.ObjectType
import javafx.scene.shape.Line
import kotlinx.serialization.Serializable
import java.awt.Rectangle
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

typealias GameItems = MutableMap<GameObject, Position>

class Wall : GameObject(ObjectType.Wall)

@Serializable
data class Coordinates(val xCoordinate: Float, val yCoordinate: Float)

@Serializable
data class Size(val height: Float, val width: Float)


@Serializable
data class Position(val coordinates: Coordinates, val size: Size)


class Map(private val walls: MutableMap<Wall, Position>, mapHeight: Float, val mapWidth: Float) {

    companion object PredefinedCoords {
        val playerSpawn = Position(Coordinates(20f, 20f), Size(34f, 19f))
        val doorSpawn = Position(Coordinates(1220f, 10f), Size(46f, 32f))


        const val liesUpper = 2
        const val liesLower = 8
        const val liesLeft = 1
        const val liesRight = 4
    }

    private val reachableHeight = mapHeight - 70

    var location: GameItems = HashMap(walls)
        private set

    fun add(obj: GameObject, position: Position) {
        location[obj] = position
    }

    fun addRandomPlace(obj: GameObject, size: Size) {
        val (h, w) = size
        do {
            location.remove(obj)
            val rx = ((Random.nextFloat() * (mapWidth - w)))
            val ry = ((Random.nextFloat() * (reachableHeight - h)))
            val coordinates = Coordinates(rx, ry)
            add(obj, Position(coordinates, size))
        } while (clashesWithWalls(obj))
    }

    private fun getPlainDistance(lhs: Position, rhs: Position): Float {
        val (x, y) = lhs.coordinates
        val (x2, y2) = rhs.coordinates

        return sqrt((x - x2 )*(x - x2) + (y - y2 )*(y - y2))
    }

    private fun getDistance(lhs: GameObject, rhs: GameObject): Float {
        val (x, y) = getPosition(lhs).coordinates
        val (h, w) = getPosition(lhs).size
        val (x2, y2) = getPosition(rhs).coordinates
        val (h2, w2) = getPosition(rhs).size

        return sqrt((x - x2 + w + w2)*(x - x2 + w + w2) + (y - y2 + h + h2)*(y - y2 + h + h2))
    }

    fun getMiddlePos(lhs:Position, rhs:Position): Position {
        // invariant: sizes are same
        val newX = (lhs.coordinates.xCoordinate + rhs.coordinates.xCoordinate) / 2
        val newY = (lhs.coordinates.yCoordinate + rhs.coordinates.yCoordinate) / 2
        return Position(Coordinates(newX, newY), lhs.size)
    }

    private fun getRelativeDirection(rectangle: Rectangle, x: Double, y:Double): Int {
        var out = 0
        when {
            rectangle.width <= 0 -> {
                out = out or (liesLeft or liesRight)
            }
            x < rectangle.x -> {
                out = out or liesLeft
            }
            x > rectangle.x + rectangle.width -> {
                out = out or liesRight
            }
        }
        when {
            rectangle.height <= 0 -> {
                out = out or (liesUpper or liesLower)
            }
            y < rectangle.y -> {
                out = out or liesUpper
            }
            y > rectangle.y + rectangle.height -> {
                out = out or liesLower
            }
        }
        return out
    }

    private fun lineRectangleIntersect(line: Line, rectangle: Rectangle): Boolean {
        var relativeFromStart: Int
        var relativeFromEnd: Int
        if (getRelativeDirection(rectangle, line.endX, line.endY).also { relativeFromEnd = it } == 0) {
            return true
        }
        while (getRelativeDirection(rectangle, line.startX, line.startY).also { relativeFromStart = it } != 0) {
            if (relativeFromStart and relativeFromEnd != 0) {
                return false
            }
            if (relativeFromStart and (liesLeft or liesRight) != 0) {
                var xNew = rectangle.x.toDouble()
                if (relativeFromStart and liesRight != 0) {
                    xNew += rectangle.width
                }
                line.startY = line.startY + (xNew - line.startX) * (line.endY - line.startY) / (line.endX - line.startX)
                line.startX = xNew

            } else {
                var yNew = rectangle.y.toDouble()
                if (relativeFromStart and liesLower != 0) {
                    yNew += rectangle.height
                }
                line.startX = line.startX + (yNew - line.startY) * (line.endX - line.startX) / (line.endY - line.startY)
                line.startY = yNew
            }
        }
        return true
    }

    private fun lineIntersectsWithAnyWall(line: Line, wallsParam: MutableMap<Wall, Position> = walls): Boolean {
        for (entry in wallsParam) {
            val wallPos = entry.value
            val rect = Rectangle(wallPos.coordinates.xCoordinate.toInt(), wallPos.coordinates.yCoordinate.toInt(),
                wallPos.size.width.toInt(), wallPos.size.height.toInt() )
            if (lineRectangleIntersect(line, rect)) {
                return true
            }
        }
        return false
    }

    fun isIntersectWithWalls(obj: GameCharacter, moveDirection: Direction): Boolean {
        val (objCoord, objSize) = getPosition(obj)
        val (shift_x, shift_y) = obj.getCoordinateMoveDirection(moveDirection)
        val newCoordinates = Coordinates(objCoord.xCoordinate + shift_x,
                        objCoord.yCoordinate + shift_y)
        val newPosition = Position(newCoordinates, objSize)

        val resultLine = Line(objCoord.xCoordinate.toDouble(),objCoord.yCoordinate.toDouble(),
            newCoordinates.xCoordinate.toDouble(), newCoordinates.yCoordinate.toDouble()
        )

        val middlePos = getMiddlePos(Position(objCoord, objSize), newPosition)
        val intersectDist = getPlainDistance(middlePos, newPosition)
        val closeWalls = walls.filter { isClose(obj, it.key, intersectDist.toDouble()) }

        return lineIntersectsWithAnyWall(resultLine, closeWalls.toMutableMap()) // maybe bad transforming
    }

    fun isClose(lhs: GameObject, rhs: GameObject, threshold: Double): Boolean {
        return getDistance(lhs, rhs) <= threshold * 2
    }

    fun getDirection(x: Float, y: Float): Direction {
        if (x == 0f && y == 0f) {
            return Direction.None
        }

        val absX = abs(x).toInt()

        if (y > absX) {
            return Direction.Down
        }

        val absY = abs(y).toInt()

        if (absY > absX) {
            return Direction.Up
        }

        if (x > 0) {
            return if (-y == x) {
                Direction.Down
            } else Direction.Right
        }

        if (y < 0 && y == x) return Direction.Down

        return if (y == x) {
            Direction.Down
        } else Direction.Left
    }


    fun objectsConnect(obj1: GameObject, obj2: GameObject): Boolean {
        val (x, y) = getPosition(obj1).coordinates
        val (h, w) = getPosition(obj1).size
        val (x2, y2) = getPosition(obj2).coordinates
        val (h2, w2) = getPosition(obj2).size
        return x < x2 + w2 && x + w > x2 && y < y2 + h2 && y + h > y2
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

    fun remove(obj: GameObject) {
        location.remove(obj)
    }

    fun reset() {
        location = HashMap(walls)
    }

    private fun Position.valid(): Boolean {
        val (x, y) = coordinates
        val (height, width) = size
        return x > 0 && y > 0 && x + width < mapWidth && y + height < reachableHeight
    }

    private fun clashesWithWalls(obj: GameObject): Boolean {
        walls.forEach {
            if (objectsConnect(it.key, obj)) {
                return true
            }
        }
        return false
    }

    private fun getPosition(obj: GameObject): Position {
        return location[obj]!!
    }
}