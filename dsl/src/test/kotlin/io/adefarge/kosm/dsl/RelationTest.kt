package io.adefarge.kosm.dsl

import io.adefarge.kosm.core.relation
import io.adefarge.kosm.core.relationOrNull
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RelationTest {
    @Test
    fun `can instantiate a relation`() {
        val relation = relation { }

        assertTrue(relation.nodesByRole.isEmpty())
        assertTrue(relation.waysByRole.isEmpty())
        assertTrue(relation.relationsByRole.isEmpty())
        assertTrue(relation.tags.isEmpty())
    }

    @Test
    fun `can instantiate a relation in a graph`() {
        val graph = osmGraph { relation { } }

        assertEquals(1, graph.relations.size)
        val relation = graph.relations.first()

        assertTrue(relation.nodesByRole.isEmpty())
        assertTrue(relation.waysByRole.isEmpty())
        assertTrue(relation.relationsByRole.isEmpty())
        assertTrue(relation.tags.isEmpty())
    }

    @Test
    fun `can instantiate a relation with id`() {
        val graph = osmGraph { relation(1) {} }
        val relation = graph.relations.first()
        assertEquals(1, relation.id)
    }

    @Test
    @Suppress("DEPRECATION")
    fun `can still instantiate a relation with id using the id in the Builder`() {
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
                way(1) { }
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

    @Test
    fun `can merge multiple declaration of relations`() {
        val graph = osmGraph {
            relation(0) {
                tags {
                    "a" to "b"
                }
                node(0)
                node(2)
            }

            relation(0) {
                tags { "c" to "d" }
                node(3)
            }
        }

        assertEquals(1, graph.relations.size)
        val relation = graph.relations.first()

        assertEquals(0, relation.id)
        assertEquals(listOf(0, 2, 3), relation.nodesByRole.getValue("").map { it.id.toInt() })
        assertEquals(mapOf("a" to "b", "c" to "d"), relation.tags)
    }

    @Test
    fun `can declare multiple relations without overwriting them`() {
        val graph = osmGraph {
            relation { tags { "decl" to "0" } }
            relation(1) { tags { "decl" to "1" } }
            relation { tags { "decl" to "2" } }
        }

        assertEquals(3, graph.relations.size)
        assertEquals(1, graph.relations.count { it.id == 1L })
        val relation1 = graph.relation(1)
        assertEquals(mapOf("decl" to "1"), relation1.tags)

        val anonymousRelations = graph.relations.filter { it.id != 1L }
        assertEquals(2, anonymousRelations.size)
        val anon1 = anonymousRelations.first()
        assertEquals(1, anon1.tags.size)
        val anon2 = anonymousRelations.last()
        assertEquals(1, anon2.tags.size)
        assertNotEquals(anon1.tags, anon2.tags)
    }

    @Test
    fun `can declare relation in a relation`() {
        val graph = osmGraph {
            relation(0) { tags { "id" to "0" } }
            relation(1) {
                relation(0)
                tags { "id" to "1" }
            }
        }

        assertEquals(2, graph.relations.size)
        val relation0 = graph.relationOrNull(0)
        assertNotNull(relation0)
        assertEquals("0", relation0.tags["id"])
        val relation1 = graph.relationOrNull(1)
        assertNotNull(relation1)
        assertEquals("1", relation1.tags["id"])

        val relationsInDefaultRole = relation1.relationsByRole[""]
        assertNotNull(relationsInDefaultRole)
        assertEquals(1, relationsInDefaultRole.size)
        assertEquals(relation0, relationsInDefaultRole.first())
    }

    @Test(expected = IllegalStateException::class)
    fun `cannot declare a relation with circular dependency`() {
        osmGraph {
            relation(0) {
                relation(1)
                tags { "id" to "0" }
            }
            relation(1) {
                relation(0)
                tags { "id" to "1" }
            }
        }
    }
}
