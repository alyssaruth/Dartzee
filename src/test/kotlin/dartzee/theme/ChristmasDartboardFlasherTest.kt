package dartzee.theme

import com.github.romankh3.image.comparison.ImageComparison
import com.github.romankh3.image.comparison.model.ImageComparisonState
import dartzee.bean.PresentationDartboard
import dartzee.screen.GameplayDartboard
import io.github.alyssaruth.swingtest.getChild
import io.github.alyssaruth.swingtest.shouldMatchImage
import io.github.alyssaruth.swingtest.toBufferedImage
import javax.swing.JButton
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class ChristmasDartboardFlasherTest {
    @TestFactory
    @Tag("screenshot")
    fun screenshots(): Collection<DynamicTest> {
        return FlashMode.entries.map { mode ->
            dynamicTest("Should match snapshot - $mode") { snapshotDistinctStages(mode) }
        }
    }

    private fun snapshotDistinctStages(mode: FlashMode) {
        val dartboard = PresentationDartboard(ChristmasDartboardPainter(), renderScoreLabels = true)
        dartboard.setBounds(0, 0, 400, 400)
        var lastImg = dartboard.toBufferedImage()
        val flasher = ChristmasDartboardFlasher(dartboard, {}, {}, mode)

        (0..19).forEach { step ->
            flasher.flashDartboard()
            val img = dartboard.toBufferedImage()
            val result =
                ImageComparison(lastImg, img)
                    .setPixelToleranceLevel(0.01)
                    .setAllowingPercentOfDifferentPixels(0.1)
                    .compareImages()

            if (result.imageComparisonState != ImageComparisonState.MATCH) {
                println("$mode - $step - A difference of ${result.differencePercent}% was detected")
                dartboard.shouldMatchImage("$mode-$step")
            }

            lastImg = img
        }
    }

    @Test
    @Tag("screenshot")
    fun `Should match screenshot with cable`() {
        val dartboard = GameplayDartboard(ChristmasDartboardPainter())
        dartboard.background = christmasBackground
        dartboard.isOpaque = true
        dartboard.setBounds(0, 0, 500, 500)
        val flasher = dartboard.setupFlasher()
        dartboard.repaint()
        dartboard.getChild<JButton>("FlashToggle").isVisible = false
        dartboard.shouldMatchImage("cable")

        flasher.breakCable()
        dartboard.shouldMatchImage("broken-cable")
    }
}
