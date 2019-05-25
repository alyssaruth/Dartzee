package burlton.dartzee.code.utils

import burlton.core.code.util.Debug
import burlton.desktopcore.code.util.DialogUtil
import com.mashape.unirest.http.Unirest
import org.json.JSONObject
import java.io.File
import java.io.IOException
import javax.swing.JOptionPane

/**
 * Class to check for updates and launch the EntropyUpdater via a batch file if they are available
 */
object UpdateChecker
{
    fun checkForUpdates()
    {
        try
        {
            checkForUpdatesAndDoDownloadIfRequired()
        }
        finally
        {
            DialogUtil.dismissLoadingDialog()
        }
    }

    private fun checkForUpdatesAndDoDownloadIfRequired()
    {
        //Show this here, checking the CRC can take time
        DialogUtil.showLoadingDialog("Checking for updates...")

        Debug.append("Checking for updates - my version is $DARTS_VERSION_NUMBER")

        val jsonObject = queryLatestReleaseJson()
        if (jsonObject == null)
        {
            DialogUtil.showError("Failed to check for updates (unable to connect).")
            return
        }

        val remoteVersion = jsonObject.get("tag_name").toString()
        if (remoteVersion == DARTS_VERSION_NUMBER)
        {
            Debug.append("I am up to date")
            return
        }

        DialogUtil.dismissLoadingDialog()

        Debug.append("Newer release available - $remoteVersion")
        val assets = jsonObject.getJSONArray("assets")
        if (assets.length() != 1)
        {
            Debug.append(jsonObject.toString())
            Debug.stackTrace("Unexpected number of assets ${assets.length()} - aborting update")
            return
        }

        //An update is available
        val answer = DialogUtil.showQuestion("An update is available ($remoteVersion). Would you like to download it now?", false)
        if (answer == JOptionPane.NO_OPTION)
        {
            return
        }

        startUpdate(remoteVersion, assets.getJSONObject(0))
    }

    private fun queryLatestReleaseJson(): JSONObject?
    {
        try
        {
            val response = Unirest.get("https://api.github.com/repos/alexburlton/Dartzee/releases/latest").asJson()
            if (response.status != 200)
            {
                Debug.append("Received unexpected HTTP response. Status ${response.status} - ${response.statusText}")
                Debug.append(response.body.toString())
                DialogUtil.showError("Failed to check for updates (unable to connect).")
                return null
            }

            return response.body.`object`
        }
        catch (t: Throwable)
        {
            Debug.stackTraceSilently(t)
            return null
        }
    }

    private fun startUpdate(remoteVersion: String, asset: JSONObject)
    {
        val assetId = asset.getLong("id")
        val fileName = asset.getString("name")
        val size = asset.getLong("size")

        val updateFile = File("update.bat")
        if (!updateFile.exists())
        {
            //Write the batch file now
            val updateScript = javaClass.getResource("/update/update.bat").readText()
            updateFile.writeText(updateScript)
        }

        val args = "$size $remoteVersion $fileName $assetId"

        try
        {
            Runtime.getRuntime().exec("cmd /c start update.bat $args")
            System.exit(0)
        }
        catch (ioe: IOException)
        {
            Debug.stackTrace(ioe)
            val manualCommand = "update.bat $args"

            val msg = "Failed to launch update.bat - call the following manually to perform the update: \n\n$manualCommand"
            DialogUtil.showError(msg)
        }
    }
}
