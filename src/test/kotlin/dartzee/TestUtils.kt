package dartzee

import com.github.alexburlton.swingtest.isEqual
import dartzee.`object`.*
import dartzee.bean.ComboBoxGameType
import dartzee.core.bean.DateFilterPanel
import dartzee.core.bean.ScrollTable
import dartzee.core.bean.items
import dartzee.core.helper.makeMouseEvent
import dartzee.dartzee.DartzeeRuleDto
import dartzee.game.GameType
import dartzee.logging.LogRecord
import dartzee.logging.LoggingCode
import dartzee.logging.Severity
import dartzee.screen.Dartboard
import io.kotlintest.matchers.doubles.shouldBeBetween
import io.kotlintest.matchers.maps.shouldContainExactly
import io.kotlintest.shouldBe
import io.mockk.MockKMatcherScope
import java.awt.Color
import java.awt.Component
import java.awt.Point
import java.awt.image.BufferedImage
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.Icon
import javax.swing.JComponent

val bullseye = DartboardSegment("25_$SEGMENT_TYPE_DOUBLE")
val outerBull = DartboardSegment("25_$SEGMENT_TYPE_OUTER_SINGLE")
val innerSingle = DartboardSegment("20_$SEGMENT_TYPE_INNER_SINGLE")
val outerSingle = DartboardSegment("15_$SEGMENT_TYPE_OUTER_SINGLE")
val missTwenty = DartboardSegment("20_$SEGMENT_TYPE_MISS")
val missedBoard = DartboardSegment("15_$SEGMENT_TYPE_MISSED_BOARD")

val singleTwenty = DartboardSegment("20_$SEGMENT_TYPE_INNER_SINGLE")
val doubleTwenty = DartboardSegment("20_$SEGMENT_TYPE_DOUBLE")
val trebleTwenty = DartboardSegment("20_$SEGMENT_TYPE_TREBLE")
val singleNineteen = DartboardSegment("19_$SEGMENT_TYPE_OUTER_SINGLE")
val doubleNineteen = DartboardSegment("19_$SEGMENT_TYPE_DOUBLE")
val trebleNineteen = DartboardSegment("19_$SEGMENT_TYPE_TREBLE")
val singleEighteen = DartboardSegment("18_$SEGMENT_TYPE_OUTER_SINGLE")
val singleTen = DartboardSegment("10_$SEGMENT_TYPE_INNER_SINGLE")
val singleFive = DartboardSegment("5_$SEGMENT_TYPE_INNER_SINGLE")

val CURRENT_TIME: Instant = Instant.parse("2020-04-13T11:04:00.00Z")
val CURRENT_TIME_STRING: String = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withLocale(Locale.UK)
        .withZone(ZoneId.systemDefault())
        .format(CURRENT_TIME)

private var dartboard: Dartboard? = null

fun borrowTestDartboard(): Dartboard
{
    if (dartboard == null)
    {
        dartboard = Dartboard(100, 100)
        dartboard!!.paintDartboard()
    }

    return dartboard!!
}

fun Dartboard.getColor(pt: Point): Color = Color(dartboardImage!!.getRGB(pt.x, pt.y), true)

fun Dartboard.doClick(x: Int, y: Int)
{
    val me = makeMouseEvent(x = x, y = y)

    dartboardLabel.mouseListeners.forEach {
        it.mouseReleased(me)
    }
}

fun makeLogRecord(timestamp: Instant = CURRENT_TIME,
                  severity: Severity = Severity.INFO,
                  loggingCode: LoggingCode = LoggingCode("log"),
                  message: String = "A thing happened",
                  errorObject: Throwable? = null,
                  keyValuePairs: Map<String, Any?> = mapOf()): LogRecord
{
    return LogRecord(timestamp, severity, loggingCode, message, errorObject, keyValuePairs)
}

fun Float.shouldBeBetween(a: Double, b: Double) {
    return toDouble().shouldBeBetween(a, b, 0.0)
}

fun Component.shouldHaveColours(colours: Pair<Color?, Color?>)
{
    background shouldBe colours.first
    foreground shouldBe colours.second
}

fun JComponent.shouldHaveBorderThickness(left: Int, right: Int, top: Int, bottom: Int)
{
    val insets = border.getBorderInsets(this)
    insets.left shouldBe left
    insets.right shouldBe right
    insets.top shouldBe top
    insets.bottom shouldBe bottom
}

fun MockKMatcherScope.ruleDtosEq(players: List<DartzeeRuleDto>) = match<List<DartzeeRuleDto>> {
    it.map { p -> p.generateRuleDescription() } == players.map { p -> p.generateRuleDescription() }
}

fun LogRecord.shouldContainKeyValues(vararg values: Pair<String, Any?>)
{
    keyValuePairs.shouldContainExactly(mapOf(*values))
}

fun ComboBoxGameType.updateSelection(type: GameType)
{
    selectedItem = items().find { it.hiddenData == type }
}

fun DateFilterPanel.makeInvalid()
{
    cbDateFrom.date = LocalDate.ofYearDay(2020, 30)
    cbDateTo.date = LocalDate.ofYearDay(2020, 20)
}

fun ScrollTable.getColumnNames() = (0 until columnCount).map { getColumnName(it) }

fun ScrollTable.getDisplayValueAt(row: Int, col: Int): Any = table.getValueAt(row, col)

fun Icon.shouldMatch(other: Icon)
{
    toBufferedImage().isEqual(other.toBufferedImage()) shouldBe true
}
private fun Icon.toBufferedImage(): BufferedImage
{
    val bi = BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_RGB)
    val g = bi.createGraphics()
    paintIcon(null, g, 0, 0)
    g.dispose()
    return bi
}