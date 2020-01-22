package dartzee.screen.preference

import dartzee.bean.SliderAiSpeed
import dartzee.utils.*
import dartzee.core.bean.NumberField
import net.miginfocom.swing.MigLayout
import javax.swing.JCheckBox
import javax.swing.JLabel

class PreferencesPanelMisc : AbstractPreferencesPanel()
{

    private val lblDefaultAiSpeed = JLabel("Default AI speed")
    val slider = SliderAiSpeed(false)
    val chckbxAiAutomaticallyFinish = JCheckBox("AI automatically finish")
    val chckbxCheckForUpdates = JCheckBox("Automatically check for updates")
    private val lblRowsToShow = JLabel("Rows to show on Leaderboards")
    val nfLeaderboardSize = NumberField(10, 200)
    val chckbxShowAnimations = JCheckBox("Play sounds/animations")
    val chckbxPreloadResources = JCheckBox("Pre-load resources (recommended)")

    init
    {
        nfLeaderboardSize.columns = 10
        layout = MigLayout("", "[][grow][]", "[][][][][][]")

        add(lblDefaultAiSpeed, "cell 0 0")
        add(slider, "cell 1 0")

        add(lblRowsToShow, "cell 0 1,alignx leading")

        add(nfLeaderboardSize, "cell 1 1,alignx leading")
        add(chckbxAiAutomaticallyFinish, "flowx,cell 0 2")

        add(chckbxCheckForUpdates, "flowx,cell 0 3")

        add(chckbxShowAnimations, "cell 0 4")

        add(chckbxPreloadResources, "cell 0 5")
    }

    override fun refresh(useDefaults: Boolean)
    {
        val aiSpd = PreferenceUtil.getIntValue(PREFERENCES_INT_AI_SPEED, useDefaults)
        slider.value = aiSpd

        val leaderboardSize = PreferenceUtil.getIntValue(PREFERENCES_INT_LEADERBOARD_SIZE, useDefaults)
        nfLeaderboardSize.value = leaderboardSize

        val aiAuto = PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE, useDefaults)
        chckbxAiAutomaticallyFinish.isSelected = aiAuto

        val checkForUpdates = PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES, useDefaults)
        chckbxCheckForUpdates.isSelected = checkForUpdates

        val showAnimations = PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS, useDefaults)
        chckbxShowAnimations.isSelected = showAnimations

        val preLoad = PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_PRE_LOAD_RESOURCES, useDefaults)
        chckbxPreloadResources.isSelected = preLoad

    }

    override fun valid(): Boolean
    {
        return true
    }

    override fun save()
    {
        val aiSpd = slider.value
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, aiSpd)

        val leaderboardSize = nfLeaderboardSize.getNumber()
        PreferenceUtil.saveInt(PREFERENCES_INT_LEADERBOARD_SIZE, leaderboardSize)

        val aiAuto = chckbxAiAutomaticallyFinish.isSelected
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE, aiAuto)

        val checkForUpdates = chckbxCheckForUpdates.isSelected
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES, checkForUpdates)

        val showAnimations = chckbxShowAnimations.isSelected
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS, showAnimations)

        val preLoad = chckbxPreloadResources.isSelected
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_PRE_LOAD_RESOURCES, preLoad)
    }

}
