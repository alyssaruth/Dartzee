package dartzee.main

import dartzee.`object`.DartsClient
import dartzee.core.util.DialogUtil
import dartzee.logging.CODE_LOOK_AND_FEEL_ERROR
import dartzee.logging.CODE_LOOK_AND_FEEL_SET
import dartzee.utils.InjectedThings.logger
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
        DialogUtil.showError("Failed to load Look & Feel 'Nimbus'.")
    }

    logger.info(CODE_LOOK_AND_FEEL_SET, "Set look and feel to $laf")
}