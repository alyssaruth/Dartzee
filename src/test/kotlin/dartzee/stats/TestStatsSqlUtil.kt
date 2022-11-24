package dartzee.stats

import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.insertGameForPlayer
import dartzee.helper.insertIntoDatabase
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.randomGuid
import dartzee.`object`.Dart
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestStatsSqlUtil : AbstractTest()
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

    @Test
    fun `Should retrieve a single game wrapper successfully`()
    {
        val game = insertGame(gameType = GameType.GOLF, gameParams = "9")
        val player = insertPlayer()
        val pt = insertParticipant(playerId = player.rowId, gameId = game.rowId)

        val rounds = listOf(
            listOf(Dart(1, 0), Dart(1, 3)),
            listOf(Dart(17, 1), Dart(15, 1), Dart(15, 2)),
            listOf(Dart(3, 2))
        )
        rounds.insertIntoDatabase(player, pt)

        retrieveGameData(player.rowId, GameType.X01).size shouldBe 0
        retrieveGameData(randomGuid(), GameType.GOLF).size shouldBe 0

        val map = retrieveGameData(player.rowId, GameType.GOLF)
        val wrapper = map[game.localId]!!
        wrapper.gameParams shouldBe "9"
        wrapper.finalScore shouldBe -1
        wrapper.dartEntities.size shouldBe 6
    }
}
