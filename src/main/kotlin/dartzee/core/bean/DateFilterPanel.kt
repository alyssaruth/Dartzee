package dartzee.core.bean

import com.github.lgooddatepicker.components.DatePicker
import com.github.lgooddatepicker.components.DatePickerSettings
import dartzee.core.util.DialogUtil.showErrorOLD
import java.sql.Timestamp
import java.text.SimpleDateFormat
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class DateFilterPanel : JPanel() {
    private val dtFormat = SimpleDateFormat("dd/MM/yyyy")

    val cbDateFrom = DatePicker(makeDatePickerSettings())
    val cbDateTo = DatePicker(makeDatePickerSettings())
    private val lblFrom = JLabel("from")
    private val lblTo = JLabel("to")

    init {
        add(lblFrom)
        lblFrom.horizontalAlignment = SwingConstants.LEFT
        add(cbDateFrom)
        add(lblTo)
        lblTo.horizontalAlignment = SwingConstants.LEFT
        add(cbDateTo)
    }

    fun valid(): Boolean {
        if (!cbDateFrom.isEnabled) {
            return true
        }

        if (getDtFrom().isAfter(getDtTo())) {
            showErrorOLD("The 'date from' cannot be after the 'date to'")
            return false
        }

        return true
    }

    fun filterSqlDate(sqlDt: Timestamp) =
        !sqlDt.before(getSqlDtFrom()) && !sqlDt.after(getSqlDtTo())

    fun getSqlDtFrom(): Timestamp = Timestamp.valueOf(getDtFrom())

    fun getSqlDtTo(): Timestamp = Timestamp.valueOf(getDtTo())

    fun getFilterDesc() = "${dtFormat.format(getSqlDtFrom())} - ${dtFormat.format(getSqlDtTo())}"

    private fun getDtFrom() = cbDateFrom.date.atTime(0, 0)

    private fun getDtTo() = cbDateTo.date.atTime(0, 0)

    private fun makeDatePickerSettings(): DatePickerSettings {
        val settings = DatePickerSettings()

        settings.allowEmptyDates = false
        settings.visibleTodayButton = true

        return settings
    }
}
