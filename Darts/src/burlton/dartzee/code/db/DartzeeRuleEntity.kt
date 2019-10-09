package burlton.dartzee.code.db

class DartzeeRuleEntity: AbstractEntity<DartzeeRuleEntity>()
{
    var gameId = ""
    var dart1Rule = ""
    var dart2Rule = ""
    var dart3Rule = ""
    var totalRule = ""
    var inOrder = false
    var allowMisses = false
    var ordinal = -1
    var calculationResult = ""

    override fun getTableName() = "DartzeeRule"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("GameId VARCHAR(36) NOT NULL, "
                + "Dart1Rule VARCHAR(255) NOT NULL, "
                + "Dart2Rule VARCHAR(255) NOT NULL, "
                + "Dart3Rule VARCHAR(255) NOT NULL, "
                + "TotalRule VARCHAR(255) NOT NULL, "
                + "InOrder BOOLEAN NOT NULL, "
                + "AllowMisses BOOLEAN NOT NULL, "
                + "Ordinal INT NOT NULL, "
                + "CalculationResult VARCHAR(32000) NOT NULL")
    }
}
