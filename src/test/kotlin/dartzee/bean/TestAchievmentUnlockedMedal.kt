package dartzee.bean

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.achievements.x01.AchievementX01HotelInspector
import dartzee.helper.AbstractTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestAchievmentUnlockedMedal : AbstractTest() {
    @Test
    @Tag("screenshot")
    fun `Should match snapshot - red`() {
        val achievement = makeAchievement(AchievementX01HotelInspector().redThreshold)
        val medal = AchievementUnlockedMedal(achievement)
        medal.shouldMatchImage("red")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - orange`() {
        val achievement = makeAchievement(AchievementX01HotelInspector().orangeThreshold)
        val medal = AchievementUnlockedMedal(achievement)
        medal.shouldMatchImage("orange")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - yellow`() {
        val achievement = makeAchievement(AchievementX01HotelInspector().yellowThreshold)
        val medal = AchievementUnlockedMedal(achievement)
        medal.shouldMatchImage("yellow")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - green`() {
        val achievement = makeAchievement(AchievementX01HotelInspector().greenThreshold)
        val medal = AchievementUnlockedMedal(achievement)
        medal.shouldMatchImage("green")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - blue`() {
        val achievement = makeAchievement(AchievementX01HotelInspector().blueThreshold)
        val medal = AchievementUnlockedMedal(achievement)
        medal.shouldMatchImage("blue")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - pink`() {
        val achievement = makeAchievement(AchievementX01HotelInspector().pinkThreshold)
        val medal = AchievementUnlockedMedal(achievement)
        medal.shouldMatchImage("pink")
    }

    private fun makeAchievement(attainedValue: Int = -1) =
        AchievementX01HotelInspector().also { it.attainedValue = attainedValue }
}
