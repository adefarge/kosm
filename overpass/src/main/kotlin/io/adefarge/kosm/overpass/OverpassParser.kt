package io.adefarge.kosm.overpass

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.treeToValue
import io.adefarge.kosm.core.MutableRelation
import io.adefarge.kosm.core.OsmGraph
import io.adefarge.kosm.core.Way
import java.io.File
import java.io.InputStream

fun osmGraph(jsonResource: String): OsmGraph {
    val uri = object {}.javaClass.getResource("/$jsonResource").toURI()
    val file = File(uri)
    return osmGraph(file)
}

fun osmGraph(json: File): OsmGraph {
    return OverpassParser.parseJsonFile(json)
}

object OverpassParser {
    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    fun parseJsonFile(file: File): OsmGraph {
        val tree = mapper.readTree(file).at("/elements")
        return parse(tree)
    }

    fun parseJsonInputStream(ins: InputStream): OsmGraph {
        val tree = mapper.readTree(ins).at("/elements")
        return parse(tree)
    }

    fun parseJsonNode(node: JsonNode) = parse(node)

    private fun Node.toCoreModel(): io.adefarge.kosm.core.Node {
        return io.adefarge.kosm.core.Node(
            id = id,
            lat = lat,
            lon = lon,
            tags = tags
        )
    }

    private fun parse(tree: JsonNode): OsmGraph {
        val elements = mapper.treeToValue<Array<OsmElement>>(tree).asList()

        val nodes = elements
            .asSequence()
            .filterIsInstance<Node>()
            .map { it.toCoreModel() }
            .toList()
        val nodesById = nodes.associateBy { it.id }

        val ways = elements
            .asSequence()
            .filterIsInstance<WayWithId>()
            .map { way ->
                Way(
                    id = way.id,
                    tags = way.tags,
                    nodes = way.nodes.map(nodesById::getValue)
                )
            }
            .toList()
        val waysById = ways.associateBy { it.id }

        val relations = elements
            .asSequence()
            .filterIsInstance<RelationWithId>()
            .filter { it.members.isNotEmpty() }
            .map { relation ->
                MutableRelation(
                    id = relation.id,
                    tags = relation.tags,
                    waysByRole = relation.members
                        .filter { it.type == "way" }
                        .groupBy { it.role }
                        .map { (role, list) ->
                            role to list.map { waysById.getValue(it.ref) }
                        }
                        .toMap(),
                    nodesByRole = relation.members
                        .filter { it.type == "node" }
                        .groupBy { it.role }
                        .map { (role, list) ->
                            role to list.map { nodesById.getValue(it.ref) }
                        }
                        .toMap()
                )
            }
            .toList()
        val relsById = relations.associateBy { it.id }

        elements.asSequence()
            .filterIsInstance<RelationWithId>()
            .forEach { relation ->
                val mutableRel = relsById.getValue(relation.id)

                mutableRel.relationsByRole = relation.members
                    .filter { it.type == "relation" }
                    .groupBy { it.role }
                    .map { (role, list) ->
                        role to list.map { relsById.getValue(it.ref) }
                    }
                    .toMap()
            }

        return OsmGraph(nodes, ways, relations)
    }
}
