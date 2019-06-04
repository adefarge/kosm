package io.adefarge.kosm.dsl

import io.adefarge.kosm.core.Way

fun WaysBuilderTrait.pedestrianWay(init: WayBuilder.() -> Unit): Ref<Way> {
    return way {
        init()
        tags { "highway" to "pedestrian" }
    }
}

fun WaysBuilderTrait.pedestrianArea(init: WayBuilder.() -> Unit): Ref<Way> {
    return way {
        init()
        tags {
            "highway" to "pedestrian"
            "area" to "yes"
        }
    }
}
