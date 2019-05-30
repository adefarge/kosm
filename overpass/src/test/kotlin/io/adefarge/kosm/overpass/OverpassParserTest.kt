package io.adefarge.kosm.overpass

import org.junit.Test
import java.io.File

class OverpassParserTest {
    @Test
    fun `it works`() {
        val uri = this.javaClass.getResource("/sample.json").toURI()
        val file = File(uri)
        val graph = OverpassParser.parseJsonFile(file)
    }
}
