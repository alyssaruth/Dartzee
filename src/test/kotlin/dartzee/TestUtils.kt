package dartzee

import dartzee.`object`.*
import dartzee.core.helper.makeMouseEvent
import dartzee.core.util.getAllChildComponentsForType
import dartzee.dartzee.DartzeeRuleDto
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
import java.awt.Container
import java.awt.Point
import java.time.Instant
import javax.swing.AbstractButton
import javax.swing.JComponent
import javax.swing.SwingUtilities

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
val CURRENT_TIME_STRING = "2020-04-13 12:04:00"

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

/**
 * Flush the Event Dispatch Thread by invoking an empty fn onto the back of the queue and waiting for it
 */
fun flushEdt()
{
    val lambda = {}
    SwingUtilities.invokeAndWait(lambda)
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

fun Component.doClick(x: Int = 0, y: Int = 0) {
    val me = makeMouseEvent(x = x, y = y)
    mouseListeners.forEach { it.mouseReleased(me) }
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

inline fun <reified T: AbstractButton> Container.clickComponent(text: String)
{

    val allComponents = getAllChildComponentsForType<T>()
    val matching = allComponents.filter { it.text == name }

    if (matching.isEmpty())
    {
        throw Exception("No ${T::class.simpleName} found with text [$text]")
    }
    else if (matching.size > 1)
    {
        throw Exception("Non-unique text - ${matching.size} ${T::class.simpleName}s found with text [$text]")
    }

    matching.first().doClick()
}
