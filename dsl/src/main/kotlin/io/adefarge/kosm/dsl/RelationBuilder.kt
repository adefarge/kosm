package io.adefarge.kosm.dsl

import io.adefarge.kosm.core.Node
import io.adefarge.kosm.core.Relation
import io.adefarge.kosm.core.Way

interface RelationsBuilderTrait {
    val nodeFactory: OsmFactory<Node>
    val wayFactory: OsmFactory<Way>
    val relationFactory: OsmFactory<Relation>

    fun registerRelRef(ref: OsmFactory<Relation>.Ref) {}

    fun relation(init: RelationBuilder.() -> Unit): OsmFactory<Relation>.Ref {
        return relationFactory.getRef(
            RelationBuilder(nodeFactory, wayFactory)
                .apply(init)
        ).also { registerRelRef(it) }
    }

    fun rel(init: RelationBuilder.() -> Unit): OsmFactory<Relation>.Ref {
        return relation(init)
    }
}

// TODO impl relations in relation but care about cycles
class RelationBuilder private constructor(
    override val nodeFactory: OsmFactory<Node>,
    override val wayFactory: OsmFactory<Way>,
    private val defaultRole: RoleBuilder
) : BuilderWithTagsAndId<Relation>(), NodesBuilderTrait by defaultRole, WaysBuilderTrait by defaultRole {

    constructor(nodeFactory: OsmFactory<Node>, wayFactory: OsmFactory<Way>)
            : this(nodeFactory, wayFactory, RoleBuilder(nodeFactory, wayFactory))

    private val roles = mutableMapOf<String, RoleBuilder>()
        .apply { set("", defaultRole) }

    fun role(value: String = "", init: RoleBuilder.() -> Unit) {
        roles
            .computeIfAbsent(value) { RoleBuilder(nodeFactory, wayFactory) }
            .apply(init)
    }

    override fun build(): Relation {
        val waysByRole = mutableMapOf<String, List<Way>>()
        val nodesByRole = mutableMapOf<String, List<Node>>()
        for ((role, roleBuilder) in roles) {
            val (nodes, ways, _) = roleBuilder.build()
            if (nodes.isEmpty() && ways.isEmpty()) continue

            nodesByRole[role] = nodes
            waysByRole[role] = ways
        }

        return Relation(
            id = id!!.toLong(),
            nodesByRole = nodesByRole,
            waysByRole = waysByRole,
            relsByRole = emptyMap(),
            tags = tags
        )
    }
}

data class RoleMembers(val nodes: List<Node>, val ways: List<Way>, val rels: List<Relation>)

class RoleBuilder(
    override val nodeFactory: OsmFactory<Node>,
    override val wayFactory: OsmFactory<Way>
) : Builder<RoleMembers>, NodesBuilderTrait,
    WaysBuilderTrait {

    private val nodes = mutableListOf<OsmFactory<Node>.Ref>()
    private val ways = mutableListOf<OsmFactory<Way>.Ref>()

    override fun registerNodeRef(ref: OsmFactory<Node>.Ref) {
        nodes += ref
    }

    override fun registerWayRef(ref: OsmFactory<Way>.Ref) {
        ways += ref
    }

    override fun build(): RoleMembers {
        return RoleMembers(
            nodes = nodes.map { it.deref() },
            ways = ways.map { it.deref() },
            rels = emptyList()
        )
    }
}
