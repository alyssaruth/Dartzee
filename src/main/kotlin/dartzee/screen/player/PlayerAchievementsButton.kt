package dartzee.screen.player

import dartzee.achievements.getAchievementMaximum
import dartzee.achievements.getPlayerAchievementScore
import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity
import dartzee.screen.ScreenCache
import javax.swing.ImageIcon

class PlayerAchievementsButton(
    private val player: PlayerEntity,
    private val achievementRows: List<AchievementEntity>
) : PlayerSummaryButton() {
    override val defaultText = makeDefaultText()
    override val hoverText = "<html><h3>Achievements &gt;</h3></html>"

    init {
        icon = ImageIcon(javaClass.getResource("/achievements/trophy.png"))
        text = defaultText

        rolloverIcon = ImageIcon(javaClass.getResource("/buttons/achievement.png"))
        selectedIcon = ImageIcon(javaClass.getResource("/buttons/achievement.png"))

        addActionListener(this)
        addMouseListener(this)
    }

    private fun makeDefaultText(): String {
        val score = getPlayerAchievementScore(achievementRows, player)
        val lineOne = "<h3>Achievements</h3>"
        val lineTwo = "$score / ${getAchievementMaximum()}"
        return "<html><center>$lineOne $lineTwo</center></html>"
    }

    override fun buttonPressed() {
        ScreenCache.switchToAchievementsScreen(player)
    }
}
