package dartzee

import com.github.alyssaburlton.swingtest.awaitCondition
import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.doClick
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import dartzee.bean.ComboBoxGameType
import dartzee.bean.InteractiveDartboard
import dartzee.bean.PlayerImageRadio
import dartzee.bean.PresentationDartboard
import dartzee.core.bean.ButtonColumn
import dartzee.core.bean.DateFilterPanel
import dartzee.core.bean.ScrollTable
import dartzee.core.bean.items
import dartzee.core.util.runOnEventThread
import dartzee.core.util.runOnEventThreadBlocking
import dartzee.game.GameLaunchParams
import dartzee.game.GameType
import dartzee.logging.LogRecord
import dartzee.logging.LoggingCode
import dartzee.logging.Severity
import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.screen.GameplayDartboard
import dartzee.screen.PlayerImageDialog
import dartzee.utils.getAverage
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.MockKMatcherScope
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Point
import java.awt.Window
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.AbstractButton
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JTabbedPane
import javax.swing.table.DefaultTableModel
import javax.swing.text.JTextComponent

val bullseye = DartboardSegment(SegmentType.DOUBLE, 25)
val outerBull = DartboardSegment(SegmentType.OUTER_SINGLE, 25)
val innerSingle = DartboardSegment(SegmentType.INNER_SINGLE, 20)
val outerSingle = DartboardSegment(SegmentType.OUTER_SINGLE, 15)
val missTwenty = DartboardSegment(SegmentType.MISS, 20)

val singleTwenty = DartboardSegment(SegmentType.INNER_SINGLE, 20)
val doubleTwenty = DartboardSegment(SegmentType.DOUBLE, 20)
val trebleTwenty = DartboardSegment(SegmentType.TREBLE, 20)
val singleNineteen = DartboardSegment(SegmentType.OUTER_SINGLE, 19)
val doubleNineteen = DartboardSegment(SegmentType.DOUBLE, 19)
val trebleNineteen = DartboardSegment(SegmentType.TREBLE, 19)
val singleEighteen = DartboardSegment(SegmentType.OUTER_SINGLE, 18)
val singleTen = DartboardSegment(SegmentType.INNER_SINGLE, 10)
val singleFive = DartboardSegment(SegmentType.INNER_SINGLE, 5)

val PAST_TIME: Instant = Instant.parse("2020-04-12T11:04:00.00Z")
val CURRENT_TIME: Instant = Instant.parse("2020-04-13T11:04:00.00Z")
val CURRENT_TIME_STRING: String = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withLocale(Locale.UK)
        .withZone(ZoneId.systemDefault())
        .format(CURRENT_TIME)

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
    toDouble().shouldBeBetween(a, b, 0.0)
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

