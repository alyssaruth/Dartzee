package burlton.dartzee.code.db

import burlton.dartzee.code.dartzee.AbstractDartzeeDartRule

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
}
