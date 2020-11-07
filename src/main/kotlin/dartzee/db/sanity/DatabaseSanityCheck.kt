package dartzee.db.sanity

import dartzee.core.bean.ScrollTableButton
import dartzee.core.screen.ProgressDialog
import dartzee.core.screen.TableModelDialog
import dartzee.core.util.DialogUtil
import dartzee.core.util.TableUtil.DefaultModel
import dartzee.core.util.runOnEventThread
import dartzee.logging.*
import dartzee.screen.ScreenCache
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.mainDatabase
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.table.DefaultTableModel

fun getAllSanityChecks(): List<AbstractSanityCheck>
{
    //Bog standard checks
    val checks = mutableListOf(
            SanityCheckFinishedParticipantsNoScore(),
            SanityCheckDuplicateDarts(),
            SanityCheckColumnsThatAllowDefaults(),
            SanityCheckUnfinishedGamesNoActiveParticipants(),
            SanityCheckDuplicateMatchOrdinals(),
            SanityCheckFinalScoreX01(),
            SanityCheckFinalScoreGolf(),
            SanityCheckFinalScoreRtc(),
            SanityCheckPlayerIdMismatch(),
            SanityCheckX01Finishes())

    //Checks that run on all entities
    DartsDatabaseUtil.getAllEntities().forEach{
        checks.add(SanityCheckDanglingIdFields(it))
        checks.add(SanityCheckUnsetIdFields(it))
    }

    return checks
}

object DatabaseSanityCheck
{
    private val sanityErrors = mutableListOf<AbstractSanityCheckResult>()

    fun runSanityCheck()
    {
        logger.info(CODE_SANITY_CHECK_STARTED, "Running ${getAllSanityChecks().size} sanity checks...")

        sanityErrors.clear()

        runAllChecks()
    }

    private fun runAllChecks()
    {
        val r = Runnable { runChecksInOtherThread(getAllSanityChecks())}
        val t = Thread(r, "Sanity checks")
        t.start()
    }

    private fun runChecksInOtherThread(checks: List<AbstractSanityCheck>)
    {
        val dlg = ProgressDialog.factory("Running Sanity Check", "checks remaining", checks.size)
        dlg.setVisibleLater()

        try
        {
            getAllSanityChecks().forEach{
                val results = it.runCheck()
                sanityErrors.addAll(results)

                dlg.incrementProgressLater()
            }
        }
        finally
        {
            mainDatabase.dropUnexpectedTables()
            dlg.disposeLater()
        }

        runOnEventThread { sanityCheckComplete() }
    }

    private fun sanityCheckComplete()
    {
        logger.info(CODE_SANITY_CHECK_COMPLETED, "Completed sanity check and found ${sanityErrors.size} issues")

        sanityErrors.forEach {
            logger.info(CODE_SANITY_CHECK_RESULT,
                    "${it.getCount()} ${it.getDescription()}",
                    KEY_SANITY_DESCRIPTION to it.getDescription(),
                    KEY_SANITY_COUNT to it.getCount())
        }

        val tm = buildResultsModel()
        if (tm.rowCount > 0)
        {
            val showResults = object : AbstractAction()
            {
                override fun actionPerformed(e: ActionEvent)
                {
                    val modelRow = Integer.valueOf(e.actionCommand)

                    val result = sanityErrors[modelRow]
                    showResultsBreakdown(result)
                }
            }

            val autoFix = object : AbstractAction()
            {
                override fun actionPerformed(e: ActionEvent)
                {
                    val modelRow = Integer.valueOf(e.actionCommand)

                    val result = sanityErrors[modelRow]
                    result.autoFix()
                }
            }

            val table = ScrollTableButton(tm)
            table.setButtonColumn(2, showResults)
            table.setButtonColumn(3, autoFix)

            val dlg = TableModelDialog("Sanity Results", table)
            dlg.setColumnWidths("-1;50;150;150")
            dlg.setLocationRelativeTo(ScreenCache.mainScreen)
            dlg.isVisible = true
        }
        else
        {
            DialogUtil.showInfo("Sanity check completed and found no issues")
        }
    }

    private fun buildResultsModel(): DefaultTableModel
    {
        val model = DefaultModel()
        model.addColumn("Description")
        model.addColumn("Count")
        model.addColumn("")
        model.addColumn("")

        for (result in sanityErrors)
        {
            val row = arrayOf(result.getDescription(), result.getCount(), "View Results >", "Auto-fix")
            model.addRow(row)
        }

        return model
    }

    private fun showResultsBreakdown(result: AbstractSanityCheckResult)
    {
        val dlg = result.getResultsDialog()
        dlg.setSize(800, 600)
        dlg.setLocationRelativeTo(ScreenCache.mainScreen)
        dlg.isVisible = true
    }
}
