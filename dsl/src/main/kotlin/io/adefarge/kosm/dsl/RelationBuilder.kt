package io.adefarge.kosm.dsl

import io.adefarge.kosm.core.Node
import io.adefarge.kosm.core.Relation
import io.adefarge.kosm.core.Way

interface RelationsBuilderTrait {
    val nodeFactory: OsmFactory<Node, NodeBuilder>
    val wayFactory: OsmFactory<Way, WayBuilder>
    val relationFactory: OsmFactory<Relation, RelationBuilder>

    fun registerRelationRef(ref: Ref<Relation>) {}

    fun relation(id: Number? = null, init: RelationBuilder.() -> Unit): Ref<Relation> {
        return relationFactory.getRef(id, init)
            .also { registerRelationRef(it) }
    }

    fun rel(id: Number? = null, init: RelationBuilder.() -> Unit): Ref<Relation> {
        return relation(id, init)
    }
}

// TODO impl relations in relation but care about cycles
class RelationBuilder private constructor(
    override val nodeFactory: OsmFactory<Node, NodeBuilder>,
    override val wayFactory: OsmFactory<Way, WayBuilder>,
    private val defaultRole: RoleBuilder
) : BuilderWithTagsAndId<Relation>(), NodesBuilderTrait by defaultRole, WaysBuilderTrait by defaultRole {

    constructor(nodeFactory: OsmFactory<Node, NodeBuilder>, wayFactory: OsmFactory<Way, WayBuilder>) :
            this(nodeFactory, wayFactory, RoleBuilder(nodeFactory, wayFactory))

    private val roles = mutableMapOf<String, RoleBuilder>()
        .apply { set("", defaultRole) }

    fun role(value: String = "", init: RoleBuilder.() -> Unit) {
        roles
            .computeIfAbsent(value) { RoleBuilder(nodeFactory, wayFactory) }
            .apply(init)
    }

    override fun build(id: Long): Relation {
        val waysByRole = mutableMapOf<String, List<Way>>()
        val nodesByRole = mutableMapOf<String, List<Node>>()
        for ((role, roleBuilder) in roles) {
            val (nodes, ways, _) = roleBuilder.build()
            if (nodes.isEmpty() && ways.isEmpty()) continue

            nodesByRole[role] = nodes
            waysByRole[role] = ways
        }

        return Relation(
            id = id,
            nodesByRole = nodesByRole,
            waysByRole = waysByRole,
            relationsByRole = emptyMap(),
            tags = tags
        )
    }
}

data class RoleMembers(val nodes: List<Node>, val ways: List<Way>, val rels: List<Relation>)

class RoleBuilder(
    override val nodeFactory: OsmFactory<Node, NodeBuilder>,
    override val wayFactory: OsmFactory<Way, WayBuilder>
) : Builder<RoleMembers>, NodesBuilderTrait,
    WaysBuilderTrait {

    private val nodes = mutableListOf<Ref<Node>>()
    private val ways = mutableListOf<Ref<Way>>()

    override fun registerNodeRef(ref: Ref<Node>) {
        nodes += ref
    }

    override fun registerWayRef(ref: Ref<Way>) {
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
