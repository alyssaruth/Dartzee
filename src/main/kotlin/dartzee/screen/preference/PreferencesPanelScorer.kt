package dartzee.screen.preference

import dartzee.core.util.getAllChildComponentsForType
import dartzee.utils.*
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class PreferencesPanelScorer : AbstractPreferencesPanel(), ChangeListener
{
    private val panelCenter = JPanel()
    private val panelX01Colours = JPanel()
    private val panelScorerPreview = JPanel()

    private val panelOptions = JPanel()

    val spinnerHueFactor = JSpinner()
    val spinnerFgBrightness = JSpinner()
    val spinnerBgBrightness = JSpinner()

    init
    {
        add(panelCenter, BorderLayout.CENTER)
        panelCenter.layout = MigLayout("al center center, gapy 20")
        panelCenter.add(panelX01Colours)
        panelX01Colours.border = TitledBorder(null, "X01 Colour Scheme", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        panelX01Colours.preferredSize = Dimension(600, 160)
        panelX01Colours.layout = MigLayout("al center center, wrap, gapy 20")

        panelX01Colours.add(panelScorerPreview)
        panelX01Colours.add(panelOptions)
        panelScorerPreview.layout = FlowLayout()

        panelScorerPreview.add(makeScoreLabel(0))
        panelScorerPreview.add(makeScoreLabel(10))
        panelScorerPreview.add(makeScoreLabel(20))
        panelScorerPreview.add(makeScoreLabel(30))
        panelScorerPreview.add(makeScoreLabel(40))
        panelScorerPreview.add(makeScoreLabel(60))
        panelScorerPreview.add(makeScoreLabel(80))
        panelScorerPreview.add(makeScoreLabel(100))
        panelScorerPreview.add(makeScoreLabel(120))
        panelScorerPreview.add(makeScoreLabel(140))
        panelScorerPreview.add(makeScoreLabel(180))

        panelOptions.layout = FlowLayout()
        val lblHueFactor = JLabel("Hue Factor")
        panelOptions.add(lblHueFactor)
        panelOptions.add(spinnerHueFactor)
        spinnerHueFactor.preferredSize = Dimension(60, 25)
        spinnerHueFactor.model = SpinnerNumberModel(0.8, -1.0, 1.0, 0.05)
        val lblBrightnessFg = JLabel("FG")
        panelOptions.add(lblBrightnessFg)
        panelOptions.add(spinnerFgBrightness)
        spinnerFgBrightness.preferredSize = Dimension(60, 25)
        spinnerFgBrightness.model = SpinnerNumberModel(0.3, 0.1, 1.0, 0.05)
        val lblBgBrightness = JLabel("BG")
        panelOptions.add(lblBgBrightness)
        panelOptions.add(spinnerBgBrightness)
        spinnerBgBrightness.preferredSize = Dimension(60, 25)
        spinnerBgBrightness.model = SpinnerNumberModel(1.0, 0.1, 1.0, 0.05)

        spinnerHueFactor.addChangeListener(this)
        spinnerBgBrightness.addChangeListener(this)
        spinnerFgBrightness.addChangeListener(this)
    }

    private fun makeScoreLabel(score: Int): JLabel
    {
        val label = JLabel("$score")
        label.font = Font("Trebuchet MS", Font.BOLD, 15)
        label.horizontalAlignment = SwingConstants.CENTER
        label.preferredSize = Dimension(40, 40)
        return label
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
