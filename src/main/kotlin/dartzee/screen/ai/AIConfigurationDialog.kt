package dartzee.screen.ai

import dartzee.ai.DartsAiModel
import dartzee.core.bean.addGhostText
import dartzee.core.util.MathsUtil
import dartzee.core.util.setFontSize
import dartzee.db.PlayerEntity
import dartzee.screen.AbstractPlayerCreationDialog
import dartzee.screen.ScreenCache
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.SoftBevelBorder
import javax.swing.border.TitledBorder

class AIConfigurationDialog(private val aiPlayer: PlayerEntity = PlayerEntity.factoryCreate()) : AbstractPlayerCreationDialog()
{
    private val panelScreen = JPanel()
    private val panelNorth = JPanel()
    private val panelName = JPanel()
    private val tabbedPaneGameSpecifics = JTabbedPane()
    private val panelX01Config = AIConfigurationSubPanelX01()
    private val panelGolfConfig = AIConfigurationSubPanelGolf()
    private val panelDartzeeConfig = AIConfigurationSubPanelDartzee()
    private val panelSouth = JPanel()
    private val panelCalculateStats = JPanel()
    val textFieldAverageScore = JTextField()
    val textFieldFinishPercent = JTextField()
    private val btnCalculate = JButton("Calculate")

    private val tabbedPane = JTabbedPane(SwingConstants.TOP)
    private val scatterTab = VisualisationPanelScatter()
    private val densityTab = VisualisationPanelDensity()

    private val panelAIConfig = AIConfigurationPanelNormalDistribution()
    private val btnRunSimulation = JButton("Run Simulation...")
    val textFieldMissPercent = JTextField()
    private val lblTreble = JLabel("Treble %")
    val textFieldTreblePercent = JTextField()

    init
    {
        title = "Configure AI"
        setSize(1100, 720)
        isResizable = false
        isModal = true
        contentPane.add(panelSouth, BorderLayout.EAST)
        panelSouth.border = null
        panelSouth.layout = BorderLayout(0, 0)
        panelCalculateStats.border = TitledBorder(null, "Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        panelSouth.add(panelCalculateStats, BorderLayout.NORTH)
        panelCalculateStats.layout = BorderLayout(0, 0)

        val panel = JPanel()
        panelCalculateStats.add(panel, BorderLayout.NORTH)
        val lblAverageScore = JLabel("Average Score")
        panel.add(lblAverageScore)
        panel.add(textFieldAverageScore)
        textFieldAverageScore.isEditable = false
        textFieldAverageScore.columns = 5
        val lblMiss = JLabel("Miss %")
        panel.add(lblMiss)
        textFieldMissPercent.isEditable = false
        textFieldMissPercent.columns = 5
        panel.add(textFieldMissPercent)
        panel.add(lblTreble)
        textFieldTreblePercent.isEditable = false
        textFieldTreblePercent.columns = 5
        panel.add(textFieldTreblePercent)
        val lblDouble = JLabel("Double %")
        panel.add(lblDouble)
        lblDouble.border = EmptyBorder(0, 10, 0, 0)
        panel.add(textFieldFinishPercent)
        textFieldFinishPercent.isEditable = false
        textFieldFinishPercent.columns = 5
        val panelCalculate = JPanel()
        panelCalculateStats.add(panelCalculate, BorderLayout.SOUTH)
        panelCalculate.add(btnCalculate)
        panelCalculate.add(btnRunSimulation)
        panelSouth.add(tabbedPane, BorderLayout.CENTER)
        tabbedPane.border = SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null)
        tabbedPane.preferredSize = Dimension(600, 5)
        tabbedPane.minimumSize = Dimension(600, 5)
        tabbedPane.addTab("Scatter", null, scatterTab, null)
        tabbedPane.addTab("Density", null, densityTab, null)
        contentPane.add(panelScreen, BorderLayout.CENTER)
        panelScreen.layout = BorderLayout(0, 0)
        panelScreen.add(panelNorth, BorderLayout.NORTH)
        panelNorth.layout = BorderLayout(0, 0)
        panelName.preferredSize = Dimension(10, 100)
        panelNorth.add(panelName, BorderLayout.CENTER)
        panelNorth.add(panelAIConfig, BorderLayout.SOUTH)
        panelName.layout = BorderLayout()
        panelName.add(textFieldName)
        panelName.border = EmptyBorder(40, 15, 40, 15)
        textFieldName.columns = 10
        textFieldName.preferredSize = Dimension(10, 50)
        textFieldName.setFontSize(20)
        textFieldName.addGhostText("Player name")

        val panelAvatar = JPanel()
        panelNorth.add(panelAvatar, BorderLayout.WEST)
        panelAvatar.layout = BorderLayout(0, 0)
        panelAvatar.add(avatar, BorderLayout.NORTH)
        tabbedPaneGameSpecifics.border = TitledBorder(null, "Strategy", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        panelScreen.add(tabbedPaneGameSpecifics, BorderLayout.CENTER)

        tabbedPaneGameSpecifics.addTab("X01", panelX01Config)
        tabbedPaneGameSpecifics.addTab("Golf", panelGolfConfig)
        tabbedPaneGameSpecifics.addTab("Dartzee", panelDartzeeConfig)

        //Listeners
        btnCalculate.addActionListener(this)
        btnRunSimulation.addActionListener(this)

        initFields()

        tabbedPane.isEnabled = false
    }

    private fun initFields()
    {
        avatar.init(aiPlayer, false)

        if (!aiPlayer.retrievedFromDb)
        {
            avatar.readOnly = false
            textFieldName.isEditable = true

            panelX01Config.reset()
            panelGolfConfig.reset()
            panelDartzeeConfig.reset()
            panelAIConfig.reset()
        }
        else
        {
            avatar.readOnly = true
            textFieldName.isEditable = false

            val name = aiPlayer.name
            textFieldName.text = name

            val model = DartsAiModel.fromJson(aiPlayer.strategy)

            panelAIConfig.initialiseFromModel(model)
            panelX01Config.initialiseFromModel(model)
            panelGolfConfig.initialiseFromModel(model)
            panelDartzeeConfig.initialiseFromModel(model)
        }
    }

    private fun factoryModelFromPanels(): DartsAiModel
    {
        var model = panelAIConfig.initialiseModel()
        model = panelX01Config.populateModel(model)
        model = panelGolfConfig.populateModel(model)
        model = panelDartzeeConfig.populateModel(model)

        return model
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnCalculate -> calculateStats()
            btnRunSimulation -> runSimulation()
            else -> super.actionPerformed(arg0)
        }
    }

