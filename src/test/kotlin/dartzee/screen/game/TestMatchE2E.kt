package dartzee.screen.game

import dartzee.`object`.GameLauncher
import dartzee.ai.AimDart
import dartzee.awaitCondition
import dartzee.core.util.DateStatics
import dartzee.game.GameType
import dartzee.game.MatchMode
import dartzee.helper.*
import org.junit.Test

class TestMatchE2E: AbstractTest()
{
    @Test
    fun `E2E - Two game match`()
    {
        val match = insertDartsMatch(games = 2, matchParams = "", mode = MatchMode.FIRST_TO)
        match.gameType = GameType.X01
        match.gameParams = "501"

        val aiModel = beastDartsModel(hmScoreToDart = mapOf(81 to AimDart(19, 3)))
        val winner = insertPlayer(model = aiModel)

        val loserModel = makeDartsModel(standardDeviation = 200.0)
        val loser = insertPlayer(model = loserModel)

        match.players = mutableListOf(winner, loser)

        GameLauncher().launchNewMatch(match)

        awaitCondition { match.dtFinish != DateStatics.END_OF_TIME }
    }
}