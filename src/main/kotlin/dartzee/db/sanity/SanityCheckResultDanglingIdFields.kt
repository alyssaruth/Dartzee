package dartzee.db.sanity

import dartzee.core.util.DialogUtil
import dartzee.db.AbstractEntity
import dartzee.db.EntityName
import dartzee.utils.InjectedThings.mainDatabase
import javax.swing.JOptionPane

class SanityCheckResultDanglingIdFields(private val idColumn: String,
                                        private val referencedEntity: EntityName,
                                        entities: List<AbstractEntity<*>>) : AbstractSanityCheckResultEntities(entities)
{
    override fun getDescription() = "$entityName rows where the $idColumn points at a non-existent $referencedEntity"

    override fun autoFix()
    {
        val rowIds = entities.map { it.rowId }

        val ans = DialogUtil.showQuestionOLD("Are you sure you want to delete ${entities.size} rows from $entityName?")
        if (ans != JOptionPane.YES_OPTION)
        {
            return
        }

        val success = mainDatabase.deleteRowsFromTable(entityName, rowIds)
        if (success)
        {
            DialogUtil.showInfoOLD("Rows deleted successfully. You should re-run the sanity check.")
        }
        else
        {
            DialogUtil.showErrorOLD("An error occurred deleting the rows.")
        }
    }
}
