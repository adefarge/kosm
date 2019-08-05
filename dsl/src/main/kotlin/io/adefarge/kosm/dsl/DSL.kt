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
    return NodeBuilder().apply(init).build(0)
}

fun way(init: WayBuilder.() -> Unit): Way {
    val nodeFactory = NodeFactory()

    return WayBuilder(nodeFactory)
        .apply(init)
        .build(0)
}

fun relation(init: RelationBuilder.() -> Unit): Relation {
    val nodeFactory = NodeFactory()
    val wayFactory = WayFactory(nodeFactory)
    val relationFactory = RelationFactory(nodeFactory, wayFactory)

    return RelationBuilder(nodeFactory, wayFactory, relationFactory)
        .apply(init)
        .build(0)
}

class OsmGraphBuilder : Builder<OsmGraph>, WaysBuilderTrait, NodesBuilderTrait, RelationsBuilderTrait {
    override val nodeFactory = NodeFactory()
    override val wayFactory = WayFactory(nodeFactory)
    override val relationFactory = RelationFactory(nodeFactory, wayFactory)

    override fun build(): OsmGraph {
        nodeFactory.ensureAllIsGenerated()
        wayFactory.ensureAllIsGenerated()
        relationFactory.ensureAllIsGenerated()

        return OsmGraph(
            nodes = nodeFactory.getAll(),
            ways = wayFactory.getAll(),
            relations = relationFactory.getAll()
        )
    }
}
