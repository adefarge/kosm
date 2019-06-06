package io.adefarge.kosm.dsl

interface Builder<out T> {
    fun build(): T
}

abstract class BuilderWithTagsAndId<out T> {
    @Deprecated(message = "Specify the id in the dsl function instead")
    var id: Number? = null
    protected var tags: MutableMap<String, String> = mutableMapOf()

    inline fun tags(init: TagsBuilder.() -> Unit) {
        TagsBuilder()
            .apply(init)
            .build()
            .let { merge(it) }
    }

    fun tags(vararg pairs: Pair<String, String>) {
        merge(pairs.toMap())
    }

    fun merge(otherMap: Map<String, String>) {
        otherMap.forEach { (key, value) -> tags[key] = value }
    }

    abstract fun build(id: Long): T
}

class TagsBuilder : Builder<Map<String, String>> {

    private val tags = mutableMapOf<String, String>()

    infix fun Any.to(value: Any) {
        val key = this
        if (key !is String || value !is String) {
            throw IllegalArgumentException("Tags keys and values must be string")
        }

        tags[key] = value
    }

    override fun build(): Map<String, String> = tags
}
