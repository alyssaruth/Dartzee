package dartzee.db.sanity

import dartzee.core.util.TableUtil.DefaultModel

open class SanityCheckResultSimpleTableModel(
    private val model: DefaultModel,
    private val desc: String
) : AbstractSanityCheckResult() {
    override fun getDescription() = desc

    override fun getCount() = model.rowCount

    override fun getResultsModel() = model
}
