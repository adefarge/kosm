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
    override fun equals(other: Any?) = other != null && other is Node && other.id == id
    override fun hashCode() = id.hashCode()

    override val ref: OsmRef = OsmRef(OsmRef.Type.NODE, id = id)
}

sealed class WayOrRelation : OsmElement(), WithOsmTags

data class Way(
    val id: Long,
    val nodes: List<Node>,
    override val tags: Map<String, String>
) : WayOrRelation() {
    override fun equals(other: Any?) = other != null && other is Way && other.id == id
    override fun hashCode() = id.hashCode()

    override val ref: OsmRef = OsmRef(OsmRef.Type.WAY, id = id)
}

open class Relation(
    val id: Long,
    val waysByRole: Map<String, List<Way>>,
    val nodesByRole: Map<String, List<Node>>,
    override val tags: Map<String, String>
) : WayOrRelation() {
    override fun equals(other: Any?) = other != null && other is Relation && other.id == id
    override fun hashCode() = id.hashCode()

    open val relationsByRole: Map<String, List<Relation>> get() = emptyMap()

    override val ref: OsmRef = OsmRef(OsmRef.Type.RELATION, id = id)
}

class MutableRelation(
    id: Long,
    waysByRole: Map<String, List<Way>>,
    nodesByRole: Map<String, List<Node>>,
    tags: Map<String, String>
) : Relation(id, waysByRole, nodesByRole, tags) {

    override lateinit var relationsByRole: Map<String, List<Relation>>
}
