package burlton.dartzee.code.db

import burlton.dartzee.code.`object`.Dart

class DartEntity : AbstractEntity<DartEntity>()
{
    /**
     * DB fields
     */
    var roundId = ""
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
        return ("RoundId VARCHAR(36) NOT NULL, "
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
        val roundIdIndex = mutableListOf("RoundId", "Ordinal")
        indexes.add(roundIdIndex)
    }

    companion object
    {
        @JvmStatic fun factory(dart: Dart, roundId: String, ordinal: Int, startingScore: Int): DartEntity
        {
            val de = DartEntity()
            de.assignRowId()
            de.score = dart.score
            de.multiplier = dart.multiplier
            de.roundId = roundId
            de.ordinal = ordinal
            de.startingScore = startingScore
            de.posX = dart.getX()!!
            de.posY = dart.getY()!!
            de.segmentType = dart.segmentType
            return de
        }
    }
}
