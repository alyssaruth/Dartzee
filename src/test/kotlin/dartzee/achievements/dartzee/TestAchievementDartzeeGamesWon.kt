package dartzee.achievements.dartzee

import dartzee.achievements.TestAbstractAchievementGamesWon
import dartzee.game.GameType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementDartzeeGamesWon: TestAbstractAchievementGamesWon<AchievementDartzeeGamesWon>()
{
    override fun factoryAchievement() = AchievementDartzeeGamesWon()

    @Test
    fun `Game type should be correct`()
    {
        factoryAchievement().gameType shouldBe GameType.DARTZEE
    }
}