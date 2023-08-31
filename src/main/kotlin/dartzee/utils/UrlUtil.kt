package dartzee.utils

import dartzee.logging.CODE_HYPERLINK_ERROR

/**
 * N.B. will likely only work on linux
 */
fun launchUrl(url: String, runtime: Runtime = Runtime.getRuntime())
{
    try
    {
        runtime.exec("xdg-open $url")
    }
    catch (e: Exception)
    {
        InjectedThings.logger.error(CODE_HYPERLINK_ERROR, "Failed to launch $url", e)
    }
}