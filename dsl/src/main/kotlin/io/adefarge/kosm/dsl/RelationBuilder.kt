package io.adefarge.kosm.dsl

import io.adefarge.kosm.core.MutableRelation
import io.adefarge.kosm.core.Node
import io.adefarge.kosm.core.Relation
import io.adefarge.kosm.core.Way

interface RelationsBuilderTrait {
    val nodeFactory: NodeFactory
    val wayFactory: WayFactory
    val relationFactory: RelationFactory

    fun registerRelationRef(ref: Ref<Relation>) {}

    fun relation(id: Number? = null, init: RelationBuilder.() -> Unit): Ref<Relation> =
        relationFactory.getRef(id, init).also { registerRelationRef(it) }

    fun relation(id: Number): Ref<Relation> = relationFactory.getRef(id).also { registerRelationRef(it) }
}

class RelationBuilder private constructor(
    override val nodeFactory: NodeFactory,
    override val wayFactory: WayFactory,
    override val relationFactory: RelationFactory,
    private val defaultRole: RoleBuilder
) : BuilderWithTagsAndId<Relation>(),
    NodesBuilderTrait by defaultRole,
    WaysBuilderTrait by defaultRole,
    RelationsBuilderTrait by defaultRole {

    constructor(
        nodeFactory: NodeFactory,
        wayFactory: WayFactory,
        relationFactory: RelationFactory
    ) : this(nodeFactory, wayFactory, relationFactory, RoleBuilder(nodeFactory, wayFactory, relationFactory))

    private val roles = mutableMapOf<String, RoleBuilder>()
        .apply { set("", defaultRole) }

    fun role(value: String = "", init: RoleBuilder.() -> Unit) {
        roles.computeIfAbsent(value) { RoleBuilder(nodeFactory, wayFactory, relationFactory) }
            .apply(init)
    }

    override fun build(id: Long): Relation {
        val waysByRole = mutableMapOf<String, List<Way>>()
        val nodesByRole = mutableMapOf<String, List<Node>>()
        val relationsByRole = mutableMapOf<String, List<Relation>>()
        for ((role, roleBuilder) in roles) {
            val (nodes, ways, relations) = roleBuilder.build()
            if (nodes.isEmpty() && ways.isEmpty() && relations.isEmpty()) continue

            nodesByRole[role] = nodes
            waysByRole[role] = ways
            relationsByRole[role] = relations
        }

        val relation = MutableRelation(
            id = id,
            nodesByRole = nodesByRole,
            waysByRole = waysByRole,
            tags = tags
        )
        relation.relationsByRole = relationsByRole
        return relation
    }
}

data class RoleMembers(val nodes: List<Node>, val ways: List<Way>, val rels: List<Relation>)

class RoleBuilder(
    override val nodeFactory: NodeFactory,
    override val wayFactory: WayFactory,
    override val relationFactory: RelationFactory
) : Builder<RoleMembers>, NodesBuilderTrait, WaysBuilderTrait, RelationsBuilderTrait {

    private val nodes = mutableListOf<Ref<Node>>()
    private val ways = mutableListOf<Ref<Way>>()
    private val rels = mutableListOf<Ref<Relation>>()

    override fun registerNodeRef(ref: Ref<Node>) {
        nodes += ref
    }

    override fun registerWayRef(ref: Ref<Way>) {
        ways += ref
    }

    override fun registerRelationRef(ref: Ref<Relation>) {
        rels += ref
    }

    override fun build(): RoleMembers {
        return RoleMembers(
            nodes = nodes.map { it.deref() },
            ways = ways.map { it.deref() },
            rels = rels.map { it.deref() }
        )
    }
}
