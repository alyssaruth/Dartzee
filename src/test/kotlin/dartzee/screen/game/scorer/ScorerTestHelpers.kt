package dartzee.screen.game.scorer

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.getChild
import dartzee.core.bean.SwingLabel
import javax.swing.JButton

fun AbstractDartsScorer<*>.findAchievementOverlay() = findChild<AchievementOverlay>()
fun AbstractDartsScorer<*>.getAchievementOverlay() = getChild<AchievementOverlay>()
fun AchievementOverlay.getAchievementName(): String = getChild<SwingLabel> { it.testId == "achievementName" }.text
fun AchievementOverlay.getPlayerName(): String = getChild<SwingLabel> { it.testId == "playerName"}.text
fun AchievementOverlay.close() = clickChild<JButton>("X")