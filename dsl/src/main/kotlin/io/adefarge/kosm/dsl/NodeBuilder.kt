package io.adefarge.kosm.dsl

import io.adefarge.kosm.core.Node
import java.util.Random
import kotlin.math.PI
import kotlin.math.atan
import kotlin.random.asKotlinRandom

interface NodesBuilderTrait {
    val nodeFactory: OsmFactory<Node>

    fun registerNodeRef(ref: OsmFactory<Node>.Ref) {}

    fun node(id: Number, init: NodeBuilder.() -> Unit): OsmFactory<Node>.Ref {
        return nodeFactory.getRef(
            NodeBuilder().apply {
                init()
                this.id = id
            }
        ).also { registerNodeRef(it) }
    }

    fun node(init: NodeBuilder.() -> Unit): OsmFactory<Node>.Ref {
        return nodeFactory
            .getRef(NodeBuilder().apply(init))
            .also { registerNodeRef(it) }
    }

    fun node(id: Number): OsmFactory<Node>.Ref {
        return nodeFactory
            .getRef(id)
            .also { registerNodeRef(it) }
    }
}

private const val EQUATORIAL_EARTH_RADIUS = 6378100.0 // in meters
private val random = Random().asKotlinRandom()

class NodeBuilder : BuilderWithTagsAndId<Node>() {
    var lat: Double? = null
    var lon: Double? = null

    // X and Y are converted to lat and lon from the Coordinate(lat=0, lon=0)
    // They are in the order of magnitude of a meter but don't base your logic on their real value
    // They can be useful if you want to do a test with grid like coordinates
    var x: Int? = null
    var y: Int? = null

    private fun lat(y: Double): Double {
        return Math.toDegrees(atan(Math.exp(y / EQUATORIAL_EARTH_RADIUS)) * 2 - PI / 2)
    }

    private fun lon(x: Double): Double {
        return Math.toDegrees(x / EQUATORIAL_EARTH_RADIUS)
    }

    fun randomCoordinate(
        latRange: ClosedFloatingPointRange<Double> = -10.0..10.0,
        lonRange: ClosedFloatingPointRange<Double> = -10.0..10.0
    ) {
        lat = random.nextDouble(latRange.start, latRange.endInclusive)
        lon = random.nextDouble(lonRange.start, lonRange.endInclusive)
    }

    override fun build(): Node {
        val lat: Double
        val lon: Double

        if (this.lat != null && this.lon != null) {
            lat = this.lat ?: 0.0
            lon = this.lon ?: 0.0
        } else {
            lat = lat(y?.toDouble() ?: 0.0)
            lon = lon(x?.toDouble() ?: 0.0)
        }

        return Node(
            id = id!!.toLong(),
            lat = lat,
            lon = lon,
            tags = tags
        )
    }
}
