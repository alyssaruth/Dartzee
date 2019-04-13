package burlton.dartzee.code.db

class RoundEntity : AbstractEntity<RoundEntity>()
{
    /**
     * DB Fields
     */
    var participantId: String = ""
    var roundNumber = -1

    override fun getTableName() = "Round"

    override fun getCreateTableSqlSpecific(): String
    {
        return "ParticipantId VARCHAR(36) NOT NULL, RoundNumber INT NOT NULL"
    }

    fun isForParticipant(pt: ParticipantEntity): Boolean
    {
        val ptId = pt.rowId
        return participantId == ptId
    }

    companion object
    {

        /**
         * This is NOT a 'factoryAndSave' because we only want to save the Round when it's over, along with
         * its corresponding darts. This makes loading unfinished games much simpler.
         */
        fun factory(participant: ParticipantEntity, roundNumber: Int): RoundEntity
        {
            val re = RoundEntity()
            re.assignRowId()

            re.participantId = participant.rowId
            re.roundNumber = roundNumber

            return re
        }
    }
}
