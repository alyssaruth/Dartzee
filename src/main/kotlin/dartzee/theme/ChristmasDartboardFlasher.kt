package dartzee.theme

import dartzee.bean.PresentationDartboard
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.utils.getAllNonMissSegments
import dartzee.utils.hmScoreToOrdinal
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
    CIRCUIT,
    OUT_AND_IN,
    ALL_FAST,
    DOUBLE_CIRCUIT,
    ALL_ON,
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

        val litNumbers = (1..20).filter { isNumberLit(it, mode, step) }
        val wrapper =
            ChristmasDartboardPainter(litNumbers = litNumbers)
                .withFont(Themes.CHRISTMAS.dartboardFont!!)
        val overrides =
            getAllNonMissSegments()
                .filter { it.score == 25 || it.getMultiplier() > 1 }
                .filter { isLit(it, mode, step) }
                .associateWith { wrapper.getFlashColour(it) }

        dartboard.overrideSegmentColours(overrides)
        dartboard.repaintScoreLabels(wrapper)
    }

    private fun isNumberLit(number: Int, mode: FlashMode, step: Int): Boolean =
        when (mode) {
            FlashMode.ALTERNATE_SLOW -> hmScoreToOrdinal[number] == step < 10
            FlashMode.ALL_FAST -> step % 4 == 0 || step % 4 == 1
            FlashMode.CIRCUIT ->
                numberOrder[step] == number || numberOrder[(20 - step) % 20] == number
            FlashMode.DOUBLE_CIRCUIT -> {
                val targets = listOf(step / 2, (20 - (step / 2)) % 20)
                val allTargets =
                    targets.flatMap { listOf(numberOrder[it], numberOrder[(it + 10) % 20]) }
                allTargets.contains(number)
            }
            FlashMode.OUT_AND_IN -> step in listOf(0, 1, 18, 19)
            FlashMode.ALL_ON -> true
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
            FlashMode.OUT_AND_IN ->
                when (step) {
                    in listOf(0, 1, 18, 19) -> false
                    in listOf(2, 3, 16, 17) -> segment.isDoubleExcludingBull()
                    in listOf(4, 5, 14, 15) -> segment.getMultiplier() == 3
                    in listOf(6, 7, 12, 13) -> segment.getMultiplier() == 1 && segment.score == 25
                    // in listOf(8, 9, 10, 11)
                    else -> segment.getMultiplier() == 2 && segment.score == 25
                }
            FlashMode.ALL_ON -> true
        }
}
