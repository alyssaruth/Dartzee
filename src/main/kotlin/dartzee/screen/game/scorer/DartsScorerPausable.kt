package dartzee.screen.game.scorer

import dartzee.core.util.DateStatics
import dartzee.game.state.AbstractPlayerState
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

abstract class DartsScorerPausable<PlayerState: AbstractPlayerState<PlayerState>>(private val parent: GamePanelPausable<*, *>) : DartsScorer<PlayerState>(), ActionListener
{
    private val btnResume = JButton("")
    private var latestState: PlayerState? = null

    init
    {
        btnResume.preferredSize = Dimension(30, 30)
        panelSouth.add(btnResume, BorderLayout.EAST)
        btnResume.isVisible = false
        btnResume.icon = ICON_RESUME

        btnResume.addActionListener(this)
    }

    fun getPaused() = btnResume.icon === ICON_RESUME

    override fun stateChanged(state: PlayerState) {
        super.stateChanged(state)
        latestState = state
    }

    fun toggleResume()
    {
        if (btnResume.icon === ICON_PAUSE)
        {
            logger.info(CODE_PLAYER_PAUSED, "Paused player $playerId")
            btnResume.icon = ICON_RESUME
            finalisePlayerResult()
        }
        else
        {
            logger.info(CODE_PLAYER_UNPAUSED, "Unpaused player $playerId")
            btnResume.icon = ICON_PAUSE
            lblResult.text = ""
            lblResult.background = null
        }
    }

    fun finalisePlayerResult()
    {
        val state = latestState ?: return


        if (state.pt.dtFinished == DateStatics.END_OF_TIME)
        {
            lblResult.text = "Unfinished"
            btnResume.isVisible = true
        }
        else
        {
            val dartCount = state.getScoreSoFar()
            lblResult.text = "$dartCount Darts"
            btnResume.isVisible = false
        }

        updateResultColourForPosition(state.pt.finishingPosition)
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
