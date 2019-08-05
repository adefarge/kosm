package io.adefarge.kosm.dsl

import io.adefarge.kosm.core.Node
import io.adefarge.kosm.core.Relation
import io.adefarge.kosm.core.Way

sealed class Ref<T>(protected val factory: OsmFactory<T, *>) {
    abstract fun deref(): T
}

private class BuilderRef<T>(private val index: Int, factory: OsmFactory<T, *>) : Ref<T>(factory) {
    override fun deref(): T = factory.derefAnonymous(index)
}

private class IdRef<T>(private val id: Long, factory: OsmFactory<T, *>) : Ref<T>(factory) {
    override fun deref(): T = factory.deref(id)
}

abstract class OsmFactory<T, B : BuilderWithTagsAndId<T>> internal constructor() {
    internal constructor(builderSupplier: () -> B) : this() {
        this.builderSupplier = builderSupplier
    }

    protected lateinit var builderSupplier: () -> B
    protected val objects = mutableMapOf<Long, T>()
    private val buildersById = mutableMapOf<Long, B>()
    private val anonymousBuilders = mutableListOf<B>()
    private val anonymousIdCorrespondenceTable = mutableMapOf<Int, Long>()

    private fun generateId(): Long {
        var id = 0L
        while (true) {
            if (id !in objects.keys && id !in buildersById.keys) {
                return id
            }
            id++
        }
    }

    fun derefAnonymous(index: Int): T {
        val osmId = anonymousIdCorrespondenceTable[index]
            ?: return generateAnonymous(index)

        return objects[osmId]!!
    }

    private fun generateAnonymous(index: Int): T {
        val builder = anonymousBuilders.getOrNull(index)
            ?: throw IllegalStateException("No anonymous builder with such index $index")

        val id = generateId()
        val obj = build(builder, id, isAnonymous = true)

        objects[id] = obj
        anonymousIdCorrespondenceTable[index] = id
        return obj
    }

    fun deref(id: Long): T {
        return objects[id]
            ?: generate(id)
            ?: fallbackOnMiss(id)
    }

    private fun generate(id: Long): T? {
        val builder = buildersById[id] ?: return null

        val obj = build(builder, id, isAnonymous = false)

        objects[id] = obj
        return obj
    }

    protected open fun build(builder: B, id: Long, isAnonymous: Boolean): T = builder.build(id)

    protected open fun fallbackOnMiss(id: Long): T {
        throw IllegalStateException("No object with id $id")
    }

    @Suppress("DEPRECATION")
    fun getRef(id: Number?, init: B.() -> Unit): Ref<T> {
        val idAsLong = when (id) {
            null -> {
                val builder = builderSupplier.invoke().apply(init)
                val builderId = builder.id
                if (builderId == null) {
                    anonymousBuilders += builder
                    return BuilderRef(anonymousBuilders.size - 1, this)
                }

                builderId.toLong()
            }
            else -> id.toLong()
        }

        val builder = buildersById.getOrPut(idAsLong, builderSupplier)
        builder.apply(init)

        return IdRef(idAsLong, this)
    }

    fun getRef(id: Number): Ref<T> {
        return IdRef(id.toLong(), this)
    }

    fun ensureAllIsGenerated() {
        for (index in anonymousBuilders.indices) {
            if (index !in anonymousIdCorrespondenceTable) {
                generateAnonymous(index)
            }
        }

        for (id in buildersById.keys) {
            if (id !in objects) {
                generate(id)
            }
        }
    }

    fun getAll(): List<T> {
        return objects.values.toList()
    }
}

class NodeFactory : OsmFactory<Node, NodeBuilder>({ NodeBuilder() }) {
    override fun fallbackOnMiss(id: Long): Node {
        return NodeBuilder().build(id).also { objects[id] = it }
    }
}

class WayFactory(nodeFactory: NodeFactory) : OsmFactory<Way, WayBuilder>({ WayBuilder(nodeFactory) })

class RelationFactory(
    nodeFactory: NodeFactory,
    wayFactory: WayFactory
) : OsmFactory<Relation, RelationBuilder>() {
    init {
        builderSupplier = { RelationBuilder(nodeFactory, wayFactory, this) }
    }

    private val relationsBeingBuilt = mutableSetOf<RelationBuilder>()

    override fun build(builder: RelationBuilder, id: Long, isAnonymous: Boolean): Relation {
        if (builder in relationsBeingBuilt) {
            val message = if (!isAnonymous) {
                "A circular dependency was detected while building the relation $id"
            } else {
                "A circular dependency was detected while building an anonymous relation"
            }
            throw IllegalStateException(message)
        }

        relationsBeingBuilt += builder
        val result = super.build(builder, id, isAnonymous)
        relationsBeingBuilt -= builder

        return result
    }
}
