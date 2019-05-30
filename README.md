# KOSM

KOSM (read kay-OSM) is a toolkit for manipulating OSM data in kotlin.

[![Build Status](https://travis-ci.org/adefarge/kosm.svg?branch=develop)](https://travis-ci.org/adefarge/kosm)

## Overview
This project provides:
- kosm-core: class definition for an OSM graph
- kosm-graph: to instantiate an OSM graph in a human readable way
- kosm-overpass: to deserialize an OSM graph from an JSON Overpass response

## KOSM DSL
Kotlin DSL to create an OSM graph. An example use case is in tests for
code where an OSM graph is the input.

Examples:

Ids are optional. If a node is not defined, a blank one will be generated.
Node coordinates are also optional.
```kotlin
val graph = osmGraph {
    node {
        id = 1
        tags { "door" to "yes" }
    }
    
    way {
        nodes(0, 1, 2)
        tags {
            "highway" to "pedestrian"
            "level" to "1"
        }
    }
    way {
        id = 2
        nodes(10, 2, 11)
    }
    
    relation {
        node(1)
        way(2)
        way { tags { "tag" to "value" } }
    }
    
    relation {
        role("role1") {
            node(0)
        }
        role("role2") {
            node(1)
        }
    }
}
```

Nodes coordinates can be specified using lat/lon, web mercator projected
x/y, omitted or even randomized.
```kotlin
val graph = osmGraph {
    node {
        lat = 10.0
        lon = -20.0
    }
    node {
        x = -10
        y = 25
    }
    node {
        randomCoordinate()
    }
    node {} // equivalent to node { lat = 0.0; lon = 0.0 }
}
```

See tests for a more complete set of functionality.

The DSL can easily be extended by using extension functions for
`NodesBuilderTrait`, `WaysBuilderTrait` and `RelationsBuilderTrait`.

Example:
```kotlin
fun WaysBuilderTrait.pedestrianWay(init: WayBuilder.() -> Unit) {
    way {
        init()
        tags { "highway" to "pedestrian" }
    }
}

val graph = osmGraph {
    pedestrianWay {
        id = 1
        tags {
            "level" to "1"
        }
    }
}

println(graph.ways.first().tags)
// {level=1, highway=pedestrian}
```

## KOSM Overpass
Usage:
```kotlin
// expect a json file with an "elements" field with the data
val graph1 = OverpassParser.parseJsonFile(file)

// same thing with an input stream instead of a file
val graph2 = OverpassParser.parseJsonInputStream(inputStream)

// expect the json node to contains the nodes, ways and relations at its root
val graph3 = OverpassParser.parseJsonInputStream(inputStream)
```

## Build and test
The project runs with gradle using a wrapper.
```
./gradlew build
```

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
