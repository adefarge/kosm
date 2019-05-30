package io.adefarge.kosm.dsl

import io.adefarge.kosm.core.Node
import io.adefarge.kosm.core.Way

interface WaysBuilderTrait {
    val nodeFactory: OsmFactory<Node>
    val wayFactory: OsmFactory<Way>

    fun registerWayRef(ref: OsmFactory<Way>.Ref) {}

    fun way(id: Number): OsmFactory<Way>.Ref {
        return wayFactory.getRef(id)
            .also { registerWayRef(it) }
    }

    fun way(init: WayBuilder.() -> Unit): OsmFactory<Way>.Ref {
        return wayFactory
            .getRef(WayBuilder(nodeFactory).apply(init))
            .also { registerWayRef(it) }
    }
}

class WayBuilder(
    private val nodeFactory: OsmFactory<Node>
) : BuilderWithTagsAndId<Way>() {
    private var nodes: List<OsmFactory<Node>.Ref> = emptyList()

    fun nodes(vararg ids: Int) {
        nodes = ids.map { nodeFactory.getRef(it) }
    }

    fun nodes(init: InWayNodeBuilder.() -> Unit) {
        nodes = InWayNodeBuilder(nodeFactory).apply(init).build()
    }

    override fun build(): Way {
        return Way(
            id = id!!.toLong(),
            nodes = nodes.map { it.deref() },
            tags = tags
        )
    }
}

class InWayNodeBuilder(
    override val nodeFactory: OsmFactory<Node>
) : Builder<List<OsmFactory<Node>.Ref>>,
    NodesBuilderTrait {
    private val nodes = mutableListOf<OsmFactory<Node>.Ref>()

    override fun registerNodeRef(ref: OsmFactory<Node>.Ref) {
        nodes += ref
    }

    override fun build(): List<OsmFactory<Node>.Ref> = nodes
}
