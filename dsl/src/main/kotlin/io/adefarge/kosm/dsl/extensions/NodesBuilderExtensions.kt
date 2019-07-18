package io.adefarge.kosm.dsl.extensions

import io.adefarge.kosm.dsl.NodesBuilderTrait
import kotlin.math.ceil

fun NodesBuilderTrait.nodesFromGrid(cellWidth: Int = 5, cellHeight: Int = 10, gridSupplier: () -> String) {
    val grid = gridSupplier.invoke()

    val tokenBuilder = StringBuilder()
    val lines = grid.lines()
    val yAtEnd = lines.size - 1
    for ((yFromEnd, line) in lines.withIndex()) {
        val y = yAtEnd - yFromEnd
        var x = 0
        for (char in line) {
            when {
                char.isDigit() -> tokenBuilder.append(char)
                tokenBuilder.isNotEmpty() -> makeNodeAndReset(tokenBuilder, x, y, cellWidth, cellHeight)
            }
            x++
        }

        if (tokenBuilder.isNotEmpty()) {
            makeNodeAndReset(tokenBuilder, x, y, cellWidth, cellHeight)
        }
    }
}

private fun NodesBuilderTrait.makeNodeAndReset(
    tokenBuilder: StringBuilder,
    endX: Int,
    y: Int,
    cellWidth: Int,
    cellHeight: Int
) {
    val token = tokenBuilder.toString()
    tokenBuilder.clear()

    val meanX = endX - ceil((token.length + 1) / 2.0).toInt()
    val id = token.toInt()

    node(id) {
        this.x = meanX * cellWidth
        this.y = y * cellHeight
    }
}
