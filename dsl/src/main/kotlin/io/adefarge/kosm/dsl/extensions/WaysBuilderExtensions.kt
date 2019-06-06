package io.adefarge.kosm.dsl.extensions

import io.adefarge.kosm.core.Way
import io.adefarge.kosm.dsl.Ref
import io.adefarge.kosm.dsl.WayBuilder
import io.adefarge.kosm.dsl.WaysBuilderTrait
import io.adefarge.kosm.dsl.way

fun WaysBuilderTrait.pedestrianWay(id: Number? = null, init: WayBuilder.() -> Unit): Ref<Way> {
    return way(id) {
        init()
        tags { "highway" to "pedestrian" }
    }
}

fun WaysBuilderTrait.pedestrianArea(id: Number? = null, init: WayBuilder.() -> Unit): Ref<Way> {
    return way(id) {
        init()
        tags {
            "highway" to "pedestrian"
            "area" to "yes"
        }
    }
}
