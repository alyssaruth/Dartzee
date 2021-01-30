package dartzee.achievements

import dartzee.achievements.x01.AchievementX01HighestBust
import dartzee.achievements.x01.TestAchievementX01HighestBust
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.helper.retrieveAchievement
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldNotContain
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementUtil: AbstractTest()
{
    @Test
    fun `Running achievement conversion should not reuse old temp tables`()
    {
        ensureX01RoundsTableExists(listOf("foo"), mainDatabase)

        val g = insertGame(gameType = GameType.X01)
        val p = insertPlayer()
        TestAchievementX01HighestBust().setUpAchievementRowForPlayerAndGame(p, g, mainDatabase)

        val t = runConversionsWithProgressBar(listOf(AchievementX01HighestBust()), listOf(p.rowId))
        t.join()

        val result = retrieveAchievement()
        result.playerId shouldBe p.rowId
        result.achievementType shouldBe AchievementType.X01_HIGHEST_BUST
        result.gameIdEarned shouldBe g.rowId
    }

    @Test
    fun `Running achievement conversion should leave no temp tables lying around`()
    {
        val t = runConversionsWithProgressBar(getAllAchievements(), emptyList())
        t.join()

        mainDatabase.dropUnexpectedTables().shouldBeEmpty()
    }

    @Test
    fun `Should return the right achievements by game type`()
    {
        val x01Refs = getAchievementsForGameType(GameType.X01).map { it.achievementType }
        val golfRefs = getAchievementsForGameType(GameType.GOLF).map { it.achievementType }

        x01Refs.shouldContain(AchievementType.X01_BEST_FINISH)
        x01Refs.shouldNotContain(AchievementType.GOLF_BEST_GAME)

        golfRefs.shouldNotContain(AchievementType.X01_BEST_FINISH)
        golfRefs.shouldContain(AchievementType.GOLF_BEST_GAME)
    }
}