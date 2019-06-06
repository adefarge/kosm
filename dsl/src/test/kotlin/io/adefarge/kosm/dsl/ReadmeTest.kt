package io.adefarge.kosm.dsl

import io.adefarge.kosm.dsl.extensions.nodesFromGrid
import io.adefarge.kosm.dsl.extensions.pedestrianWay
import org.junit.Test
import kotlin.test.assertEquals

// Ensure examples in Readme at least compile
@Suppress("UNUSED_VARIABLE")
class ReadmeTest {
    @Test
    fun `test 1`() {
        val graph = osmGraph {
            node(1) {
                tags { "door" to "yes" }
            }

            way {
                nodes(0, 1, 2)
                tags {
                    "highway" to "pedestrian"
                    "level" to "1"
                }
            }
            way(2) {
                nodes(10, 2, 11)
            }

            relation {
                node(1)
                way(2)
                way { tags { "tag" to "value" } }
            }

            relation {
                role("role1") {
                    node(0)
                }
                role("role2") {
                    node(1)
                }
            }
        }
    }

    @Test
    fun `node instantiation`() {
        val graph = osmGraph {
            node {
                lat = 10.0
                lon = -20.0
            }
            node {
                x = -10
                y = 25
            }
            node {
                randomCoordinate()
            }
            node {} // equivalent to node { lat = 0.0; lon = 0.0 }
        }
    }

    @Test
    fun `ascii grid`() {
        val graph = osmGraph {
            nodesFromGrid(cellWidth = 5, cellHeight = 10) {
                // Non digit characters are ignored and are just there for legibility
                // (they refer to the ways)
                """
                    #             4
                    #             |
                    #     10 *****3********** 11
                    #     *       |           *
                    #     *       |           *
                    # 0 --1------ 2           *
                    #     *                   *
                    #     13 **************** 12
                    #
                """.trimMargin("#")
            }
            node(1) { tags { "door" to "yes" } }
            node(3) { tags { "door" to "yes" } }

            way {
                nodes(0, 1, 2, 3, 4)
                tags {
                    "highway" to "pedestrian"
                }
            }

            way {
                nodes(10, 11, 12, 13, 10)
                tags {
                    "indoor" to "room"
                }
            }
        }
    }

    @Test
    fun `ways extension functions`() {
        fun WaysBuilderTrait.pedestrianWay(init: WayBuilder.() -> Unit) {
            way {
                init()
                tags { "highway" to "pedestrian" }
            }
        }

        val graph = osmGraph {
            pedestrianWay(1) {
                tags {
                    "level" to "1"
                }
            }
        }

        println(graph.ways.first().tags)
        assertEquals("{level=1, highway=pedestrian}", graph.ways.first().tags.toString())
    }
}
