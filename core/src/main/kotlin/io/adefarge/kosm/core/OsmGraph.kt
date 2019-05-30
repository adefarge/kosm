package io.adefarge.kosm.core

data class OsmGraph(
    val nodes: List<Node> = emptyList(),
    val ways: List<Way> = emptyList(),
    val relations: List<Relation> = emptyList()
)