fun MockKMatcherScope.launchParamsEqual(expected: GameLaunchParams) = match<GameLaunchParams> { actual ->
    val expectedNoDartzee = expected.copy(dartzeeDtos = emptyList())
    val actualNoDartzee = actual.copy(dartzeeDtos = emptyList())

    expectedNoDartzee == actualNoDartzee &&
            expected.dartzeeDtos?.map { it.generateRuleDescription() } == actual.dartzeeDtos?.map { it.generateRuleDescription() }
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

fun ScrollTable.getRows(): List<List<Any?>> =
    model.getRows(columnCount)

fun ScrollTable.clickTableButton(row: Int, col: Int) {
    val editor = table.getCellEditor(row, col)
    if (editor !is ButtonColumn) {
        throw AssertionError("Cell is not a TableButton: $editor")
    }

    table.editingRow = row
    editor.editButton.doClick()
}

fun ScrollTable.getFooterRow(): List<Any?> =
    (0 until columnCount).map { getValueAt(ScrollTable.TABLE_ROW_FOOTER, it)}

fun ScrollTable.getFirstRow() = getRows().first()

fun DefaultTableModel.getRows(columns: Int = columnCount): List<List<Any?>>
{
    val result = mutableListOf<List<Any?>>()
    for (rowIx in 0 until rowCount) {
        val row = (0 until columns).map { getValueAt(rowIx, it) }
        result.add(row)
    }

    return result.toList()
}

fun ScrollTable.firstRow(): List<Any?> = getRows().first()

fun List<List<Dart>>.zipDartRounds(other: List<List<Dart>>): List<List<Dart>> {
    val result = zip(other) { p1Round, p2Round -> listOf(p1Round, p2Round) }.flatten()

    val extraRows = subList(other.size, size)
    return result + extraRows
}

fun PresentationDartboard.getPointForSegment(segment: DartboardSegment) = getAverage(getPointsForSegment(segment))

/**
 * TODO - swing-test should do all the interactions on the event thread
 */
fun PresentationDartboard.doClick(pt: Point) {
    runOnEventThreadBlocking {
        doClick(pt.x, pt.y)
    }

    flushEdt()
}

fun GameplayDartboard.throwDartByClick(segment: DartboardSegment = DartboardSegment(SegmentType.OUTER_SINGLE, 20))
{
    val interactiveDartboard = getChild<InteractiveDartboard>()
    val pt = interactiveDartboard.getPointForSegment(segment)
    interactiveDartboard.doClick(pt)
}

fun GameplayDartboard.segmentStatuses() = getChild<PresentationDartboard>().segmentStatuses

fun Container.clickOk() = clickChild<JButton>(text = "Ok")
fun Container.clickCancel() = clickChild<JButton>(text = "Cancel")

fun awaitFileChooser() = awaitWindow<JDialog> { it.title == "Open" }

fun <T> List<T>.only(): T {
    size shouldBe 1
    return first()
}

fun PlayerImageDialog.selectImage(playerImageId: String)
{
    getChild<JTabbedPane>().selectTab<JPanel>("uploadTab")

    runOnEventThreadBlocking {
        val radio = getChild<PlayerImageRadio> { it.playerImageId == playerImageId }
        radio.clickChild<JRadioButton>()
    }

    flushEdt()
}

/**
 * TODO - Add to swing-test
 */
fun Container.dumpComponentTree(prefix: String = "", constraintDesc: String = "")
{
    val children = components
    val borderLayout = layout as? BorderLayout
    println("$prefix$constraintDesc${this.oneLineDescription()}")
    children.forEach { child ->
        val newPrefix = " ".repeat(prefix.length)
        val childConstraint = borderLayout?.getConstraints(child)?.let { " [$it] "} ?: ""
        if (child is Container)
            child.dumpComponentTree("$newPrefix|-", childConstraint)
        else
            println("$newPrefix|-$childConstraint ${child.oneLineDescription()}")
    }
}
private fun Component.oneLineDescription(): String
{
    val className = javaClass.simpleName.ifEmpty { javaClass.name }

    val desc = when (this) {
        is AbstractButton -> {
            val info = (text ?: "").ifEmpty { toolTipText }
            """$className - "$info""""
        }
        is JLabel -> """$className - "$text""""
        is JFrame -> """$className - "$title" - ${layout?.javaClass?.simpleName}"""
        is JDialog -> """$className - "$title"" - ${layout?.javaClass?.simpleName}"""
        is JComboBox<*> -> {
            val itemType = items().firstOrNull()?.describeClass() ?: "*"
            val selection = selectedItem?.toString()
            "JComboBox<$itemType> - Selected item: $selection"
        }
        is JComponent -> {
            val toolTipDesc = toolTipText?.let { """ - "$it"""" } ?: ""
            """$className$toolTipDesc - ${layout?.javaClass?.simpleName}"""
        }
        is Container -> "$className - ${layout?.javaClass?.simpleName}"
        else -> className
    }

    return if (name != null) {
        "$desc [$name]"
    } else {
        desc
    }
}

private fun Any.describeClass() = javaClass.simpleName.ifEmpty { javaClass.name }

inline fun <reified T: Component> JTabbedPane.selectTab(name: String, noinline filterFn: ((T) -> Boolean)? = null)
{
    runOnEventThreadBlocking {
        selectedComponent = getChild<T>(name, filterFn = filterFn)
    }
}

fun Container.clickButton(
    text: String? = null,
    async: Boolean = false,
    filterFn: ((JButton) -> Boolean)? = null)
{
    if (async) {
        runOnEventThread { clickChild<JButton>(null, text, filterFn) }
    } else {
        runOnEventThreadBlocking { clickChild<JButton>(null, text, filterFn) }
    }
}
inline fun <reified W : Window> awaitWindow(crossinline fn: (window: W) -> Boolean = { true }): W {
    awaitCondition { findWindow<W>(fn) != null }
    return findWindow(fn)!!
}
inline fun <reified W : Window> findWindow(fn: (window: W) -> Boolean = { true }): W? = Window.getWindows().find { it is W && fn(it) } as? W

fun JTextComponent.typeText(newText: String)
{
    runOnEventThreadBlocking { text = newText }
}