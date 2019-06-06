package io.adefarge.kosm.core

fun OsmGraph.node(id: Number): Node {
    return nodes.firstOrNull { it.id == id.toLong() }
        ?: throw NoSuchElementException("No node with id $id in graph")
}

fun OsmGraph.nodeOrNull(id: Number): Node? {
    return nodes.firstOrNull { it.id == id.toLong() }
}

fun OsmGraph.way(id: Number): Way {
    return ways.firstOrNull { it.id == id.toLong() }
        ?: throw NoSuchElementException("No way with id $id in graph")
}

fun OsmGraph.wayOrNull(id: Number): Way? {
    return ways.firstOrNull { it.id == id.toLong() }
}

fun OsmGraph.relation(id: Number): Relation {
    return relations.firstOrNull { it.id == id.toLong() }
        ?: throw NoSuchElementException("No relation with id $id in graph")
}

fun OsmGraph.relationOrNull(id: Number): Relation? {
    return relations.firstOrNull { it.id == id.toLong() }
}
