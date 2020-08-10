package dartzee.screen.ai

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.alexburlton.swingtest.shouldMatchImage
import dartzee.core.util.jsonMapper
import dartzee.helper.AbstractTest
import dartzee.helper.makeDartsModel
import org.junit.Test
import java.awt.Dimension
import java.awt.Point

class TestVisualisationPanelScatter: AbstractTest()
{
    private data class ScatterPreset(val hmPointToCount: Map<Point, Int>)

    @Test
    fun `Should match its snapshot`()
    {
        val jsonString = javaClass.getResource("/scatterPreset.json").readText()
        val module = SimpleModule().also { it.addKeyDeserializer(Point::class.java, JsonPointDeserializer()) }
        val mapper = jsonMapper().also { it.registerModule(module) }

        val scatterPreset = mapper.readValue<ScatterPreset>(jsonString)

        val panel = VisualisationPanelScatter()
        panel.size = Dimension(500, 500)
        panel.populate(scatterPreset.hmPointToCount, makeDartsModel())

        panel.shouldMatchImage("scatter")
    }

    internal inner class JsonPointDeserializer : KeyDeserializer()
    {
        override fun deserializeKey(key: String, ctxt: DeserializationContext): Any?
        {
            val x = key.substringAfter("x=").substringBefore(",").toInt()
            val y = key.substringAfter("y=").substringBefore(")").toInt()

            return Point(x, y)
        }
    }
}