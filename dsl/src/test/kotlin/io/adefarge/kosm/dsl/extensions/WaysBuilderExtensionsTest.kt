package io.adefarge.kosm.dsl.extensions

import io.adefarge.kosm.dsl.osmGraph
import org.junit.Test
import kotlin.test.assertEquals

class WaysBuilderExtensionsTest {
    @Test
    fun `can build a pedestrian way`() {
        val graph = osmGraph {
            pedestrianWay {
                tags { "a" to "b" }
            }
        }

        assertEquals(1, graph.ways.size)
        val way = graph.ways.first()
        val expectedTags = mapOf(
            "highway" to "pedestrian",
            "a" to "b"
        )

        assertEquals(expectedTags, way.tags)
    }

    @Test
    fun `can build a pedestrian area`() {
        val graph = osmGraph {
            pedestrianArea {
                tags { "a" to "b" }
            }
        }

        assertEquals(1, graph.ways.size)
        val way = graph.ways.first()
        val expectedTags = mapOf(
            "highway" to "pedestrian",
            "area" to "yes",
            "a" to "b"
        )

        assertEquals(expectedTags, way.tags)
    }
}
