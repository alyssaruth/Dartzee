package dartzee

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.clickYes
import com.github.alyssaburlton.swingtest.doClick
import com.github.alyssaburlton.swingtest.findAll
import com.github.alyssaburlton.swingtest.findWindow
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.purgeWindows
import com.github.alyssaburlton.swingtest.typeText
import dartzee.bean.ComboBoxGameType
import dartzee.bean.InteractiveDartboard
import dartzee.bean.PlayerImageRadio
import dartzee.bean.PresentationDartboard
import dartzee.core.bean.ButtonColumn
import dartzee.core.bean.DateFilterPanel
import dartzee.core.bean.FileUploader
import dartzee.core.bean.ScrollTable
import dartzee.core.bean.items
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
import dartzee.utils.DevUtilities
import dartzee.utils.getAverage
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.MockKMatcherScope
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.Point
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JTabbedPane
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableModel
import javax.swing.text.JTextComponent

val bullseye = DartboardSegment(SegmentType.DOUBLE, 25)
val outerBull = DartboardSegment(SegmentType.OUTER_SINGLE, 25)
val innerSingle = DartboardSegment(SegmentType.INNER_SINGLE, 20)
val outerSingle = DartboardSegment(SegmentType.OUTER_SINGLE, 15)
val missTwenty = DartboardSegment(SegmentType.MISS, 20)
val missSeventeen = DartboardSegment(SegmentType.MISS, 17)

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
val CURRENT_TIME_STRING: String =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withLocale(Locale.UK)
        .withZone(ZoneId.systemDefault())
        .format(CURRENT_TIME)

fun makeLogRecord(
    timestamp: Instant = CURRENT_TIME,
    severity: Severity = Severity.INFO,
    loggingCode: LoggingCode = LoggingCode("log"),
    message: String = "A thing happened",
    errorObject: Throwable? = null,
    keyValuePairs: Map<String, Any?> = mapOf(),
) = LogRecord(timestamp, severity, loggingCode, message, errorObject, keyValuePairs)

fun Float.shouldBeBetween(a: Double, b: Double) {
    toDouble().shouldBeBetween(a, b, 0.0)
}

fun Component.shouldHaveColours(colours: Pair<Color?, Color?>) {
    background shouldBe colours.first
    foreground shouldBe colours.second
}

fun JComponent.shouldHaveBorderThickness(left: Int, right: Int, top: Int, bottom: Int) {
    val insets = border.getBorderInsets(this)
    insets.left shouldBe left
    insets.right shouldBe right
    insets.top shouldBe top
    insets.bottom shouldBe bottom
}

fun MockKMatcherScope.launchParamsEqual(expected: GameLaunchParams) =
    match<GameLaunchParams> { actual ->
        val expectedNoDartzee = expected.copy(dartzeeDtos = emptyList())
        val actualNoDartzee = actual.copy(dartzeeDtos = emptyList())

        expectedNoDartzee == actualNoDartzee &&
            expected.dartzeeDtos?.map { it.generateRuleDescription() } ==
                actual.dartzeeDtos?.map { it.generateRuleDescription() }
    }

fun LogRecord.shouldContainKeyValues(vararg values: Pair<String, Any?>) {
    keyValuePairs.shouldContainExactly(mapOf(*values))
}

fun ComboBoxGameType.updateSelection(type: GameType) {
    selectedItem = items().find { it.hiddenData == type }
}

fun DateFilterPanel.makeInvalid() {
    cbDateFrom.date = LocalDate.ofYearDay(2020, 30)
    cbDateTo.date = LocalDate.ofYearDay(2020, 20)
}

fun ScrollTable.getColumnNames() = (0 until columnCount).map { getColumnName(it) }

fun ScrollTable.getDisplayValueAt(row: Int, col: Int): Any = table.getValueAt(row, col)

fun ScrollTable.getRows(): List<List<Any?>> = model.getRows(columnCount)

fun ScrollTable.getRenderedRows(): List<List<Any?>> =
    (0 until rowCount).map { row ->
        (0 until columnCount).map { col ->
            val value = getValueAt(row, col)
            val r = table.getCellRenderer(row, col)
            val component = r?.getTableCellRendererComponent(table, value, false, false, row, col)
            if (component is JLabel) component.text else value
        }
    }

fun ScrollTable.clickTableButton(row: Int, col: Int) {
    val editor = table.getCellEditor(row, col)
    if (editor !is ButtonColumn) {
        throw AssertionError("Cell is not a TableButton: $editor")
    }

    table.editingRow = row
    editor.editButton.doClick()
}