    private fun runSimulation()
    {
        val model = factoryModelFromPanels()

        //If the player hasn't actually been created yet, then we need to instantiate a PlayerEntity just to hold stuff like the name
        aiPlayer.name = textFieldName.text

        val dlg = AISimulationSetupDialog(aiPlayer, model, true)
        dlg.isVisible = true
    }

    override fun doExistenceCheck(): Boolean
    {
        return !aiPlayer.retrievedFromDb
    }

    override fun savePlayer()
    {
        val name = textFieldName.text
        aiPlayer.name = name

        val model = factoryModelFromPanels()
        aiPlayer.strategy = model.toJson()

        val avatarId = avatar.avatarId
        aiPlayer.playerImageId = avatarId

        aiPlayer.saveToDatabase()

        createdPlayer = true

        //Now dispose the window
        dispose()
    }

    private fun calculateStats()
    {
        val model = factoryModelFromPanels()

        val dartboard = scatterTab.dartboard
        val simulationWrapper = model.runSimulation(dartboard)

        val averageDart = MathsUtil.round(simulationWrapper.averageDart, 2)
        textFieldAverageScore.text = "" + averageDart

        val missPercent = simulationWrapper.missPercent
        textFieldMissPercent.text = "" + missPercent

        val treblePercent = simulationWrapper.treblePercent
        textFieldTreblePercent.text = "" + treblePercent

        val finishPercent = simulationWrapper.finishPercent
        textFieldFinishPercent.text = "" + finishPercent

        btnCalculate.text = "Re-calculate"

        tabbedPane.isEnabled = true
        scatterTab.populate(simulationWrapper.hmPointToCount, model)
        densityTab.populate(simulationWrapper.hmPointToCount, model)
    }

    companion object
    {
        fun amendPlayer(player: PlayerEntity)
        {
            val dialog = AIConfigurationDialog(player)
            dialog.setLocationRelativeTo(ScreenCache.mainScreen)
            dialog.isVisible = true
        }
    }
}
