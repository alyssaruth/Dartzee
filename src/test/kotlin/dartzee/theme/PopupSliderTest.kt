package dartzee.theme

import dartzee.helper.AbstractTest
import io.github.alyssaruth.swingtest.flushEdt
import io.github.alyssaruth.swingtest.shouldMatchImage
import java.awt.Color
import java.awt.Dimension
import javax.swing.JPanel
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
        val panel = JPanel()
        panel.background = Color.BLACK
        panel.size = Dimension(800, 600)

        val listener: PopupListener =
            object : PopupListener {
                override fun popupMoved(phase: String, step: Int) {
                    if (step % 5 == 0) {
                        panel.shouldMatchImage("$mode-$phase-$step")
                    }
                }
            }

        val slider =
            PopupSlider(
                panel,
                { true },
                "/theme/christmas/horrific/marta.png",
                listener,
                phasePause = 0,
            )
        slider.animate(mode)
        flushEdt()
    }
}
