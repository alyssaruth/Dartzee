package dartzee.achievements.rtc

import dartzee.achievements.TestAbstractAchievementGamesWon
import dartzee.game.GameType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementClockGamesWon : TestAbstractAchievementGamesWon<AchievementClockGamesWon>() {
    override fun factoryAchievement() = AchievementClockGamesWon()

    @Test
    fun `Game type should be correct`() {
        factoryAchievement().gameType shouldBe GameType.ROUND_THE_CLOCK
    }
}
