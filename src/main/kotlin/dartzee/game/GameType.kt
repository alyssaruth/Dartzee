package dartzee.game

import dartzee.db.DartzeeTemplateEntity

const val GAME_PARAMS_NOT_APPLICABLE = "N/A"

enum class GameType {
    X01,
    GOLF,
    ROUND_THE_CLOCK,
    DARTZEE;

    fun getDescription(): String =
        when (this) {
            X01 -> "X01"
            GOLF -> "Golf"
            ROUND_THE_CLOCK -> "Round the Clock"
            DARTZEE -> "Dartzee"
        }

    fun getDescription(gameParams: String): String {
        val paramDesc = getParamsDescription(gameParams)
        return when (this) {
            X01 -> paramDesc
            GOLF -> "Golf - $paramDesc"
            ROUND_THE_CLOCK -> "Round the Clock - $paramDesc"
            DARTZEE -> if (paramDesc.isEmpty()) "Dartzee" else "Dartzee - $paramDesc"
        }
    }

    fun getParamsDescription(gameParams: String): String {
        if (gameParams == GAME_PARAMS_NOT_APPLICABLE) {
            return GAME_PARAMS_NOT_APPLICABLE
        }

        return when (this) {
            X01 -> X01Config.fromJson(gameParams).description()
            GOLF -> "$gameParams holes"
            ROUND_THE_CLOCK -> RoundTheClockConfig.fromJson(gameParams).getDescription()
            DARTZEE -> DartzeeTemplateEntity().retrieveForId(gameParams, false)?.name.orEmpty()
        }
    }
}
