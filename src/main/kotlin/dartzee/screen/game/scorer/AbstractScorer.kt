package dartzee.screen.game.scorer

import dartzee.bean.ParticipantAvatar
import dartzee.bean.ScrollTableDartsGame
import dartzee.core.util.TableUtil.DefaultModel
import dartzee.game.state.IWrappedParticipant
import dartzee.utils.DartsColour
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder

abstract class AbstractScorer(val participant: IWrappedParticipant) : JPanel(), IScorerTable {
    val playerIds = participant.individuals.map { it.playerId }

    override val model = DefaultModel()

    val lblName = JLabel()
    protected val panelCenter = JPanel()
    val tableScores = ScrollTableDartsGame()
    val lblResult = JLabel("")
    protected val panelNorth = JPanel()
    val lblAvatar = ParticipantAvatar(participant)
    val panelAvatar = JPanel()
    protected val panelSouth = JPanel()

    val playerName: String
        get() = lblName.text

    init {
        layout = BorderLayout(0, 0)
        preferredSize = Dimension(180, 600)

        panelCenter.layout = BorderLayout(0, 0)

        panelCenter.add(tableScores, BorderLayout.CENTER)
        add(panelCenter, BorderLayout.CENTER)
        add(panelNorth, BorderLayout.NORTH)
        panelNorth.layout = BorderLayout(0, 0)
        panelNorth.add(lblName, BorderLayout.NORTH)
        lblName.horizontalAlignment = SwingConstants.CENTER
        lblName.font = Font("Trebuchet MS", Font.PLAIN, 16)
        lblName.text = participant.getParticipantNameHtml(false)
        lblName.foreground = Color.BLACK
        lblName.border = EmptyBorder(10, 0, 0, 0)
        panelAvatar.border = EmptyBorder(5, 15, 5, 15)
        panelNorth.add(panelAvatar, BorderLayout.CENTER)
        panelAvatar.layout = BorderLayout(0, 0)
        panelAvatar.add(lblAvatar, BorderLayout.NORTH)
        add(panelSouth, BorderLayout.SOUTH)
        panelSouth.layout = BorderLayout(0, 0)
        panelSouth.add(lblResult)
        lblResult.isOpaque = true
        lblResult.font = Font("Trebuchet MS", Font.PLAIN, 22)
        lblResult.horizontalAlignment = SwingConstants.CENTER

        tableScores.setFillsViewportHeight(false)
        tableScores.setShowRowCount(false)
        tableScores.disableSorting()
    }

    protected abstract fun initImpl()

    fun getTableOnly() = panelCenter

    fun init() {
        lblResult.text = ""

        // TableModel
        tableScores.setRowHeight(25)
        repeat(getNumberOfColumns()) { model.addColumn("") }
        tableScores.model = model

        initImpl()
    }

    protected fun updateResultColourForPosition(pos: Int) {
        DartsColour.setFgAndBgColoursForPosition(lblResult, pos)
    }
}
