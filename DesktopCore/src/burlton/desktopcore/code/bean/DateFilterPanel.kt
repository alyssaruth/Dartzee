package burlton.desktopcore.code.bean

import burlton.desktopcore.code.util.DialogUtil.showError
import burlton.desktopcore.code.util.enableChildren
import burlton.desktopcore.code.util.stripTimeComponent
import de.wannawork.jcalendar.JCalendarComboBox
import java.awt.Dimension
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class DateFilterPanel : JPanel()
{
    private val dtFormat = SimpleDateFormat("dd/MM/yyyy")

    private val cbDateFrom = JCalendarComboBox()
    private val cbDateTo = JCalendarComboBox()
    private val lblFrom = JLabel("from")
    private val lblTo = JLabel("to")

    init
    {
        add(lblFrom)
        lblFrom.horizontalAlignment = SwingConstants.LEFT
        cbDateFrom.preferredSize = Dimension(130, 22)
        add(cbDateFrom)
        add(lblTo)
        lblTo.horizontalAlignment = SwingConstants.LEFT
        cbDateTo.preferredSize = Dimension(130, 22)
        add(cbDateTo)
    }

    fun enableComponents(enabled: Boolean)
    {
        enableChildren(enabled)
    }

    fun valid(): Boolean
    {
        if (!cbDateFrom.isEnabled)
        {
            return true
        }

        if (getDtFrom().after(getDtTo()))
        {
            showError("The 'date from' cannot be after the 'date to'")
            return false
        }

        return true
    }

    fun filterSqlDate(sqlDt: Timestamp) = !sqlDt.before(getDtFrom()) && !sqlDt.after(getDtTo())

    private fun getDtFrom(): Date = stripTimeComponent(cbDateFrom.date)
    fun getSqlDtFrom() = Timestamp(getDtFrom().time)

    private fun getDtTo() = stripTimeComponent(cbDateTo.date)
    fun getSqlDtTo() = Timestamp(getDtTo().time)

    fun getFilterDesc() = "${dtFormat.format(getDtFrom())} - ${dtFormat.format(getDtTo())}"
}