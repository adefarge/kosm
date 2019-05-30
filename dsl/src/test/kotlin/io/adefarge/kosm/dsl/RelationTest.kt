package io.adefarge.kosm.dsl

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RelationTest {
    @Test
    fun `can instantiate a relation`() {
        val relation = relation { }

        assertTrue(relation.nodesByRole.isEmpty())
        assertTrue(relation.waysByRole.isEmpty())
        assertTrue(relation.relsByRole.isEmpty())
        assertTrue(relation.tags.isEmpty())
    }

    @Test
    fun `can instantiate a relation in a graph`() {
        val graph = osmGraph { relation { } }

        assertEquals(1, graph.relations.size)
        val relation = graph.relations.first()

        assertTrue(relation.nodesByRole.isEmpty())
        assertTrue(relation.waysByRole.isEmpty())
        assertTrue(relation.relsByRole.isEmpty())
        assertTrue(relation.tags.isEmpty())
    }

    @Test
    fun `can instantiate a relation with id`() {
        val graph = osmGraph { relation { id = 1 } }

        val relation = graph.relations.first()
        assertEquals(1, relation.id)
    }

    @Test
    fun `can instantiate a relation with tags`() {
        val graph = osmGraph {
            relation {
                tags {
                    "bla" to "true"
                    "also bla" to "also true"
                }
            }
        }

        val relation = graph.relations.first()
        assertEquals(2, relation.tags.size)
        assertEquals("true", relation.tags["bla"])
        assertEquals("also true", relation.tags["also bla"])
    }

    @Test
    fun `can instantiate a relation with ways and nodes`() {
        val graph = osmGraph {
            relation {
                way { id = 1 }
                way { nodes(0, 1) }
                node(0)
                node(1) { randomCoordinate() }
            }
        }

        val relation = graph.relations.first()

        assertEquals(2, graph.nodes.size)
        assertEquals(1, relation.nodesByRole.size)
        assertNotNull(relation.nodesByRole[""])
        assertEquals(2, relation.nodesByRole.getValue("").size)

        assertEquals(2, graph.ways.size)
        assertEquals(1, relation.waysByRole.size)
        assertNotNull(relation.waysByRole[""])
        assertEquals(2, relation.waysByRole.getValue("").size)
        assertEquals(2, relation.waysByRole.getValue("").firstOrNull { it.nodes.isNotEmpty() }?.nodes?.size)
    }

    @Test
    fun `can add role to relation members`() {
        val graph = osmGraph {
            relation {
                role("123") {
                    node(0)
                }
                role("abcd") {
                    node(1)
                }
                node(2)
            }
        }

        val relation = graph.relations.first()
        assertEquals(3, relation.nodesByRole.size)
        assertEquals(1, relation.nodesByRole["123"]?.size)
        assertEquals(1, relation.nodesByRole["abcd"]?.size)
        assertEquals(1, relation.nodesByRole[""]?.size)
    }

    @Test
    fun `defining elements at root of relation, in default role or in a role with empty name is equivalent`() {
        val graph = osmGraph {
            relation {
                way { }
                role {
                    way { }
                }
                role("") {
                    way { }
                }
            }
        }

        val relation = graph.relations.first()
        assertEquals(1, relation.waysByRole.size)
        assertEquals(3, relation.waysByRole[""]?.size)
    }
}
