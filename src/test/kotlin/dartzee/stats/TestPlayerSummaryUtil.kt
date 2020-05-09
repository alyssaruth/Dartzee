package dartzee.stats

import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertGameForPlayer
import dartzee.helper.insertPlayer
import io.kotlintest.shouldBe
import org.junit.Test

class TestPlayerSummaryUtil: AbstractTest()
{
    @Test
    fun `Should return the counts by game type for a particular player`()
    {
        val playerA = insertPlayer()
        val playerB = insertPlayer()

        insertGameForPlayer(playerA, GameType.X01)
        insertGameForPlayer(playerA, GameType.X01)
        insertGameForPlayer(playerA, GameType.GOLF)

        insertGameForPlayer(playerB, GameType.X01)
        insertGameForPlayer(playerB, GameType.ROUND_THE_CLOCK)

        val aCounts = getGameCounts(playerA)
        aCounts.getCount(GameType.X01) shouldBe 2
        aCounts.getCount(GameType.GOLF) shouldBe 1
        aCounts.getCount(GameType.ROUND_THE_CLOCK) shouldBe 0

        val bCounts = getGameCounts(playerB)
        bCounts.getCount(GameType.X01) shouldBe 1
        bCounts.getCount(GameType.GOLF) shouldBe 0
        bCounts.getCount(GameType.ROUND_THE_CLOCK) shouldBe 1
    }
}