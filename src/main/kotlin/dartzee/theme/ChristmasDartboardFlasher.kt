package dartzee.theme

import dartzee.bean.PresentationDartboard
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.utils.getAllNonMissSegments
import dartzee.utils.isEven
import dartzee.utils.numberOrder
import javax.swing.SwingUtilities

enum class FlashMode {
    ALTERNATE_SLOW,
    ALL_FAST,
    CIRCUIT,
}

class ChristmasDartboardFlasher(private val dartboard: PresentationDartboard) {
    private var mode: FlashMode = FlashMode.ALTERNATE_SLOW
    private var step = 0

    init {
        val r = Runnable {
            while (true) {
                Thread.sleep(250)
                SwingUtilities.invokeAndWait { flashDartboard() }
            }
        }

        val t = Thread(r)
        t.start()
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
            FlashMode.ALTERNATE_SLOW -> segment.isEven() == (step % 4 == 0 || step % 4 == 1)
            FlashMode.ALL_FAST -> step % 2 == 0
            FlashMode.CIRCUIT -> {
                if (segment.type == SegmentType.DOUBLE) {
                    numberOrder[step] == segment.score
                } else {
                    numberOrder[19 - step] == segment.score
                }
            }
        }
}
