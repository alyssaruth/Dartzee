package dartzee.screen

import dartzee.db.PlayerEntity
import dartzee.screen.stats.player.PlayerStatisticsScreen
import dartzee.stats.PlayerSummaryStats
import dartzee.utils.getTypeDesc
import net.miginfocom.swing.MigLayout
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.border.TitledBorder

class PlayerSummaryPanel(private val gameType: Int) : JPanel(), ActionListener
{
    private var player: PlayerEntity? = null

    private val nfGamesPlayed = JTextField()
    private val nfGamesWon = JTextField()
    private val nfBestGame = JTextField()
    private val btnViewStats = JButton("View Stats")
    private val lblP = JLabel("Played")
    private val lblW = JLabel("Won")
    private val lblHighScore = JLabel("High score")

    init
    {
        border = TitledBorder(null, getTypeDesc(gameType), TitledBorder.LEADING, TitledBorder.TOP, Font("Tahoma", Font.PLAIN, 20))
        layout = MigLayout("", "[][][][][][][][][grow][]", "[][][]")

        add(lblP, "cell 0 0")

        add(lblW, "cell 2 0,alignx leading")

        add(lblHighScore, "cell 4 0")
        nfGamesPlayed.isEditable = false
        add(nfGamesPlayed, "cell 0 1,growx")
        nfGamesPlayed.columns = 10
        val horizontalStrut = Box.createHorizontalStrut(20)
        add(horizontalStrut, "cell 1 1")
        nfGamesWon.isEditable = false
        add(nfGamesWon, "cell 2 1,growx")
        nfGamesWon.columns = 10

        val strutOne = Box.createHorizontalStrut(20)
        add(strutOne, "cell 3 1")
        nfBestGame.isEditable = false
        add(nfBestGame, "cell 4 1,growx")
        nfBestGame.columns = 10

        val strut2 = Box.createHorizontalStrut(20)
        add(strut2, "flowx,cell 8 1")
        btnViewStats.font = Font("Tahoma", Font.PLAIN, 16)
        add(btnViewStats, "cell 8 1,alignx center")

        btnViewStats.addActionListener(this)
    }

    fun init(player: PlayerEntity)
    {
        isVisible = true

        this.player = player

        val stats = PlayerSummaryStats.getSummaryStats(player, gameType)

        val gamesPlayed = stats.gamesPlayed
        val gamesWon = stats.gamesWon
        val bestScore = stats.bestScore

        nfGamesPlayed.text = "" + gamesPlayed
        nfGamesWon.text = "" + gamesWon

        if (bestScore > 0)
        {
            nfBestGame.text = "$bestScore"
        }
        else
        {
            nfBestGame.text = ""
        }

        btnViewStats.isEnabled = gamesPlayed > 0
    }


    override fun actionPerformed(arg0: ActionEvent)
    {
        if (arg0.source === btnViewStats)
        {
            val statsScrn = ScreenCache.getScreen(PlayerStatisticsScreen::class.java)
            statsScrn.setVariables(gameType, player!!)

            ScreenCache.switchScreen(statsScrn)
        }
    }
}
