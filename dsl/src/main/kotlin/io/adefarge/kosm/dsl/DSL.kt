package io.adefarge.kosm.dsl

import io.adefarge.kosm.core.Node
import io.adefarge.kosm.core.OsmGraph
import io.adefarge.kosm.core.Relation
import io.adefarge.kosm.core.Way

inline fun osmGraph(init: OsmGraphBuilder.() -> Unit): OsmGraph {
    return OsmGraphBuilder()
        .apply(init)
        .build()
}

fun node(init: NodeBuilder.() -> Unit): Node {
    return NodeBuilder().apply {
        id = 0
        init()
    }.build()
}

fun way(init: WayBuilder.() -> Unit): Way {
    val nodeFactory = NodeFactory()
    return WayBuilder(nodeFactory)
        .apply {
            id = 0
            init()
        }
        .build()
}

fun relation(init: RelationBuilder.() -> Unit): Relation {
    val nodeFactory = NodeFactory()
    val wayFactory = OsmFactory<Way>()
    return RelationBuilder(nodeFactory, wayFactory)
        .apply {
            id = 0
            init()
        }
        .build()
}

class OsmGraphBuilder : Builder<OsmGraph>, WaysBuilderTrait,
    NodesBuilderTrait, RelationsBuilderTrait {
    override val nodeFactory = NodeFactory()
    override val wayFactory = OsmFactory<Way>()
    override val relationFactory = OsmFactory<Relation>()

    override fun build(): OsmGraph {
        nodeFactory.generateAll()
        wayFactory.generateAll()
        relationFactory.generateAll()

        return OsmGraph(
            nodes = nodeFactory.getAll(),
            ways = wayFactory.getAll(),
            relations = relationFactory.getAll()
        )
    }
}
