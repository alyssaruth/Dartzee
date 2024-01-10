package dartzee.utils

import dartzee.core.screen.ProgressDialog
import dartzee.db.DartzeeRuleEntity

object DartzeeRuleConversion {
    fun convertDartzeeRules() {
        val allRules = DartzeeRuleEntity().retrieveEntities()

        val r = Runnable {
            val dlg = ProgressDialog.factory("Converting Dartzee Rules", "rules", allRules.size)

            dlg.setVisibleLater()

            try {
                allRules.forEach { rule ->
                    val dto = rule.toDto(false)
                    val newResult = dto.runStrengthCalculation()
                    rule.calculationResult = newResult.toDbString()
                    rule.saveToDatabase()

                    dlg.incrementProgressLater()
                }
            } finally {
                dlg.disposeLater()
            }
        }

        val t = Thread(r)
        t.start()
        t.join()
    }
}
