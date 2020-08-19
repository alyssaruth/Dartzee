package dartzee.screen.game.scorer

import dartzee.logging.CODE_PLAYER_PAUSED
import dartzee.logging.CODE_PLAYER_UNPAUSED
import dartzee.screen.game.GamePanelPausable
import dartzee.utils.InjectedThings.logger
import dartzee.utils.ResourceCache.ICON_PAUSE
import dartzee.utils.ResourceCache.ICON_RESUME
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton

abstract class DartsScorerPausable(private val parent: GamePanelPausable<out DartsScorerPausable>) : DartsScorer(), ActionListener
{
    private val btnResume = JButton("")

    init
    {
        btnResume.preferredSize = Dimension(30, 30)
        panelSouth.add(btnResume, BorderLayout.EAST)
        btnResume.isVisible = false
        btnResume.icon = ICON_RESUME

        btnResume.addActionListener(this)
    }

    /**
     * Abstract Methods
     */
    abstract fun playerIsFinished(): Boolean

    fun getPaused() = btnResume.icon === ICON_RESUME

    fun toggleResume()
    {
        if (btnResume.icon === ICON_PAUSE)
        {
            logger.info(CODE_PLAYER_PAUSED, "Paused player $playerId")
            btnResume.icon = ICON_RESUME
            finalisePlayerResult(finishPos)
        }
        else
        {
            logger.info(CODE_PLAYER_UNPAUSED, "Unpaused player $playerId")
            btnResume.icon = ICON_PAUSE
            lblResult.text = ""
            lblResult.background = null
        }
    }

    fun finalisePlayerResult(finishPos: Int)
    {
        this.finishPos = finishPos

        if (!playerIsFinished())
        {
            lblResult.text = "Unfinished"
            btnResume.isVisible = true
        }
        else
        {
            val dartCount = getTotalScore()
            lblResult.text = "$dartCount Darts"
            btnResume.isVisible = false
        }

        updateResultColourForPosition(finishPos)
    }

    override fun updatePlayerResult()
    {
        val dartCount = getTotalScore()
        if (dartCount == 0)
        {
            lblResult.isVisible = false
        }
        else
        {
            lblResult.isVisible = true
            lblResult.text = "$dartCount Darts"
        }
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        if (getPaused())
        {
            parent.unpauseLastPlayer()
        }
        else
        {
            parent.pauseLastPlayer()
        }

        toggleResume()
    }
}
