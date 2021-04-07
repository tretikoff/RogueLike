package com.bomjRogue

class MazeGenerator(private val x: Int, private val y: Int) {
    private val maze = Array(x) { IntArray(y) }

    private fun generate(cx: Int, cy: Int) {
        Direction.values().shuffle().forEach {
            val nx = cx + it.dx
            val ny = cy + it.dy
            if (between(nx, x) && between(ny, y) && maze[nx][ny] == 0) {
                maze[cx][cy] = maze[cx][cy] or it.bit
                maze[nx][ny] = maze[nx][ny] or it.opposite!!.bit
                generate(nx, ny)
            }
        }
    }

    fun getMaze(): MutableMap<Wall, Position> {
        generate(0, 0)
        val result = mutableMapOf<Wall, Position>()
        for (i in 0 until y) {
            for (j in 0 until x) {
                if (maze[j][i] and 1 == 0) {
                    result[Wall()] = Position(Coordinates(100f * j, 100f * i), Size(2f, 100f))
                }
            }
            for (j in 0 until x) {
                if (maze[j][i] and 8 == 0) {
                    result[Wall()] = Position(Coordinates(100f * j, 100f * i), Size(100f, 2f))
                }
            }
        }

        return result
    }

    private inline fun <reified T> Array<T>.shuffle(): Array<T> {
        val list = toMutableList()
        list.shuffle()
        return list.toTypedArray()
    }

    private enum class Direction(val bit: Int, val dx: Int, val dy: Int) {
        N(1, 0, -1), S(2, 0, 1), E(4, 1, 0), W(8, -1, 0);

        var opposite: Direction? = null

        companion object {
            init {
                N.opposite = S
                S.opposite = N
                E.opposite = W
                W.opposite = E
            }
        }
    }

    private fun between(v: Int, upper: Int) = v in 0 until upper
}
