package io.adefarge.kosm.dsl.extensions

import io.adefarge.kosm.core.Node
import io.adefarge.kosm.core.node
import io.adefarge.kosm.core.way
import io.adefarge.kosm.dsl.osmGraph
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodesBuilderExtensionsTest {

    @Test
    fun `nodesFromGrid empty`() {
        val graph = osmGraph {
            nodesFromGrid { "              " }
            nodesFromGrid { "" }
        }

        assertEquals(0, graph.nodes.size)
    }

    @Test
    fun `nodesFromGrid one node`() {
        val graph = osmGraph {
            nodesFromGrid { "1" }
        }

        assertEquals(1, graph.nodes.size)
        assertEquals(1, graph.nodes.first().id)
    }

    @Test
    fun `nodesFromGrid one node with multi digit id`() {
        val graph = osmGraph {
            nodesFromGrid { "11" }
        }

        assertEquals(1, graph.nodes.size)
        assertEquals(11, graph.nodes.first().id)
    }

    @Test
    fun `nodesFromGrid one node with non zero coordinate`() {
        val graph = osmGraph {
            nodesFromGrid(cellWidth = 10, cellHeight = 10) {
                """
                |
                |         3
                |
                |
                """.trimMargin()
            }
        }

        assertEquals(1, graph.nodes.size)
        assertEquals(3, graph.nodes.first().id)
        assertCoordinateFromGrid(90, 10, graph.node(3))
    }

    @Test
    fun `nodesFromGrid when using a multi digit node id, the middle digit is used for coordinate`() {
        val graph = osmGraph {
            nodesFromGrid(cellWidth = 10) { "  123" }
        }

        assertCoordinateFromGrid(30, 0, graph.node(123))
    }

    @Test
    fun `nodesFromGrid when using a multi digit node id, if there is a tie for the middle digit, the left one is used`() {
        val graph = osmGraph {
            nodesFromGrid(cellWidth = 10) { "  1234" }
        }

        assertCoordinateFromGrid(30, 0, graph.node(1234))
    }

    @Test
    fun `can add tag to nodes defined with nodesFromGrid`() {
        val graph = osmGraph {
            nodesFromGrid { "1" }
            node(1) {
                tags { "a" to "b" }
            }
        }

        assertEquals(1, graph.nodes.size)
        val node = graph.nodes.first()
        assertEquals(1, node.id)
        assertEquals(mapOf("a" to "b"), node.tags)
    }

    @Test
    fun `nodesFromGrid simple case`() {
        val graph = osmGraph {
            nodesFromGrid { "1  2  4  0" }
        }

        assertEquals(4, graph.nodes.size)
        assertEquals(setOf(0, 1, 2, 4), graph.nodes.map { it.id.toInt() }.toSet())

        assertTrue(graph.node(1).lon < graph.node(2).lon)
        assertTrue(graph.node(2).lon < graph.node(4).lon)
        assertTrue(graph.node(4).lon < graph.node(0).lon)
        assertEquals(graph.node(1).lat, graph.node(2).lat)
        assertEquals(graph.node(2).lat, graph.node(4).lat)
        assertEquals(graph.node(4).lat, graph.node(0).lat)
    }

    @Test
    fun `non alpha numerics should be ignored`() {
        val graph = osmGraph {
            nodesFromGrid { "1--2__4**0&é(-è_çà)'5afbnfd;6" }
        }

        assertEquals(6, graph.nodes.size)
        assertEquals(setOf(0, 1, 2, 4, 5, 6), graph.nodes.map { it.id.toInt() }.toSet())

        assertTrue(graph.node(1).lon < graph.node(2).lon)
        assertTrue(graph.node(2).lon < graph.node(4).lon)
        assertTrue(graph.node(4).lon < graph.node(0).lon)
        assertTrue(graph.node(0).lon < graph.node(5).lon)
        assertTrue(graph.node(5).lon < graph.node(6).lon)
        assertEquals(graph.node(1).lat, graph.node(2).lat)
        assertEquals(graph.node(2).lat, graph.node(4).lat)
        assertEquals(graph.node(4).lat, graph.node(0).lat)
        assertEquals(graph.node(0).lat, graph.node(5).lat)
        assertEquals(graph.node(5).lat, graph.node(6).lat)
    }

    @Test
    fun `nodesFromGrid multiple node with multi digit id`() {
        val graph = osmGraph {
            nodesFromGrid { "11 22-(405 =02" }
        }

        assertEquals(4, graph.nodes.size)
        assertEquals(setOf(11, 22, 405, 2), graph.nodes.map { it.id.toInt() }.toSet())
    }

    @Test
    fun `nodesFromGrid multiline`() {
        val graph = osmGraph {
            nodesFromGrid(cellWidth = 10, cellHeight = 10) {
                """
                |1  2  4 0
                |    5
                | 6      9
                """.trimMargin()
            }
        }

        assertEquals(7, graph.nodes.size)
        assertEquals(setOf(0, 1, 2, 4, 5, 6, 9), graph.nodes.map { it.id.toInt() }.toSet())
        assertCoordinateFromGrid(0, 0, graph.node(1))
        assertCoordinateFromGrid(30, 0, graph.node(2))
        assertCoordinateFromGrid(60, 0, graph.node(4))
        assertCoordinateFromGrid(80, 0, graph.node(0))
        assertCoordinateFromGrid(40, 10, graph.node(5))
        assertCoordinateFromGrid(10, 20, graph.node(6))
        assertCoordinateFromGrid(80, 20, graph.node(9))
    }

    @Test
    fun `nodesFromGrid can generate the layout for multiple areas`() {
        val graph = osmGraph {
            // Special characters are ignored and are just here for legibility
            nodesFromGrid(cellWidth = 10, cellHeight = 10) {
                """
                    #   1 --------- 2
                    #   |           |
                    #   |           |
                    #   0 --------- 3
                    #   | *       * |
                    #   |   *   *   |
                    #   |     4     |
                    #   5 --------- 6
                    #
                """.trimMargin("#")
            }

            pedestrianArea(0) {
                nodes(0, 1, 2, 3, 0)
            }

            pedestrianArea(1) {
                nodes(0, 4, 3, 6, 5, 0)
            }
        }

        assertEquals(7, graph.nodes.size)
        assertEquals(2, graph.ways.size)
        assertEquals(listOf(0, 1, 2, 3, 0), graph.way(0).nodes.map { it.id.toInt() })
        assertEquals(listOf(0, 4, 3, 6, 5, 0), graph.way(1).nodes.map { it.id.toInt() })

        assertCoordinateFromGrid(30, 0, graph.node(1))
        assertCoordinateFromGrid(150, 0, graph.node(2))
        assertCoordinateFromGrid(30, 30, graph.node(0))
        assertCoordinateFromGrid(150, 30, graph.node(3))
        assertCoordinateFromGrid(90, 60, graph.node(4))
        assertCoordinateFromGrid(30, 70, graph.node(5))
        assertCoordinateFromGrid(150, 70, graph.node(6))
    }

    private fun assertCoordinateFromGrid(x: Int, y: Int, node: Node) {
        val expectedNode = io.adefarge.kosm.dsl.node { this.x = x; this.y = y }
        assertEquals(expectedNode.lat, node.lat, "The node lat is different from expected")
        assertEquals(expectedNode.lon, node.lon, "The node lon is different from expected")
    }
}
