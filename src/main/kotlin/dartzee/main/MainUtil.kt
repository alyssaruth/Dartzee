package dartzee.main

import Theme
import dartzee.core.util.DialogUtil
import dartzee.logging.CODE_LOOK_AND_FEEL_ERROR
import dartzee.logging.CODE_LOOK_AND_FEEL_SET
import dartzee.logging.KEY_APP_VERSION
import dartzee.logging.KEY_DEVICE_ID
import dartzee.logging.KEY_DEV_MODE
import dartzee.logging.KEY_OPERATING_SYSTEM
import dartzee.logging.KEY_USERNAME
import dartzee.`object`.ColourWrapper
import dartzee.`object`.DartsClient
import dartzee.preferences.Preferences
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.preferenceService
import java.awt.Color
import java.util.*
import javax.swing.UIManager

fun setLookAndFeel() {
    if (!DartsClient.isAppleOs()) {
        setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
    }
}

fun setLookAndFeel(laf: String) {
    try {
        val lightOrange = Color.decode("#FF6600")
        val orange = Color.decode("#CF5704")
        val colours =
            ColourWrapper(
                lightOrange,
                orange,
                orange,
                Color.decode("#009900"),
                Color.GREEN,
                Color.GREEN,
                orange,
                Color.GREEN,
            )

        InjectedThings.theme =
            Theme(
                "halloween",
                orange,
                Color.decode("#32172a"),
                lightBackground = Color.decode("#DAB1DA"),
                fontColor = Color.decode("#880808"),
                dartboardColours = colours,
            )

        InjectedThings.theme?.apply()
        UIManager.setLookAndFeel(laf)
    } catch (e: Throwable) {
        logger.error(CODE_LOOK_AND_FEEL_ERROR, "Failed to load laf $laf", e)
        DialogUtil.showErrorOLD("Failed to load Look & Feel 'Nimbus'.")
    }

    logger.info(CODE_LOOK_AND_FEEL_SET, "Set look and feel to $laf")
}

fun setLoggingContextFields() {
    logger.addToContext(KEY_USERNAME, getUsername())
    logger.addToContext(KEY_APP_VERSION, DARTS_VERSION_NUMBER)
    logger.addToContext(KEY_OPERATING_SYSTEM, DartsClient.operatingSystem)
    logger.addToContext(KEY_DEVICE_ID, getDeviceId())
    logger.addToContext(KEY_DEV_MODE, DartsClient.devMode)
}

fun getDeviceId() = preferenceService.find(Preferences.deviceId) ?: setDeviceId()

private fun setDeviceId(): String {
    val deviceId = UUID.randomUUID().toString()
    preferenceService.save(Preferences.deviceId, deviceId)
    return deviceId
}

fun getUsername(): String = System.getProperty("user.name")
