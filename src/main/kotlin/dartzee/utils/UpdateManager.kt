package dartzee.utils

import dartzee.core.bean.LinkLabel
import dartzee.core.util.DialogUtil
import dartzee.logging.CODE_BATCH_ERROR
import dartzee.logging.CODE_PARSE_ERROR
import dartzee.logging.CODE_UPDATE_CHECK
import dartzee.logging.CODE_UPDATE_CHECK_RESULT
import dartzee.logging.CODE_UPDATE_ERROR
import dartzee.logging.KEY_RESPONSE_BODY
import dartzee.`object`.DartsClient
import dartzee.utils.InjectedThings.logger
import java.awt.BorderLayout
import java.io.File
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import kong.unirest.Unirest
import kong.unirest.json.JSONObject

/**
 * Automatically check for and download updates using the Github API
 *
 * https://developer.github.com/v3/repos/releases/#get-the-latest-release
 */
object UpdateManager {
    fun checkForUpdates(currentVersion: String) {
        // Show this here, checking the CRC can take time
        logger.info(CODE_UPDATE_CHECK, "Checking for updates - my version is $currentVersion")

        val jsonResponse = queryLatestReleaseJson(DARTZEE_REPOSITORY_URL)
        jsonResponse ?: return

        val metadata = parseUpdateMetadata(jsonResponse)
        if (metadata == null || !shouldUpdate(currentVersion, metadata)) {
            return
        }

        startUpdate(metadata.getArgs(), Runtime.getRuntime())
    }

    fun queryLatestReleaseJson(repositoryUrl: String): JSONObject? {
        try {
            DialogUtil.showLoadingDialog("Checking for updates...")

            val response = Unirest.get("$repositoryUrl/releases/latest").asJson()
            if (response.status != 200) {
                logger.error(
                    CODE_UPDATE_ERROR,
                    "Received non-success HTTP status: ${response.status} - ${response.statusText}",
                    KEY_RESPONSE_BODY to response.body,
                )
                DialogUtil.showError("Failed to check for updates (unable to connect).")
                return null
            }

            return response.body.`object`
        } catch (t: Throwable) {
            logger.error(CODE_UPDATE_ERROR, "Caught $t checking for updates", t)
            DialogUtil.showError("Failed to check for updates (unable to connect).")
            return null
        } finally {
            DialogUtil.dismissLoadingDialog()
        }
    }

    fun shouldUpdate(currentVersion: String, metadata: UpdateMetadata): Boolean {
        val newVersion = metadata.version
        if (newVersion == currentVersion) {
            logger.info(CODE_UPDATE_CHECK_RESULT, "Up to date")
            return false
        }

        // An update is available
        logger.info(CODE_UPDATE_CHECK_RESULT, "Newer release available - $newVersion")

        if (!DartsClient.isWindowsOs()) {
            showManualDownloadMessage(newVersion)
            return false
        }

        val answer =
            DialogUtil.showQuestion(
                "An update is available (${metadata.version}). Would you like to download it now?",
                false,
            )
        return answer == JOptionPane.YES_OPTION
    }

    private fun showManualDownloadMessage(newVersion: String) {
        val fullUrl = "$DARTZEE_MANUAL_DOWNLOAD_URL/tag/$newVersion"
        val panel = JPanel()
        panel.layout = BorderLayout(0, 0)
        val lblOne =
            JLabel("An update is available ($newVersion). You can download it manually from:")
        val linkLabel = LinkLabel(fullUrl) { launchUrl(fullUrl) }

        panel.add(lblOne, BorderLayout.NORTH)
        panel.add(linkLabel, BorderLayout.SOUTH)

        DialogUtil.showCustomMessage(panel)
    }

    fun parseUpdateMetadata(responseJson: JSONObject): UpdateMetadata? {
        return try {
            val remoteVersion = responseJson.getString("tag_name")
            val assets = responseJson.getJSONArray("assets")
            val asset = assets.getJSONObject(0)

            val assetId = asset.getLong("id")
            val fileName = asset.getString("name")
            val size = asset.getLong("size")
            UpdateMetadata(remoteVersion, assetId, fileName, size)
        } catch (t: Throwable) {
            logger.error(
                CODE_PARSE_ERROR,
                "Error parsing update response",
                t,
                KEY_RESPONSE_BODY to responseJson,
            )
            null
        }
    }

    fun startUpdate(args: String, runtime: Runtime) {
        prepareBatchFile()

        try {
            runtime.exec("cmd /c start update.bat $args")
        } catch (t: Throwable) {
            logger.error(CODE_BATCH_ERROR, "Error running update.bat", t)
            val manualCommand = "update.bat $args"

            val msg =
                "Failed to launch update.bat - call the following manually to perform the update: \n\n$manualCommand"
            DialogUtil.showError(msg)
            return
        }

        InjectedThings.exiter.exit(0)
    }

    fun prepareBatchFile() {
        val updateFile = File("update.bat")

        updateFile.delete()
        val updateScript = javaClass.getResource("/update/update.bat").readText()
        updateFile.writeText(updateScript)
    }
}

data class UpdateMetadata(
    val version: String,
    val assetId: Long,
    val fileName: String,
    val size: Long,
) {
    fun getArgs() = "$size $version $fileName $assetId"
}
