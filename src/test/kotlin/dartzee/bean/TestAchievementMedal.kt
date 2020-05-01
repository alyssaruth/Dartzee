package dartzee.bean

import dartzee.achievements.x01.AchievementX01BestThreeDarts
import dartzee.helper.AbstractTest
import dartzee.helper.shouldMatchImage
import org.junit.Test

class TestAchievementMedal: AbstractTest()
{
    @Test
    fun `Should match snapshot - locked`()
    {
        val achievement = makeAchievement(-1)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("locked")
    }

    @Test
    fun `Should match snapshot - red`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().redThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("red")
    }

    @Test
    fun `Should match snapshot - orange`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().orangeThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("orange")
    }

    @Test
    fun `Should match snapshot - yellow`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().yellowThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("yellow")
    }

    @Test
    fun `Should match snapshot - green`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().greenThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("green")
    }

    @Test
    fun `Should match snapshot - blue`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().blueThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("blue")
    }

    @Test
    fun `Should match snapshot - pink`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().pinkThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("pink")
    }

    private fun makeAchievement(attainedValue: Int = -1) = AchievementX01BestThreeDarts().also { it.attainedValue = attainedValue }
}