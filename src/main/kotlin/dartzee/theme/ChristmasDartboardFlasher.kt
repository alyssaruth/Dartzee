package dartzee.theme

import dartzee.bean.PresentationDartboard
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.utils.getAllNonMissSegments
import dartzee.utils.isEven
import dartzee.utils.numberOrder
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JButton
import javax.swing.SwingUtilities

enum class FlashMode {
    ALTERNATE_SLOW,
    ALL_FAST,
    CIRCUIT,
    DOUBLE_CIRCUIT,
}

class ChristmasDartboardFlasher(private val dartboard: PresentationDartboard) : ActionListener {
    private var mode: FlashMode = FlashMode.ALTERNATE_SLOW
    private var step = 0
    private val toggleButton = JButton("X")

    init {
        toggleButton.addActionListener(this)
        toggleButton.size = Dimension(20, 20)
        dartboard.add(toggleButton)

        dartboard.addComponentListener(
            object : ComponentAdapter() {
                override fun componentResized(evt: ComponentEvent) {
                    toggleButton.setLocation(dartboard.width - 40, dartboard.height - 40)
                }
            }
        )

        val r = Runnable {
            while (true) {
                Thread.sleep(100)
                SwingUtilities.invokeAndWait { flashDartboard() }
            }
        }

        val t = Thread(r)
        t.start()
    }

    override fun actionPerformed(e: ActionEvent) {
        when (e.source) {
            toggleButton -> toggleMode()
        }
    }

    fun toggleMode() {
        val modes = FlashMode.values().toList()
        val currentIx = modes.indexOf(mode)
        val newIx = (currentIx + 1) % modes.size
        mode = modes[newIx]
    }

    private fun flashDartboard() {
        step = (step + 1) % 20 // 0-19

        val wrapper = ChristmasDartboardPainter()
        val overrides =
            getAllNonMissSegments()
                .filter { it.score == 25 || it.getMultiplier() > 1 }
                .filter { isLit(it, mode, step) }
                .associateWith { wrapper.getFlashColour(it) }

        dartboard.overrideSegmentColours(overrides)
    }

    private fun isLit(segment: DartboardSegment, mode: FlashMode, step: Int): Boolean =
        when (mode) {
            FlashMode.ALTERNATE_SLOW -> (segment.isEven() || segment.getTotal() == 50) == step < 10
            FlashMode.ALL_FAST -> step % 4 == 0 || step % 4 == 1
            FlashMode.CIRCUIT ->
                if (segment.type == SegmentType.DOUBLE) {
                    numberOrder[step] == segment.score
                } else {
                    numberOrder[(20 - step) % 20] == segment.score
                }
            FlashMode.DOUBLE_CIRCUIT -> {
                val target =
                    if (segment.type == SegmentType.DOUBLE) {
                        step / 2
                    } else {
                        (20 - (step / 2)) % 20
                    }

                val litScores = listOf(numberOrder[target], numberOrder[(target + 10) % 20])
                litScores.contains(segment.score) ||
                    segment.score == 25 && (litScores.contains(20) || litScores.contains(6))
            }
        }
}
