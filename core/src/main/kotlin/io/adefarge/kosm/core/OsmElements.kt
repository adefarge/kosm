package io.adefarge.kosm.core

interface WithOsmTags {
    val tags: Map<String, String>
}

sealed class OsmElement : WithOsmTags {
    abstract val ref: OsmRef
}

data class Node(
    val id: Long,
    val lat: Double,
    val lon: Double,
    override val tags: Map<String, String>
) : OsmElement() {
    override fun equals(other: Any?): Boolean = other != null && other is Node && other.id == id
    override fun hashCode(): Int = id.hashCode()

    override val ref: OsmRef =
        OsmRef(OsmRef.Type.NODE, id = id)
}

sealed class WayOrRelation : OsmElement(), WithOsmTags
typealias OsmArea = WayOrRelation

data class Way(
    val id: Long,
    val nodes: List<Node>,
    override val tags: Map<String, String>
) : WayOrRelation() {
    override val ref: OsmRef =
        OsmRef(OsmRef.Type.WAY, id = id)
}

class Relation(
    val id: Long,
    val waysByRole: Map<String, List<Way>>,
    val nodesByRole: Map<String, List<Node>>,
    var relsByRole: Map<String, List<Relation>> = emptyMap(),
    override val tags: Map<String, String>
) : WayOrRelation() {

    override val ref: OsmRef =
        OsmRef(OsmRef.Type.RELATION, id = id)
}
