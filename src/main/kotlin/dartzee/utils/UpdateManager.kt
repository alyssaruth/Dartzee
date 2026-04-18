package dartzee.utils

import dartzee.core.bean.LinkLabel
import dartzee.core.util.DialogUtil
import dartzee.core.util.FileUtil
import dartzee.logging.CODE_PARSE_ERROR
import dartzee.logging.CODE_UPDATE_CHECK
import dartzee.logging.CODE_UPDATE_CHECK_RESULT
import dartzee.logging.CODE_UPDATE_ERROR
import dartzee.logging.CODE_UPDATE_STARTING
import dartzee.logging.KEY_RESPONSE_BODY
import dartzee.`object`.DartsClient
import dartzee.utils.InjectedThings.logger
import java.awt.BorderLayout
import java.io.File
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import kong.unirest.MimeTypes
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

        startUpdate(DARTZEE_REPOSITORY_URL, metadata, Runtime.getRuntime())
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
        val lblOne = JLabel("Failed to download $newVersion. You can download it manually from:")
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

    fun startUpdate(repositoryUrl: String, metadata: UpdateMetadata, runtime: Runtime) {
        val success = downloadJar(repositoryUrl, metadata)
        if (!success) {
            return
        }

        if (DartsClient.isWindowsOs()) {
            prepareUpdateFile("update.bat")
        } else if (DartsClient.isLinux()) {
            prepareUpdateFile("update.sh")
        }

        relaunchWithScript(metadata, runtime)
    }

    private fun downloadJar(repositoryUrl: String, metadata: UpdateMetadata): Boolean =
        try {
            FileUtil.deleteFileIfExists(metadata.fileName)
            val downloadUrl = "$repositoryUrl/releases/assets/${metadata.assetId}"
            logger.info(
                CODE_UPDATE_STARTING,
                "Downloading from $downloadUrl to ${metadata.fileName}",
            )

            DialogUtil.showLoadingDialog("Downloading ${metadata.version}...")
            val response = Unirest.get(downloadUrl).accept(MimeTypes.EXE).asFile(metadata.fileName)
            if (response.status != 200) {
                logger.error(
                    CODE_UPDATE_ERROR,
                    "Received non-success HTTP status: ${response.status} - ${response.statusText}",
                    KEY_RESPONSE_BODY to response.body,
                )
                DialogUtil.showError("Failed to check for updates (unable to connect).")
                return false
            }

            true
        } catch (e: Exception) {
            DialogUtil.dismissLoadingDialog()
            logger.error(CODE_UPDATE_ERROR, "Caught $e during download", e)
            showManualDownloadMessage(metadata.version)
            false
        } finally {
            DialogUtil.dismissLoadingDialog()
        }

    private fun relaunchWithScript(metadata: UpdateMetadata, runtime: Runtime) {
        val success =
            runCommand(
                windows = arrayOf("cmd", "/c", "start", "update.bat", metadata.fileName),
                linux = arrayOf("sh", "update.sh", metadata.fileName),
                runtime,
            )

        if (!success) {
            DialogUtil.showError(
                "Failed to swap in updated file. \n\nDelete the old Dartzee.jar and rename ${metadata.fileName} -> Dartzee.jar"
            )
        }

        InjectedThings.exiter.exit(0)
    }

    fun prepareUpdateFile(filename: String) {
        val updateFile = File(filename)

        updateFile.delete()
        val updateScript = javaClass.getResource("/update/$filename").readText()
        updateFile.writeText(updateScript)
    }
}

data class UpdateMetadata(
    val version: String,
    val assetId: Long,
    val fileName: String,
    val size: Long,
)
