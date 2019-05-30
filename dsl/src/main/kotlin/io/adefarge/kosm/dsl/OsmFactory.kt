package io.adefarge.kosm.dsl

import io.adefarge.kosm.core.Node

open class OsmFactory<T> {
    protected val objects = mutableMapOf<Long, T>()
    private val waitingForId = mutableListOf<BuilderWithTagsAndId<T>>()

    private fun generateId(): Long {
        var id = 0L
        while (true) {
            if (id !in objects.keys) {
                return id
            }
            id++
        }
    }

    abstract inner class Ref {
        abstract fun deref(): T
    }

    private inner class BuilderRef(private val index: Int) : Ref() {
        override fun deref() = waitingForId.getOrNull(index)
            ?.let { generate(it) }
            ?: throw IllegalStateException("No builder with such index $index")
    }

    private inner class IdRef(private val value: Long) : Ref() {
        override fun deref() = this@OsmFactory.deref(value)
    }

    private fun generate(builder: BuilderWithTagsAndId<T>): T {
        if (builder.id == null) {
            builder.id = generateId()
        }

        return builder.build().also {
            objects[builder.id!!.toLong()] = it
        }
    }

    protected open fun deref(id: Long): T {
        return objects[id]
            ?: throw IllegalStateException("No object with id $id")
    }

    fun getRef(builder: BuilderWithTagsAndId<T>): Ref {
        val id = builder.id?.toLong()
        if (id != null) {
            objects.put(id, builder.build())
                ?.let { throw IllegalStateException("Multiple object with id $id") }
            return IdRef(id)
        }

        val ref = BuilderRef(waitingForId.size)
        waitingForId += builder
        return ref
    }

    fun getRef(id: Number): Ref {
        return IdRef(id.toLong())
    }

    fun generateAll() {
        waitingForId.forEach { generate(it) }
    }

    fun getAll(): List<T> {
        return objects.values.toList()
    }
}

class NodeFactory : OsmFactory<Node>() {
    override fun deref(id: Long): Node {
        return objects[id]
            ?: NodeBuilder()
                .apply { this.id = id }
                .build()
                .also { objects[id] = it }
    }
}
