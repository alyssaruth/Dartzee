package dartzee.screen.ai

import com.github.alexburlton.swingtest.shouldMatchImage
import dartzee.helper.AbstractTest
import dartzee.helper.makeDartsModel
import org.junit.jupiter.api.Test
import java.awt.Dimension

class TestVisualisationPanelDensity: AbstractTest()
{
    @Test
    fun `Should match snapshot - T20`()
    {
        val model = makeDartsModel(scoringDart = 20, standardDeviation = 50.0, maxRadius = 250)

        val panel = VisualisationPanelDensity()
        panel.size = Dimension(500, 500)
        panel.populate(mapOf(), model)

        panel.shouldMatchImage("T20")
    }

    @Test
    fun `Should match snapshot - bullseye`()
    {
        val model = makeDartsModel(scoringDart = 25, standardDeviation = 100.0, maxRadius = 500)

        val panel = VisualisationPanelDensity()
        panel.size = Dimension(500, 500)
        panel.populate(mapOf(), model)

        panel.shouldMatchImage("Bullseye")
    }

    @Test
    fun `Should adjust for low erraticness`()
    {
        val model = makeDartsModel(scoringDart = 25, standardDeviation = 100.0, maxRadius = 150)

        val panel = VisualisationPanelDensity()
        panel.size = Dimension(500, 500)
        panel.populate(mapOf(), model)

        panel.shouldMatchImage("Erraticness")
    }

    @Test
    fun `Should match snapshot - key`()
    {
        val model = makeDartsModel(scoringDart = 25, standardDeviation = 100.0)

        val panel = VisualisationPanelDensity()
        panel.size = Dimension(500, 500)
        panel.populate(mapOf(), model)

        panel.panelKey.shouldMatchImage("Key")
    }
}