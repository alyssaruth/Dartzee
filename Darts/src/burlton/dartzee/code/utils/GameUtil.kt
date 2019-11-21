package burlton.dartzee.code.utils

import burlton.dartzee.code.bean.*
import burlton.dartzee.code.db.GAME_TYPE_DARTZEE
import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.code.db.GAME_TYPE_ROUND_THE_CLOCK
import burlton.dartzee.code.db.GAME_TYPE_X01

fun getGameDesc(gameType: Int, gameParams: String): String
{
    return when(gameType)
    {
        GAME_TYPE_X01 -> gameParams
        GAME_TYPE_GOLF -> "Golf - $gameParams holes"
        GAME_TYPE_ROUND_THE_CLOCK -> "Round the Clock - $gameParams"
        GAME_TYPE_DARTZEE -> "Dartzee"
        else -> ""
    }
}

fun getTypeDesc(gameType: Int): String
{
    return when (gameType)
    {
        GAME_TYPE_X01 -> "X01"
        GAME_TYPE_GOLF -> "Golf"
        GAME_TYPE_ROUND_THE_CLOCK -> "Round the Clock"
        GAME_TYPE_DARTZEE -> "Dartzee"
        else -> "<Game Type>"
    }
}

fun getFilterPanel(gameType: Int): GameParamFilterPanel
{
    return when (gameType)
    {
        GAME_TYPE_X01 -> GameParamFilterPanelX01()
        GAME_TYPE_GOLF -> GameParamFilterPanelGolf()
        GAME_TYPE_ROUND_THE_CLOCK -> GameParamFilterPanelRoundTheClock()
        else -> GameParamFilterPanelDartzee()
    }
}

fun doesHighestWin(gameType: Int): Boolean
{
    return when (gameType)
    {
        GAME_TYPE_DARTZEE -> true
        else -> false
    }
}

fun getAllGameTypes(): MutableList<Int>
{
    return mutableListOf(GAME_TYPE_X01, GAME_TYPE_GOLF, GAME_TYPE_ROUND_THE_CLOCK, GAME_TYPE_DARTZEE)
}