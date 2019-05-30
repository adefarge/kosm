package io.adefarge.kosm.core

data class OsmRef(val type: Type, val id: Long) {
    enum class Type {
        NODE, WAY, RELATION;

        val displayValue = name.toLowerCase().capitalize()
    }

    override fun toString(): String {
        return "${type.displayValue}($id)"
    }
}
