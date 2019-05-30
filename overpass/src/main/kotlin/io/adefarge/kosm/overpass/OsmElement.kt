package io.adefarge.kosm.overpass

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(Node::class, name = "node"),
    JsonSubTypes.Type(WayWithId::class, name = "way"),
    JsonSubTypes.Type(RelationWithId::class, name = "relation")
)
internal abstract class OsmElement(val type: String) {
    abstract val id: Long
    abstract val tags: Map<String, String>
}

internal data class Node(
    override val id: Long,
    val lat: Double,
    val lon: Double,
    override val tags: Map<String, String> = mapOf()
) : OsmElement("node")

internal data class WayWithId(
    override val id: Long,
    val nodes: List<Long>,
    override val tags: Map<String, String> = mapOf()
) : OsmElement("way")

internal data class RelationWithId(
    override val id: Long,
    val members: List<RelationMember>,
    override val tags: Map<String, String> = mapOf()
) : OsmElement("relation")

internal data class RelationMember(
    val type: String,
    val ref: Long,
    val role: String
)
