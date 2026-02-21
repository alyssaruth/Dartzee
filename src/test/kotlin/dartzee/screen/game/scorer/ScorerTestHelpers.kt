package dartzee.screen.game.scorer

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.getChild
import javax.swing.JButton
import javax.swing.JLabel

fun AbstractDartsScorer<*>.findAchievementOverlay() = findChild<AchievementOverlay>()

fun AbstractDartsScorer<*>.getAchievementOverlay() = getChild<AchievementOverlay>()

fun AchievementOverlay.getAchievementName(): String = getChild<JLabel>("achievementName").text

fun AchievementOverlay.getPlayerName(): String = getChild<JLabel>("playerName").text

fun AchievementOverlay.close() = clickChild<JButton>(text = "X")
