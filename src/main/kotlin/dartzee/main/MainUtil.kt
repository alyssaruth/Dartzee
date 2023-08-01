package dartzee.main

import dartzee.core.util.CoreRegistry.INSTANCE_STRING_DEVICE_ID
import dartzee.core.util.CoreRegistry.instance
import dartzee.core.util.DialogUtil
import dartzee.logging.CODE_LOOK_AND_FEEL_ERROR
import dartzee.logging.CODE_LOOK_AND_FEEL_SET
import dartzee.logging.KEY_APP_VERSION
import dartzee.logging.KEY_DEVICE_ID
import dartzee.logging.KEY_DEV_MODE
import dartzee.logging.KEY_OPERATING_SYSTEM
import dartzee.logging.KEY_USERNAME
import dartzee.`object`.DartsClient
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.InjectedThings.logger
import java.util.*
import javax.swing.UIManager

fun setLookAndFeel()
{
    if (!DartsClient.isAppleOs())
    {
        setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
    }
}

fun setLookAndFeel(laf: String)
{
    try
    {
        UIManager.setLookAndFeel(laf)
    }
    catch (e: Throwable)
    {
        logger.error(CODE_LOOK_AND_FEEL_ERROR, "Failed to load laf $laf", e)
        DialogUtil.showErrorOLD("Failed to load Look & Feel 'Nimbus'.")
    }

    logger.info(CODE_LOOK_AND_FEEL_SET, "Set look and feel to $laf")
}

fun setLoggingContextFields()
{
    logger.addToContext(KEY_USERNAME, getUsername())
    logger.addToContext(KEY_APP_VERSION, DARTS_VERSION_NUMBER)
    logger.addToContext(KEY_OPERATING_SYSTEM, DartsClient.operatingSystem)
    logger.addToContext(KEY_DEVICE_ID, getDeviceId())
    logger.addToContext(KEY_DEV_MODE, DartsClient.devMode)
}

fun getDeviceId() = instance.get(INSTANCE_STRING_DEVICE_ID, null) ?: setDeviceId()
private fun setDeviceId(): String
{
    val deviceId = UUID.randomUUID().toString()
    instance.put(INSTANCE_STRING_DEVICE_ID, deviceId)
    return deviceId
}

fun getUsername(): String = System.getProperty("user.name")