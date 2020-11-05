package dartzee.achievements

import dartzee.achievements.x01.AchievementX01BestFinish
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldNotContain
import org.junit.Test

class TestAchievementUtil: AbstractTest()
{
    @Test
    fun `Running achievement conversion should leave no temp tables lying around`()
    {
        runConversionsWithProgressBar(getAllAchievements(), emptyList())

        mainDatabase.dropUnexpectedTables().shouldBeEmpty()
    }

    @Test
    fun `Should return the right achievements by game type`()
    {
        val x01Refs = getAchievementsForGameType(GameType.X01).map { it.achievementRef }
        val golfRefs = getAchievementsForGameType(GameType.GOLF).map { it.achievementRef }

        x01Refs.shouldContain(ACHIEVEMENT_REF_X01_BEST_FINISH)
        x01Refs.shouldNotContain(ACHIEVEMENT_REF_GOLF_BEST_GAME)

        golfRefs.shouldNotContain(ACHIEVEMENT_REF_X01_BEST_FINISH)
        golfRefs.shouldContain(ACHIEVEMENT_REF_GOLF_BEST_GAME)
    }
}