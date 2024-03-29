package dartzee.screen.game.scorer

import dartzee.helper.AbstractTest
import dartzee.`object`.Dart
import dartzee.`object`.DartHint
import io.kotest.matchers.shouldBe
import java.awt.Color
import java.awt.Font
import org.junit.jupiter.api.Test

class TestDartRenderer : AbstractTest() {
    @Test
    fun `Should render DartHints in brackets, and regular darts without`() {
        val renderer = DartRenderer()

        renderer.getReplacementValue(DartHint(20, 3)) shouldBe "(T20)"
        renderer.getReplacementValue(Dart(20, 3)) shouldBe "T20"
    }

    @Test
    fun `Should render normal darts correctly, and be responsive to selection`() {
        val renderer = DartRenderer()

        renderer.setCellColours(Dart(20, 3), false)
        renderer.foreground shouldBe Color.BLACK
        renderer.font.style shouldBe Font.PLAIN

        renderer.setCellColours(Dart(20, 3), true)
        renderer.foreground shouldBe Color.WHITE
        renderer.font.style shouldBe Font.PLAIN
    }

    @Test
    fun `Should render dart hints in red italic, and be responsive to selection`() {
        val renderer = DartRenderer()

        renderer.setCellColours(DartHint(20, 3), false)
        renderer.foreground shouldBe Color.RED
        renderer.font.style shouldBe Font.ITALIC

        renderer.setCellColours(DartHint(20, 3), true)
        renderer.foreground shouldBe Color.CYAN
        renderer.font.style shouldBe Font.ITALIC
    }
}
