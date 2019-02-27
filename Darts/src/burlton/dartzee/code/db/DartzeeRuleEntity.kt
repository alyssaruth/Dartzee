package burlton.dartzee.code.db

import burlton.dartzee.code.dartzee.AbstractDartzeeDartRule
import burlton.dartzee.code.dartzee.parseDartzeeRule
import java.sql.PreparedStatement
import java.sql.ResultSet

class DartzeeRuleEntity: AbstractEntity<DartzeeRuleEntity>()
{
    var gameId = -1L
    var dart1Rule : AbstractDartzeeDartRule? = null
    var dart2Rule : AbstractDartzeeDartRule? = null
    var dart3Rule : AbstractDartzeeDartRule? = null
    var totalRule = ""
    var inOrder = false
    var allowMisses = false
    var scoreMode = -1
    var textualName = ""
    var textualDescription = "" //Allow textual rules
    var ordinal = -1

    override fun getTableName(): String
    {
        return "DartzeeRule"
    }

    override fun getCreateTableSqlSpecific(): String
    {
        return ("GameId INT NOT NULL, "
                + "Dart1Rule VARCHAR(255) NOT NULL, "
                + "Dart2Rule VARCHAR(255) NOT NULL, "
                + "Dart3Rule VARCHAR(255) NOT NULL, "
                + "TotalRule VARCHAR(255) NOT NULL, "
                + "InOrder BOOLEAN NOT NULL, "
                + "AllowMisses BOOLEAN NOT NULL, "
                + "ScoreMode INT NOT NULL, "
                + "TextualName VARCHAR(255) NOT NULL, "
                + "TextualDescription VARCHAR(2500) NOT NULL, "
                + "Ordinal INT NOT NULL")

    }

    override fun populateFromResultSet(entity: DartzeeRuleEntity, rs: ResultSet)
    {
        entity.gameId = rs.getLong("GameId")
        entity.dart1Rule = parseDartzeeRule(rs.getString("Dart1Rule"))
        entity.dart2Rule = parseDartzeeRule(rs.getString("Dart2Rule"))
        entity.dart3Rule = parseDartzeeRule(rs.getString("Dart3Rule"))
        entity.totalRule = rs.getString("TotalRule")
        entity.inOrder = rs.getBoolean("InOrder")
        entity.allowMisses = rs.getBoolean("AllowMisses")
        entity.scoreMode = rs.getInt("ScoreMode")
        entity.textualName = rs.getString("TextualName")
        entity.textualDescription = rs.getString("TextualDescription")
    }

    override fun writeValuesToStatement(statement: PreparedStatement, startIndex: Int, emptyStatement: String): String
    {
        var i = startIndex
        var statementStr = emptyStatement

        statementStr = writeLong(statement, i++, gameId, statementStr)
        statementStr = writeString(statement, i++, dart1Rule?.toDbString() ?: "", statementStr)
        statementStr = writeString(statement, i++, dart2Rule?.toDbString() ?: "", statementStr)
        statementStr = writeString(statement, i++, dart3Rule?.toDbString() ?: "", statementStr)
        statementStr = writeString(statement, i++, totalRule, statementStr)
        statementStr = writeBoolean(statement, i++, inOrder, statementStr)
        statementStr = writeBoolean(statement, i++, allowMisses, statementStr)
        statementStr = writeInt(statement, i++, scoreMode, statementStr)
        statementStr = writeString(statement, i++, textualName, statementStr)
        statementStr = writeString(statement, i++, textualDescription, statementStr)
        statementStr = writeInt(statement, i, ordinal, statementStr)

        return statementStr
    }


}
