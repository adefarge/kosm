package io.adefarge.kosm.dsl

import io.adefarge.kosm.core.Node
import io.adefarge.kosm.core.Way

interface WaysBuilderTrait {
    val wayFactory: OsmFactory<Way, WayBuilder>

    fun registerWayRef(ref: Ref<Way>) {}

    fun way(id: Number): Ref<Way> {
        return wayFactory.getRef(id)
            .also { registerWayRef(it) }
    }

    fun way(id: Number? = null, init: WayBuilder.() -> Unit): Ref<Way> {
        return wayFactory.getRef(id, init)
            .also { registerWayRef(it) }
    }
}

class WayBuilder(
    private val nodeFactory: OsmFactory<Node, NodeBuilder>
) : BuilderWithTagsAndId<Way>() {
    private var nodes: List<Ref<Node>> = emptyList()

    fun nodes(vararg ids: Int) {
        nodes = ids.map { nodeFactory.getRef(it) }
    }

    fun nodes(init: InWayNodeBuilder.() -> Unit) {
        nodes = InWayNodeBuilder(nodeFactory).apply(init).build()
    }

    override fun build(id: Long): Way {
        return Way(
            id = id,
            nodes = nodes.map { it.deref() },
            tags = tags
        )
    }
}

class InWayNodeBuilder(
    override val nodeFactory: OsmFactory<Node, NodeBuilder>
) : Builder<List<Ref<Node>>>,
    NodesBuilderTrait {
    private val nodes = mutableListOf<Ref<Node>>()

    override fun registerNodeRef(ref: Ref<Node>) {
        nodes += ref
    }

    override fun build(): List<Ref<Node>> = nodes
}
