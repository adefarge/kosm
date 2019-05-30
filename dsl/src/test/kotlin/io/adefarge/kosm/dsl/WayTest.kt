package io.adefarge.kosm.dsl

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WayTest {
    @Test
    fun `can instantiate a way`() {
        val way = way {}

        assertTrue(way.nodes.isEmpty())
        assertTrue(way.tags.isEmpty())
    }

    @Test
    fun `can instantiate a way in a graph`() {
        val graph = osmGraph { way {} }

        assertEquals(1, graph.ways.size)
        val way = graph.ways.first()

        assertTrue(way.nodes.isEmpty())
        assertTrue(way.tags.isEmpty())
    }

    @Test
    fun `can instantiate a way with id`() {
        val graph = osmGraph { way { id = 1 } }

        val way = graph.ways.first()

        assertEquals(1, way.id)
    }

    @Test
    fun `can instantiate a way with tags`() {
        val graph = osmGraph {
            way {
                tags {
                    "highway" to "pedestrian"
                    "level" to "1"
                }
            }
        }

        val way = graph.ways.first()
        assertEquals(2, way.tags.size)
        assertEquals("pedestrian", way.tags["highway"])
        assertEquals("1", way.tags["level"])
    }

    @Test
    fun `can instantiate a way with nodes`() {
        val graph = osmGraph {
            way {
                nodes {
                    node { randomCoordinate() }
                    node { randomCoordinate() }
                    node { randomCoordinate() }
                }
            }
        }

        val way = graph.ways.first()
        assertEquals(3, way.nodes.size)
        assertEquals(3, graph.nodes.size)
    }

    @Test
    fun `can instantiate nodes and link it in a way by id`() {
        val graph = osmGraph {
            node(0) { }
            node(1) { }
            node(2) { }

            way { nodes(0, 1, 2) }
        }

        val way = graph.ways.first()
        assertEquals(listOf(0L, 1L, 2L), way.nodes.map { it.id })
        assertEquals(3, graph.nodes.size)
    }

    @Test
    fun `can mix instantiation of node inside and outside of way`() {
        val graph = osmGraph {
            node(0) { tags { "node" to "0" } }
            node(1) { tags { "node" to "1" } }

            way {
                nodes {
                    node(0)
                    node { tags { "node" to "2" } }
                    node(1)
                }
            }
        }

        val way = graph.ways.first()
        assertEquals(3, way.nodes.size)
        assertEquals(0, way.nodes.first().id)
        assertEquals(1, way.nodes.last().id)
        assertEquals(3, graph.nodes.size)

        assertEquals("0", graph.nodes.first { it.id == 0L }.tags["node"])
        assertEquals("1", graph.nodes.first { it.id == 1L }.tags["node"])
    }
}
