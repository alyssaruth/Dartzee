package dartzee.game

import dartzee.db.DartzeeTemplateEntity

enum class GameType
{
    X01,
    GOLF,
    ROUND_THE_CLOCK,
    DARTZEE;

    fun getDescription(): String =
        when (this)
        {
            X01 -> "X01"
            GOLF -> "Golf"
            ROUND_THE_CLOCK -> "Round the Clock"
            DARTZEE -> "Dartzee"
        }

    fun getDescription(gameParams: String): String
    {
        val paramDesc = getParamsDescription(gameParams)
        return when (this)
        {
            X01 -> paramDesc
            GOLF -> "Golf - $paramDesc"
            ROUND_THE_CLOCK -> "Round the Clock - $paramDesc"
            DARTZEE -> if (paramDesc.isEmpty()) "Dartzee" else "Dartzee - $paramDesc"
        }
    }

    fun getParamsDescription(gameParams: String) =
        when (this)
        {
            X01 -> gameParams
            GOLF -> "$gameParams holes"
            ROUND_THE_CLOCK -> RoundTheClockConfig.fromJson(gameParams).getDescription()
            DARTZEE -> DartzeeTemplateEntity().retrieveForId(gameParams, false)?.name ?: ""
        }
}