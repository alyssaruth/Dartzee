package dartzee.theme

import dartzee.core.bean.CheatBar
import dartzee.helper.AbstractTest
import dartzee.screen.DartsApp
import dartzee.screen.EmbeddedScreen
import dartzee.toLabel
import io.github.alyssaruth.swingtest.flushEdt
import io.github.alyssaruth.swingtest.shouldMatchImage
import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestFactory

class PopupSliderTest : AbstractTest() {
    @TestFactory
    @Tag("screenshot")
    fun screenshots(): Collection<DynamicTest> {
        return SlideMode.entries.map { mode ->
            dynamicTest("Should match snapshot - $mode") { snapshotPopup(mode) }
        }
    }

    private fun snapshotPopup(mode: SlideMode) {
        val app = DartsApp(CheatBar())

        app.switchScreen(BlankScreen())
        app.isVisible = true

        val listener: PopupListener =
            object : PopupListener {
                override fun popupMoved(phase: String, step: Int) {
                    if (step % 5 == 0) {
                        val img = BufferedImage(app.width, app.height, BufferedImage.TYPE_INT_ARGB)
                        app.paint(img.graphics)
                        ImageIcon(img).toLabel().shouldMatchImage("$mode-$phase-$step")
                    }
                }
            }

        val slider =
            PopupSlider(app, "/theme/christmas/horrific/marta.png", listener, phasePause = 0)
        slider.animate(mode)
        flushEdt()
    }

    private class BlankScreen : EmbeddedScreen() {
        override fun initialise() {
            background = Color.BLACK
        }

        init {
            removeAll()
        }

        override fun getScreenName() = "Blank"
    }
}
