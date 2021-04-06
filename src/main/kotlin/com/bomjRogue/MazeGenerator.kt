package com.bomjRogue

class MazeGenerator(private val x: Int, private val y: Int) {
    private val maze: Array<IntArray> = Array(x) { IntArray(y) }
    fun display() {
        for (i in 0 until y) {
            // draw the north edge
            for (j in 0 until x) {
                print(if (maze[j][i] and 1 == 0) "+---" else "+   ")
            }
            println("+")
            // draw the west edge
            for (j in 0 until x) {
                print(if (maze[j][i] and 8 == 0) "|   " else "    ")
            }
            println("|")
        }
        // draw the bottom line
        for (j in 0 until x) {
            print("+---")
        }
        println("+")
    }

    private fun generateMaze(cx: Int, cy: Int) {
        val dirs = DIR.values()
        mutableListOf(*dirs).shuffle()
        for (dir in dirs) {
            val nx = cx + dir.dx
            val ny = cy + dir.dy
            if (between(nx, x) && between(ny, y)
                && maze[nx][ny] == 0
            ) {
                maze[cx][cy] = maze[cx][cy] or dir.bit
                maze[nx][ny] = maze[nx][ny] or dir.opposite!!.bit
                generateMaze(nx, ny)
            }
        }
    }

    private enum class DIR(val bit: Int, val dx: Int, val dy: Int) {
        N(1, 0, -1), S(2, 0, 1), E(4, 1, 0), W(8, -1, 0);

        var opposite: DIR? = null

        companion object {
            init {
                N.opposite = S
                S.opposite = N
                E.opposite = W
                W.opposite = E
            }
        }
    }

    companion object {
        private fun between(v: Int, upper: Int): Boolean {
            return v in 0 until upper
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val x = if (args.isNotEmpty()) args[0].toInt() else 8
            val y = if (args.size == 2) args[1].toInt() else 8
            val maze = MazeGenerator(x, y)
            maze.display()
        }
    }

    init {
        generateMaze(0, 0)
    }
}