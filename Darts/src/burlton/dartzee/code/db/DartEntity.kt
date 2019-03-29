package burlton.dartzee.code.db

import burlton.dartzee.code.`object`.Dart
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

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

    @Throws(SQLException::class)
    override fun populateFromResultSet(entity: DartEntity, rs: ResultSet)
    {
        entity.roundId = rs.getString("RoundId")
        entity.ordinal = rs.getInt("Ordinal")
        entity.score = rs.getInt("Score")
        entity.multiplier = rs.getInt("Multiplier")
        entity.startingScore = rs.getInt("StartingScore")
        entity.posX = rs.getInt("PosX")
        entity.posY = rs.getInt("PosY")
        entity.segmentType = rs.getInt("SegmentType")
    }

    @Throws(SQLException::class)
    override fun writeValuesToStatement(statement: PreparedStatement, startIndex: Int, emptyStatement: String): String
    {
        var i = startIndex
        var statementStr = emptyStatement
        statementStr = writeString(statement, i++, roundId, statementStr)
        statementStr = writeInt(statement, i++, ordinal, statementStr)
        statementStr = writeInt(statement, i++, score, statementStr)
        statementStr = writeInt(statement, i++, multiplier, statementStr)
        statementStr = writeInt(statement, i++, startingScore, statementStr)
        statementStr = writeInt(statement, i++, posX, statementStr)
        statementStr = writeInt(statement, i++, posY, statementStr)
        statementStr = writeInt(statement, i, segmentType, statementStr)

        return statementStr
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<MutableList<String>>)
    {
        val roundIdIndex = mutableListOf("RoundId", "Ordinal")
        indexes.add(roundIdIndex)
    }

    companion object
    {
        fun factoryAndSave(dart: Dart, roundId: String, ordinal: Int, startingScore: Int): DartEntity
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

            de.saveToDatabase()
            return de
        }
    }
}
