package dartzee.screen.player

import dartzee.achievements.getGamesWonIcon
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.screen.ScreenCache
import dartzee.screen.stats.player.PlayerStatisticsScreen
import javax.swing.ImageIcon

class PlayerStatsButton(private val player: PlayerEntity,
                        private val gameType: GameType,
                        played: Int,
                        highScore: Int) : PlayerSummaryButton()
{
    private val iconUrl = getGamesWonIcon(gameType)
    override val defaultText = makeStatsText(played, highScore)
    override val hoverText = "<html><h3>${gameType.getDescription()} stats &gt;</h3></html>"

    init
    {
        icon = ImageIcon(iconUrl)
        text = defaultText

        rolloverIcon = ImageIcon(javaClass.getResource("/buttons/stats_large.png"))
        selectedIcon = ImageIcon(javaClass.getResource("/buttons/stats_large.png"))

        isEnabled = played > 0

        addActionListener(this)
        addMouseListener(this)
    }

    private fun makeStatsText(played: Int, highScore: Int): String
    {
        val lineOne = "<h3>${gameType.getDescription()}</h3>"
        val lineTwo = "<b>Played: </b> $played"
        val lineThree = "<b>Best game: </b> $highScore"
        return "<html><center>$lineOne $lineTwo<br>$lineThree</center></html>"
    }

    override fun buttonPressed()
    {
        val statsScrn = ScreenCache.get<PlayerStatisticsScreen>()
        statsScrn.setVariables(gameType, player)

        ScreenCache.switch(statsScrn)
    }
}
