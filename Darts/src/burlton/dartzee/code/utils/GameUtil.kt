package burlton.dartzee.code.utils

import burlton.core.code.util.sortedBy
import burlton.dartzee.code.achievements.getWinAchievementRef
import burlton.dartzee.code.bean.*
import burlton.dartzee.code.db.*

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

fun setFinishingPositions(participants: List<ParticipantEntity>, game: GameEntity)
{
    //If there's only one player, it's already set to -1 which is correct
    if (participants.size == 1)
    {
        return
    }

    val entries = participants.groupBy { it.finalScore }
                              .entries
                              .sortedBy(doesHighestWin(game.gameType)) { it.key }

    var finishPos = 1
    entries.forEach { (_, participants) ->
        participants.forEach { it.saveFinishingPosition(game, finishPos) }
        finishPos += participants.size
    }
}

private fun ParticipantEntity.saveFinishingPosition(game: GameEntity, position: Int)
{
    this.finishingPosition = position
    this.saveToDatabase()

    if (position == 1)
    {
        val achievementRef = getWinAchievementRef(game.gameType)
        AchievementEntity.incrementAchievement(achievementRef, playerId, game.rowId)
    }
}