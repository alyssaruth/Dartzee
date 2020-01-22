package dartzee.db.sanity

import dartzee.db.AbstractEntity

class SanityCheckResultEntitiesSimple(entities: List<AbstractEntity<*>>, val desc: String) : AbstractSanityCheckResultEntities(entities)
{
    override fun getDescription() = desc
}
