package io.adefarge.kosm.dsl

import io.adefarge.kosm.core.Node

sealed class Ref<T>(protected val factory: OsmFactory<T, *>) {
    abstract fun deref(): T
}

private class BuilderRef<T>(private val index: Int, factory: OsmFactory<T, *>) : Ref<T>(factory) {
    override fun deref(): T = factory.derefAnonymous(index)
}

private class IdRef<T>(private val id: Long, factory: OsmFactory<T, *>) : Ref<T>(factory) {
    override fun deref(): T = factory.deref(id)
}

open class OsmFactory<T, B : BuilderWithTagsAndId<T>>(private val builderSupplier: () -> B) {
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
        val obj = builder.build(id)
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

        val obj = builder.build(id)
        objects[id] = obj
        return obj
    }

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

class NodeFactory<B : BuilderWithTagsAndId<Node>>(builderSupplier: () -> B) : OsmFactory<Node, B>(builderSupplier) {
    override fun fallbackOnMiss(id: Long): Node {
        return NodeBuilder().build(id).also { objects[id] = it }
    }
}
