package burlton.dartzee.code.db

import burlton.dartzee.code.`object`.Dart

class DartEntity : AbstractEntity<DartEntity>()
{
    /**
     * DB fields
     */
    var playerId = ""
    var participantId = ""
    var roundNumber = -1
    var ordinal = -1
    var score = -1
    var multiplier = -1
    var startingScore = -1
    var posX = -1
    var posY = -1
    var segmentType = -1

    override fun getTableName() = "Dart"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("PlayerId VARCHAR(36) NOT NULL, "
                + "ParticipantId VARCHAR(36) NOT NULL, "
                + "RoundNumber INT NOT NULL, "
                + "Ordinal INT NOT NULL, "
                + "Score INT NOT NULL, "
                + "Multiplier INT NOT NULL, "
                + "StartingScore INT NOT NULL, "
                + "PosX INT NOT NULL, "
                + "PosY INT NOT NULL, "
                + "SegmentType INT NOT NULL")
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<MutableList<String>>)
    {
        val roundIdIndex = mutableListOf("PlayerId", "ParticipantId", "RoundNumber", "Ordinal")
        indexes.add(roundIdIndex)
    }

    companion object
    {
        @JvmStatic fun factory(dart: Dart, playerId: String, participantId: String, roundNumber: Int, ordinal: Int, startingScore: Int): DartEntity
        {
            val de = DartEntity()
            de.assignRowId()
            de.playerId = playerId
            de.participantId = participantId
            de.roundNumber = roundNumber
            de.score = dart.score
            de.multiplier = dart.multiplier
            de.ordinal = ordinal
            de.startingScore = startingScore
            de.posX = dart.getX()!!
            de.posY = dart.getY()!!
            de.segmentType = dart.segmentType
            return de
        }
    }
}