fun ScrollTable.getFooterRow(): List<Any?> =
    (0 until columnCount).map { getValueAt(ScrollTable.TABLE_ROW_FOOTER, it) }

fun ScrollTable.getFirstRow() = getRows().first()

fun DefaultTableModel.getRows(columns: Int = columnCount): List<List<Any?>> {
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

fun PresentationDartboard.getPointForSegment(segment: DartboardSegment) =
    getAverage(getPointsForSegment(segment))

fun PresentationDartboard.doClick(pt: Point) {
    doClick(pt.x, pt.y)
    flushEdt()
}

fun GameplayDartboard.throwDartByClick(
    segment: DartboardSegment = DartboardSegment(SegmentType.OUTER_SINGLE, 20)
) {
    val interactiveDartboard = getChild<InteractiveDartboard>()
    val pt = interactiveDartboard.getPointForSegment(segment)
    interactiveDartboard.doClick(pt.x, pt.y)
}

fun GameplayDartboard.segmentStatuses() = getChild<PresentationDartboard>().segmentStatuses

fun getFileChooser() = findWindow<JDialog> { it.title == "Open" }!!

fun <T> List<T>.only(): T {
    size shouldBe 1
    return first()
}

fun PlayerImageDialog.selectImage(playerImageId: String) {
    getChild<JTabbedPane>().selectTab<JPanel>("uploadTab")

    val radio = getChild<PlayerImageRadio> { it.playerImageId == playerImageId }
    radio.clickChild<JRadioButton>()

    flushEdt()
}

fun FileUploader.uploadFileFromResource(resourceName: String) {
    clickChild<JButton>(text = "...", async = true)

    val chooserDialog = getFileChooser()
    val rsrcPath = javaClass.getResource(resourceName)!!.path
    chooserDialog.getChild<JTextComponent>().typeText(rsrcPath)
    chooserDialog.clickChild<JButton>(text = "Open")
    flushEdt()

    getChild<JTextField>().text shouldBe File(rsrcPath).path
    clickChild<JButton>(text = "Upload", async = true)
    flushEdt()
}

fun getInfoDialog() = getOptionPaneDialog("Information")

fun getQuestionDialog() = getOptionPaneDialog("Question")

fun getErrorDialog() = getOptionPaneDialog("Error")

private fun getOptionPaneDialog(title: String) = findWindow<JDialog> { it.title == title }!!

fun JDialog.getDialogMessage(): String {
    val messageLabels = findAll<JLabel>().filter { it.name == "OptionPane.label" }
    return messageLabels.joinToString("\n\n") { it.text }
}

fun <T> runAsync(block: () -> T?): T? {
    var result: T? = null
    SwingUtilities.invokeLater { result = block() }

    flushEdt()
    return result
}

fun runExpectingError(errorText: String, block: () -> Boolean) {
    var result: Boolean? = null
    SwingUtilities.invokeLater { result = block() }

    flushEdt()
    getErrorDialog().getDialogMessage() shouldBe errorText

    getErrorDialog().clickOk()
    flushEdt()
    purgeWindows()

    result shouldBe false
}

fun purgeGameAndConfirm(localId: Long): String {
    runAsync { DevUtilities.purgeGame(localId) }

    return confirmGameDeletion(localId)
}

fun confirmGameDeletion(localId: Long): String {
    val dlg = getQuestionDialog()
    val questionText = dlg.getDialogMessage()
    dlg.clickYes()
    flushEdt()

    val info = getInfoDialog()
    info.getDialogMessage() shouldBe "Game #$localId has been purged."
    info.clickOk()
    flushEdt()

    return questionText
}

/** TODO - Add to swing-test */
inline fun <reified T : Component> JTabbedPane.selectTab(
    name: String,
    noinline filterFn: ((T) -> Boolean)? = null,
) {
    runOnEventThreadBlocking { selectedComponent = getChild<T>(name, filterFn = filterFn) }
}

fun ImageIcon.toLabel(): JLabel {
    val label = JLabel(this)
    label.size = Dimension(iconWidth, iconHeight)
    label.repaint()
    return label
}

fun <T> waitForAssertionWithReturn(timeout: Int = 10000, assertion: (() -> T)): T {
    val startTime = System.currentTimeMillis()
    while (true) {
        try {
            return assertion()
        } catch (e: AssertionError) {
            Thread.sleep(200)

            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed > timeout) {
                throw AssertionError("Timed out waiting for assertion - see cause for details", e)
            }
        }
    }
}

fun Container.clickButton(
    name: String? = null,
    text: String? = null,
    async: Boolean = false,
    filterFn: ((JButton) -> Boolean)? = null,
) {
    clickChild<JButton>(name, text, async, filterFn)
}
