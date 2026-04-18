package dartzee.utils

import dartzee.logging.CODE_EXEC
import dartzee.logging.CODE_EXEC_ERROR
import dartzee.`object`.DartsClient
import dartzee.utils.InjectedThings.logger

fun launchUrl(url: String, runtime: Runtime = Runtime.getRuntime()) {
    runCommand(
        windows = arrayOf("cmd", "/c", "start", url),
        linux = arrayOf("xdg-open", url),
        runtime,
    )
}

fun runCommand(
    windows: Array<String>,
    linux: Array<String>,
    runtime: Runtime = Runtime.getRuntime(),
) =
    if (DartsClient.isLinux()) {
        execCommand(linux, runtime)
    } else if (DartsClient.isWindowsOs()) {
        execCommand(windows, runtime)
    } else {
        logger.error(
            CODE_EXEC_ERROR,
            "Operating system unsupported: ${DartsClient.operatingSystem}",
        )
        false
    }

private fun execCommand(command: Array<String>, runtime: Runtime) =
    try {
        logger.info(CODE_EXEC, "Running command: ${command.joinToString(" ")}")
        runtime.exec(command)
        true
    } catch (e: Exception) {
        logger.error(CODE_EXEC_ERROR, "Command failed: ${command.joinToString(" ")}", e)
        false
    }
