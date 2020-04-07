package dartzee.utils

import dartzee.achievements.getWinAchievementRef
import dartzee.bean.GameParamFilterPanelDartzee
import dartzee.bean.GameParamFilterPanelGolf
import dartzee.bean.GameParamFilterPanelRoundTheClock
import dartzee.bean.GameParamFilterPanelX01
import dartzee.core.util.sortedBy
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.game.GameType

fun getGameDesc(gameType: GameType, gameParams: String) =
    when(gameType)
    {
        GameType.X01 -> gameParams
        GameType.GOLF -> "Golf - $gameParams holes"
        GameType.ROUND_THE_CLOCK -> "Round the Clock - $gameParams"
        GameType.DARTZEE -> "Dartzee"
    }

fun getFilterPanel(gameType: GameType) =
    when (gameType)
    {
        GameType.X01 -> GameParamFilterPanelX01()
        GameType.GOLF -> GameParamFilterPanelGolf()
        GameType.ROUND_THE_CLOCK -> GameParamFilterPanelRoundTheClock()
        GameType.DARTZEE -> GameParamFilterPanelDartzee()
    }

fun doesHighestWin(gameType: GameType?) =
    when (gameType)
    {
        GameType.DARTZEE -> true
        GameType.X01, GameType.GOLF, GameType.ROUND_THE_CLOCK, null -> false
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