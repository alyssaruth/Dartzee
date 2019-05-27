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
object UpdateManager
{
    fun checkForUpdates()
    {
        try
        {
            checkForUpdatesAndDoDownloadIfRequired()
        }
        catch (t: Throwable)
        {
            Debug.stackTrace(t)
        }
    }

    private fun checkForUpdatesAndDoDownloadIfRequired()
    {
        //Show this here, checking the CRC can take time
        Debug.append("Checking for updates - my version is $DARTS_VERSION_NUMBER")

        val jsonObject = queryLatestReleaseJson(DARTZEE_REPOSITORY_URL)
        if (!shouldUpdate(jsonObject))
        {
            return
        }

        val assets = jsonObject!!.getJSONArray("assets")
        if (assets.length() != 1)
        {
            Debug.append(jsonObject.toString())
            Debug.stackTrace("Unexpected number of assets ${assets.length()} - aborting update")
            return
        }

        val remoteVersion = jsonObject.getString("tag_name")
        startUpdate(remoteVersion, assets.getJSONObject(0))
    }

    fun queryLatestReleaseJson(repositoryUrl: String): JSONObject?
    {
        try
        {
            DialogUtil.showLoadingDialog("Checking for updates...")

            val response = Unirest.get("$repositoryUrl/releases/latest").asJson()
            if (response.status != 200)
            {
                Debug.append("Received non-success HTTP status: ${response.status} - ${response.statusText}")
                Debug.append(response.body.toString())
                return null
            }

            return response.body.`object`
        }
        catch (t: Throwable)
        {
            Debug.stackTraceSilently(t)
            return null
        }
        finally
        {
            DialogUtil.dismissLoadingDialog()
        }
    }

    fun shouldUpdate(responseJson: JSONObject?): Boolean
    {
        if (responseJson == null)
        {
            DialogUtil.showError("Failed to check for updates (unable to connect).")
            return false
        }

        val remoteVersion = responseJson.getString("tag_name")
        if (remoteVersion == DARTS_VERSION_NUMBER)
        {
            Debug.append("I am up to date")
            return false
        }

        //An update is available
        Debug.append("Newer release available - $remoteVersion")
        val answer = DialogUtil.showQuestion("An update is available ($remoteVersion). Would you like to download it now?", false)
        return answer == JOptionPane.YES_OPTION
    }

    private fun startUpdate(remoteVersion: String, asset: JSONObject)
    {
        val assetId = asset.getLong("id")
        val fileName = asset.getString("name")
        val size = asset.getLong("size")

        val updateFile = File("update.bat")

        //Write the batch file now, overwriting if necessary
        updateFile.delete()
        val updateScript = javaClass.getResource("/update/update.bat").readText()
        updateFile.writeText(updateScript)

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
