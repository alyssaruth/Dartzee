package dartzee.screen.preference

import dartzee.core.util.getAllChildComponentsForType
import dartzee.utils.*
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class PreferencesPanelScorer : AbstractPreferencesPanel(), ChangeListener
{
    private val panelOops = JPanel()
    private val panelCenter = JPanel()
    private val panelScorerPreview = JPanel()
    val spinnerHueFactor = JSpinner()
    val spinnerFgBrightness = JSpinner()
    val spinnerBgBrightness = JSpinner()

    init
    {
        add(panelOops, BorderLayout.CENTER)
        panelOops.layout = MigLayout("al center center, gapy 20")
        panelOops.add(panelCenter)
        panelCenter.border = TitledBorder(null, "Colour Scheme", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        panelCenter.preferredSize = Dimension(450, 120)

        panelCenter.layout = null
        val label = JLabel("0")
        label.font = Font("Trebuchet MS", Font.BOLD, 15)
        label.horizontalAlignment = SwingConstants.CENTER
        label.preferredSize = Dimension(30, 30)
        panelScorerPreview.setBounds(20, 20, 408, 40)
        panelCenter.add(panelScorerPreview)
        panelScorerPreview.add(label)
        val label_1 = JLabel("10")
        label_1.preferredSize = Dimension(30, 30)
        label_1.horizontalAlignment = SwingConstants.CENTER
        label_1.font = Font("Trebuchet MS", Font.BOLD, 15)
        panelScorerPreview.add(label_1)
        val label_2 = JLabel("20")
        label_2.preferredSize = Dimension(30, 30)
        label_2.horizontalAlignment = SwingConstants.CENTER
        label_2.font = Font("Trebuchet MS", Font.BOLD, 15)
        panelScorerPreview.add(label_2)
        val label_3 = JLabel("30")
        label_3.preferredSize = Dimension(30, 30)
        label_3.horizontalAlignment = SwingConstants.CENTER
        label_3.font = Font("Trebuchet MS", Font.BOLD, 15)
        panelScorerPreview.add(label_3)
        val label_4 = JLabel("40")
        label_4.preferredSize = Dimension(30, 30)
        label_4.horizontalAlignment = SwingConstants.CENTER
        label_4.font = Font("Trebuchet MS", Font.BOLD, 15)
        panelScorerPreview.add(label_4)
        val label_5 = JLabel("60")
        label_5.preferredSize = Dimension(30, 30)
        label_5.horizontalAlignment = SwingConstants.CENTER
        label_5.font = Font("Trebuchet MS", Font.BOLD, 15)
        panelScorerPreview.add(label_5)
        val label_6 = JLabel("80")
        label_6.preferredSize = Dimension(30, 30)
        label_6.horizontalAlignment = SwingConstants.CENTER
        label_6.font = Font("Trebuchet MS", Font.BOLD, 15)
        panelScorerPreview.add(label_6)
        val label_7 = JLabel("100")
        label_7.preferredSize = Dimension(30, 30)
        label_7.horizontalAlignment = SwingConstants.CENTER
        label_7.font = Font("Trebuchet MS", Font.BOLD, 15)
        panelScorerPreview.add(label_7)
        val label_8 = JLabel("120")
        label_8.preferredSize = Dimension(30, 30)
        label_8.horizontalAlignment = SwingConstants.CENTER
        label_8.font = Font("Trebuchet MS", Font.BOLD, 15)
        panelScorerPreview.add(label_8)
        val label_9 = JLabel("140")
        label_9.preferredSize = Dimension(30, 30)
        label_9.horizontalAlignment = SwingConstants.CENTER
        label_9.font = Font("Trebuchet MS", Font.BOLD, 15)
        panelScorerPreview.add(label_9)
        val label_10 = JLabel("180")
        label_10.preferredSize = Dimension(30, 30)
        label_10.horizontalAlignment = SwingConstants.CENTER
        label_10.font = Font("Trebuchet MS", Font.BOLD, 15)
        panelScorerPreview.add(label_10)
        val lblHueFactor = JLabel("Hue Factor")
        lblHueFactor.setBounds(48, 70, 65, 29)
        panelCenter.add(lblHueFactor)
        spinnerHueFactor.setBounds(113, 72, 60, 25)
        panelCenter.add(spinnerHueFactor)
        spinnerHueFactor.preferredSize = Dimension(60, 25)
        spinnerHueFactor.model = SpinnerNumberModel(0.8, -1.0, 1.0, 0.05)
        val lblBrightnessFg = JLabel("FG")
        lblBrightnessFg.setBounds(193, 70, 25, 29)
        panelCenter.add(lblBrightnessFg)
        spinnerFgBrightness.setBounds(218, 72, 60, 25)
        panelCenter.add(spinnerFgBrightness)
        spinnerFgBrightness.preferredSize = Dimension(60, 25)
        spinnerFgBrightness.model = SpinnerNumberModel(0.3, 0.1, 1.0, 0.05)
        val lblBgBrightness = JLabel("BG")
        lblBgBrightness.setBounds(298, 70, 25, 29)
        panelCenter.add(lblBgBrightness)
        spinnerBgBrightness.setBounds(323, 72, 60, 25)
        panelCenter.add(spinnerBgBrightness)
        spinnerBgBrightness.preferredSize = Dimension(60, 25)
        spinnerBgBrightness.model = SpinnerNumberModel(1.0, 0.1, 1.0, 0.05)

        spinnerHueFactor.addChangeListener(this)
        spinnerBgBrightness.addChangeListener(this)
        spinnerFgBrightness.addChangeListener(this)
    }

    override fun refreshImpl(useDefaults: Boolean)
    {
        val hueFactor = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_HUE_FACTOR, useDefaults)
        val bgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_BG_BRIGHTNESS, useDefaults)
        val fgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_FG_BRIGHTNESS, useDefaults)

        spinnerHueFactor.value = hueFactor
        spinnerBgBrightness.value = bgBrightness
        spinnerFgBrightness.value = fgBrightness

        repaintScorerPreview()
    }

    private fun repaintScorerPreview()
    {
        val hueFactor = spinnerHueFactor.value as Double
        val fgBrightness = spinnerFgBrightness.value as Double
        val bgBrightness = spinnerBgBrightness.value as Double

        val scoreLabels = panelScorerPreview.getAllChildComponentsForType<JLabel>()
        for (i in scoreLabels.indices)
        {
            val scoreLabel = scoreLabels[i]
            val score = Integer.parseInt(scoreLabel.text)

            val fg = DartsColour.getScorerColour(score.toDouble(), hueFactor, fgBrightness)
            val bg = DartsColour.getScorerColour(score.toDouble(), hueFactor, bgBrightness)

            scoreLabel.isOpaque = true
            scoreLabel.foreground = fg
            scoreLabel.background = bg
            scoreLabel.repaint()
        }
    }

    override fun saveImpl()
    {
        val hueFactor = spinnerHueFactor.value as Double
        val fgBrightness = spinnerFgBrightness.value as Double
        val bgBrightness = spinnerBgBrightness.value as Double

        PreferenceUtil.saveDouble(PREFERENCES_DOUBLE_BG_BRIGHTNESS, bgBrightness)
        PreferenceUtil.saveDouble(PREFERENCES_DOUBLE_FG_BRIGHTNESS, fgBrightness)
        PreferenceUtil.saveDouble(PREFERENCES_DOUBLE_HUE_FACTOR, hueFactor)
    }

    override fun hasOutstandingChanges() =
            spinnerHueFactor.value != PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_HUE_FACTOR)
                || spinnerBgBrightness.value != PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_BG_BRIGHTNESS)
                || spinnerFgBrightness.value != PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_FG_BRIGHTNESS)

    override fun stateChanged(arg0: ChangeEvent)
    {
        repaintScorerPreview()
        stateChanged()
    }
}
