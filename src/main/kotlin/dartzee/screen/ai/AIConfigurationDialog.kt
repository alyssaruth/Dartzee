package dartzee.screen.ai

import dartzee.ai.AbstractDartsModel
import dartzee.core.bean.selectedItemTyped
import dartzee.db.PlayerEntity
import dartzee.screen.AbstractPlayerCreationDialog
import dartzee.screen.ScreenCache
import net.miginfocom.swing.MigLayout
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
    private val panelSetup = JPanel()
    private val lblName = JLabel("Name")
    private val comboBox = JComboBox<String>()
    private val tabbedPaneGameSpecifics = JTabbedPane()
    private val panelX01Config = AIConfigurationSubPanelX01()
    private val panelGolfConfig = AIConfigurationSubPanelGolf()
    private val panelDartzeeConfig = AIConfigurationSubPanelDartzee()
    private val panelSouth = JPanel()
    private val panelCalculateStats = JPanel()
    private val textFieldAverageScore = JTextField()
    private val textFieldFinishPercent = JTextField()
    private val btnCalculate = JButton("Calculate")

    private val tabbedPane = JTabbedPane(SwingConstants.TOP)
    private val scatterTab = VisualisationPanelScatter()
    private val densityTab = VisualisationPanelDensity()

    private var panelAIConfig: AbstractAIConfigurationPanel = AIConfigurationPanelNormalDistribution()
    private val btnRunSimulation = JButton("Run Simulation...")
    private val textFieldMissPercent = JTextField()
    private val lblTreble = JLabel("Treble %")
    private val textFieldTreblePercent = JTextField()

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
        panelSetup.preferredSize = Dimension(10, 100)
        panelSetup.border = TitledBorder(null, "Setup", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        panelNorth.add(panelSetup, BorderLayout.CENTER)
        panelNorth.add(panelAIConfig, BorderLayout.SOUTH)
        panelSetup.layout = MigLayout("", "[120px][150px]", "[25px][25px]")
        val lblModel = JLabel("Model")
        lblModel.preferredSize = Dimension(120, 25)
        panelSetup.add(lblModel, "cell 0 1,alignx left,aligny top")
        panelSetup.add(comboBox, "cell 1 1,grow")
        panelSetup.add(lblName, "cell 0 0,grow")
        panelSetup.add(textFieldName, "cell 1 0,grow")
        textFieldName.columns = 10

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
        comboBox.addActionListener(this)
        btnCalculate.addActionListener(this)
        btnRunSimulation.addActionListener(this)

        populateComboBox()
        initFields()
        setFieldsEditable()

        tabbedPane.isEnabled = false
    }

    private fun initFields()
    {
        avatar.init(aiPlayer, false)

        if (!aiPlayer.retrievedFromDb)
        {
            comboBox.selectedItem = AbstractDartsModel.DARTS_MODEL_NORMAL_DISTRIBUTION
            avatar.readOnly = false
            setPanelBasedOnModel(AbstractDartsModel.DARTS_MODEL_NORMAL_DISTRIBUTION)

            panelX01Config.reset()
            panelGolfConfig.reset()
            panelDartzeeConfig.reset()
            panelAIConfig.reset()
        }
        else
        {
            avatar.readOnly = true

            val name = aiPlayer.name
            textFieldName.text = name

            val strategy = aiPlayer.strategy
            val strategyDesc = AbstractDartsModel.getStrategyDesc(strategy)
            comboBox.selectedItem = strategyDesc

            val xmlStr = aiPlayer.strategyXml
            val model = AbstractDartsModel.factoryForType(strategy)
            model!!.readXml(xmlStr)

            panelAIConfig.initialiseFromModel(model)
            panelX01Config.initialiseFromModel(model)
            panelGolfConfig.initialiseFromModel(model)
            panelDartzeeConfig.initialiseFromModel(model)
        }
    }

    private fun setFieldsEditable()
    {
        val editable = !aiPlayer.retrievedFromDb

        comboBox.isEnabled = editable
        textFieldName.isEditable = editable
    }

    private fun populateComboBox()
    {
        val models = AbstractDartsModel.getModelDescriptions()
        val model = DefaultComboBoxModel(models)
        comboBox.model = model
    }

    private fun setPanelBasedOnModel(model: String)
    {
        panelNorth.remove(panelAIConfig)

        if (model == AbstractDartsModel.DARTS_MODEL_NORMAL_DISTRIBUTION)
        {
            panelAIConfig = AIConfigurationPanelNormalDistribution()
        }

        panelNorth.add(panelAIConfig, BorderLayout.SOUTH)
        revalidate()
    }

    private fun factoryModelFromPanels(): AbstractDartsModel
    {
        val model = panelAIConfig.initialiseModel()
        panelX01Config.populateModel(model)
        panelGolfConfig.populateModel(model)
        panelDartzeeConfig.populateModel(model)

        return model
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            comboBox -> setPanelBasedOnModel(comboBox.selectedItemTyped())
            btnCalculate -> if (validModel()) calculateStats()
            btnRunSimulation -> runSimulation()
            else -> super.actionPerformed(arg0)
        }
    }

    private fun runSimulation()
    {
        if (validModel())
        {
            val model = factoryModelFromPanels()

            //If the player hasn't actually been created yet, then we need to instantiate a PlayerEntity just to hold stuff like the name
            aiPlayer.name = textFieldName.text

            val dlg = AISimulationSetup(aiPlayer, model, true)
            dlg.isVisible = true
        }
    }

    override fun valid() = super.valid() && validModel()

    override fun doExistenceCheck(): Boolean
    {
        return !aiPlayer.retrievedFromDb
    }

    private fun validModel() = panelAIConfig.valid() && panelX01Config.valid()

    override fun savePlayer()
    {
        val name = textFieldName.text
        aiPlayer.name = name

        val model = factoryModelFromPanels()
        val type = model.getType()
        val xmlStr = model.writeXml()

        aiPlayer.strategy = type
        aiPlayer.strategyXml = xmlStr

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

        val averageDart = simulationWrapper.averageDart
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
