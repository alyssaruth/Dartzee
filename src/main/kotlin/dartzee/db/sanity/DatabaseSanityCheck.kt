package dartzee.db.sanity

import dartzee.core.bean.ScrollTableButton
import dartzee.core.screen.ProgressDialog
import dartzee.core.screen.TableModelDialog
import dartzee.core.util.DialogUtil
import dartzee.core.util.TableUtil.DefaultModel
import dartzee.core.util.runOnEventThread
import dartzee.logging.CODE_SANITY_CHECK_COMPLETED
import dartzee.logging.CODE_SANITY_CHECK_RESULT
import dartzee.logging.CODE_SANITY_CHECK_STARTED
import dartzee.logging.KEY_SANITY_COUNT
import dartzee.logging.KEY_SANITY_DESCRIPTION
import dartzee.screen.ScreenCache
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.mainDatabase
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.table.DefaultTableModel

private fun getAllSanityChecks(): List<AbstractSanityCheck> {
    val specificChecks = listOf(
        SanityCheckFinishedParticipantsNoScore(),
        SanityCheckDuplicateDarts(),
        SanityCheckColumnsThatAllowDefaults(),
        SanityCheckUnfinishedGamesNoActiveParticipants(),
        SanityCheckDuplicateMatchOrdinals(),
        SanityCheckFinalScoreX01(),
        SanityCheckFinalScoreGolf(),
        SanityCheckFinalScoreRtc(),
        SanityCheckPlayerIdMismatch(),
        SanityCheckX01Finishes()
    )

    val genericChecks: List<AbstractSanityCheck> = DartsDatabaseUtil.getAllEntities().flatMap {
        listOf(SanityCheckDanglingIdFields(it), SanityCheckUnsetIdFields(it))
    }

    return specificChecks + genericChecks
}

object DatabaseSanityCheck
{
    fun runSanityCheck(checks: List<AbstractSanityCheck> = getAllSanityChecks())
    {
        logger.info(CODE_SANITY_CHECK_STARTED, "Running ${getAllSanityChecks().size} sanity checks...")

        runAllChecks(checks)
    }

    private fun runAllChecks(checks: List<AbstractSanityCheck>)
    {
        val r = Runnable { runChecksInOtherThread(checks) }
        val t = Thread(r, "Sanity checks")
        t.start()
    }

    private fun runChecksInOtherThread(checks: List<AbstractSanityCheck>)
    {
        val dlg = ProgressDialog.factory("Running Sanity Check", "checks remaining", checks.size)
        dlg.setVisibleLater()

        val sanityErrors = mutableListOf<AbstractSanityCheckResult>()

        try
        {
            checks.forEach {
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

        runOnEventThread { sanityCheckComplete(sanityErrors) }
    }

    private fun sanityCheckComplete(sanityErrors: List<AbstractSanityCheckResult>)
    {
        logger.info(CODE_SANITY_CHECK_COMPLETED, "Completed sanity check and found ${sanityErrors.size} issues")

        sanityErrors.forEach {
            logger.info(CODE_SANITY_CHECK_RESULT,
                    "${it.getCount()} ${it.getDescription()}",
                    KEY_SANITY_DESCRIPTION to it.getDescription(),
                    KEY_SANITY_COUNT to it.getCount())
        }

        val tm = buildResultsModel(sanityErrors)
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
            DialogUtil.showInfoOLD("Sanity check completed and found no issues")
        }
    }

    private fun buildResultsModel(sanityErrors: List<AbstractSanityCheckResult>): DefaultTableModel
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
