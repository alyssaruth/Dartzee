package burlton.dartzee.code.db.sanity

import burlton.dartzee.code.db.AbstractEntity

class SanityCheckResultEntitiesSimple(entities: List<AbstractEntity<*>>, val desc: String) : AbstractSanityCheckResultEntities(entities)
{
    override fun getDescription() = desc
}
