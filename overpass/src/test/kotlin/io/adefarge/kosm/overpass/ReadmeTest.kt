package io.adefarge.kosm.overpass

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test
import java.io.File

// Ensure examples in Readme at least compile
@Suppress("UNUSED_VARIABLE")
class ReadmeTest {
    @Test
    fun `use parser in multiple ways`() {
        val file = File(object {}.javaClass.getResource("/sample.json").toURI())
        val inputStream = file.inputStream()
        val jsonNode = ObjectMapper().createArrayNode()

        // expect a json file with an "elements" field with the data
        val graphFromFile = OverpassParser.parseJsonFile(file)
        val graphFromFile2 = osmGraph(file)
        val graphFromFileInResource = osmGraph("sample.json")

        // same thing with an input stream instead of a file
        val graph2 = OverpassParser.parseJsonInputStream(inputStream)

        // expect the json node to contains the nodes, ways and relations at its root
        val graph3 = OverpassParser.parseJsonNode(jsonNode)
    }
}
