package io.adefarge.kosm.dsl

import io.adefarge.kosm.core.Way
import io.adefarge.kosm.dsl.extensions.pedestrianArea as realPedestrianArea
import io.adefarge.kosm.dsl.extensions.pedestrianWay as realPedestrianWay

@Deprecated(message = "Extension functions moved to io.adefarge.kosm.dsl.extensions",
    replaceWith = ReplaceWith("pedestrianWay(id, init)", "io.adefarge.kosm.dsl.extensions.pedestrianWay")
)
fun WaysBuilderTrait.pedestrianWay(id: Number? = null, init: WayBuilder.() -> Unit): Ref<Way> {
    return realPedestrianWay(id, init)
}

@Deprecated(message = "Extension functions moved to io.adefarge.kosm.dsl.extensions",
    replaceWith = ReplaceWith("pedestrianArea(id, init)", "io.adefarge.kosm.dsl.extensions.pedestrianArea")
)
fun WaysBuilderTrait.pedestrianArea(id: Number? = null, init: WayBuilder.() -> Unit): Ref<Way> {
    return realPedestrianArea(id, init)
}
