package dartzee.game

import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.PlayerEntity

data class GameLaunchParams(
    val players: List<PlayerEntity>,
    val gameType: GameType,
    val gameParams: String,
    val pairMode: Boolean,
    val dartzeeDtos: List<DartzeeRuleDto>? = null,
) {
    fun teamCount(): Int = if (pairMode) players.chunked(2).size else players.size
}
