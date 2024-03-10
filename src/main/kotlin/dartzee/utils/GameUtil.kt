package dartzee.utils

import dartzee.bean.GameParamFilterPanelDartzee
import dartzee.bean.GameParamFilterPanelGolf
import dartzee.bean.GameParamFilterPanelRoundTheClock
import dartzee.bean.GameParamFilterPanelX01
import dartzee.core.util.sortedBy
import dartzee.db.GameEntity
import dartzee.db.IParticipant
import dartzee.game.GameType

fun getFilterPanel(gameType: GameType) =
    when (gameType) {
        GameType.X01 -> GameParamFilterPanelX01()
        GameType.GOLF -> GameParamFilterPanelGolf()
        GameType.ROUND_THE_CLOCK -> GameParamFilterPanelRoundTheClock()
        GameType.DARTZEE -> GameParamFilterPanelDartzee()
    }

fun doesHighestWin(gameType: GameType?) =
    when (gameType) {
        GameType.DARTZEE -> true
        GameType.X01,
        GameType.GOLF,
        GameType.ROUND_THE_CLOCK,
        null -> false
    }

fun setFinishingPositions(participants: List<IParticipant>, game: GameEntity) {
    // If there's only one player, it's already set to -1 which is correct
    if (participants.size == 1) {
        return
    }

    val entries =
        participants
            .filterNot { it.resigned }
            .groupBy { it.finalScore }
            .entries
            .sortedBy(doesHighestWin(game.gameType)) { it.key }

    var finishPos = 1
    entries.forEach { (_, participants) ->
        participants.forEach {
            it.finishingPosition = finishPos
            it.saveToDatabase()
        }
        finishPos += participants.size
    }
}
