package io.adefarge.kosm.dsl

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class NodeTest {
    @Test
    fun `can instantiate a node`() {
        val node = node {}

        assertEquals(0.0, node.lat)
        assertEquals(0.0, node.lon)
        assertTrue(node.tags.isEmpty())
    }

    @Test
    fun `can instantiate a node in graph`() {
        val graph = osmGraph {
            node {}
        }

        assertEquals(1, graph.nodes.size)
        val node = graph.nodes.first()
        assertEquals(0.0, node.lat)
        assertEquals(0.0, node.lon)
        assertTrue(node.tags.isEmpty())
    }

    @Test
    fun `can instantiate a node with id`() {
        val graph = osmGraph {
            node { id = 1 }
        }

        val node = graph.nodes.first()
        assertEquals(1, node.id)
    }

    @Test
    fun `can instantiate a node with id using another way`() {
        val graph = osmGraph {
            node(1) {}
        }

        val node = graph.nodes.first()
        assertEquals(1, node.id)
    }

    @Test
    fun `can instantiate a node with tags`() {
        val graph = osmGraph {
            node {
                tags {
                    "door" to "yes"
                    "level" to "1"
                }
            }
        }

        val node = graph.nodes.first()
        assertEquals(2, node.tags.size)
        assertEquals("yes", node.tags["door"])
        assertEquals("1", node.tags["level"])
    }

    @Test
    fun `can instantiate a node with lat lon`() {
        val graph = osmGraph {
            node { lat = 10.0; lon = -15.0 }
        }

        val node = graph.nodes.first()
        assertEquals(10.0, node.lat)
        assertEquals(-15.0, node.lon)
    }

    @Test
    fun `can instantiate a node with projected x y`() {
        val graph = osmGraph {
            node { x = 10; y = -15 }
        }

        val node = graph.nodes.first()
        assertTrue(node.lat < 0 && node.lat > -1)
        assertTrue(node.lon > 0 && node.lon < 1)
    }

    @Test
    fun `can instantiate a node with random coordinates`() {
        val graph = osmGraph {
            node { randomCoordinate() }
        }

        val node = graph.nodes.first()
        assertTrue(node.lat != 0.0)
        assertTrue(node.lon != 0.0)
    }

    @Test
    fun `can instantiate multiple nodes`() {
        val graph = osmGraph {
            node { tags { "defined" to "0" } }
            node { tags { "defined" to "1" } }
            node(1) { tags { "defined" to "2" } }
            node(0) { tags { "defined" to "3" } }
        }

        assertEquals(4, graph.nodes.size)
        val nodeDefined0 = graph.nodes.first { it.tags["defined"] == "0" }
        val nodeDefined1 = graph.nodes.first { it.tags["defined"] == "1" }
        val nodeDefined2 = graph.nodes.first { it.tags["defined"] == "2" }
        val nodeDefined3 = graph.nodes.first { it.tags["defined"] == "3" }

        assertTrue(nodeDefined0.id !in setOf(0L, 1L))
        assertTrue(nodeDefined1.id !in setOf(0L, 1L))
        assertNotEquals(nodeDefined0.id, nodeDefined1.id)
        assertEquals(1, nodeDefined2.id)
        assertEquals(0, nodeDefined3.id)
    }
}
