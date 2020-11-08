package dartzee.screen.game.scorer

import com.github.alexburlton.swingtest.clickChild
import com.github.alexburlton.swingtest.findChild
import com.github.alexburlton.swingtest.getChild
import dartzee.core.bean.SwingLabel
import javax.swing.JButton

fun AbstractDartsScorer<*>.getAchievementOverlay() = findChild<AbstractDartsScorer<*>.AchievementOverlay>()
fun AbstractDartsScorer<*>.AchievementOverlay.getAchievementName() = getChild<SwingLabel> { it.testId == "achievementName" }.text
fun AbstractDartsScorer<*>.AchievementOverlay.close() = clickChild<JButton>("X")