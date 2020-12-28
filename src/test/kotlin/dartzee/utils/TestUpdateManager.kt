package dartzee.utils

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import dartzee.`object`.DartsClient
import dartzee.helper.AbstractTest
import dartzee.logging.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldEndWith
import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException
import javax.swing.JOptionPane

class TestUpdateManager: AbstractTest()
{
    /**
     * Communication
     */
    @Test
    @Tag("integration")
    fun `Should log out an unexpected HTTP response, along with the full JSON payload`()
    {
        Unirest.setTimeouts(2000, 2000)
        val result = UpdateManager.queryLatestReleaseJson("https://api.github.com/repos/alexburlton/foo")
        result shouldBe null

        val log = verifyLog(CODE_UPDATE_ERROR, Severity.ERROR)
        log.message shouldBe "Received non-success HTTP status: 404 - Not Found"
        log.keyValuePairs[KEY_RESPONSE_BODY].toString() shouldContain """"message":"Not Found""""

        dialogFactory.errorsShown.shouldContainExactly("Failed to check for updates (unable to connect).")

        dialogFactory.loadingsShown.shouldContainExactly("Checking for updates...")
        dialogFactory.loadingVisible shouldBe false
    }

    @Test
    @Tag("integration")
    fun `Should catch and log any exceptions communicating over HTTPS`()
    {
        Unirest.setTimeouts(100, 100)
        val result = UpdateManager.queryLatestReleaseJson("https://ww.blargh.zcss.w")

        result shouldBe null
        val errorLog = verifyLog(CODE_UPDATE_ERROR, Severity.ERROR)

        errorLog.errorObject.shouldBeInstanceOf<UnirestException>()
        dialogFactory.errorsShown.shouldContainExactly("Failed to check for updates (unable to connect).")

        dialogFactory.loadingsShown.shouldContainExactly("Checking for updates...")
        dialogFactory.loadingVisible shouldBe false
    }

    @Test
    @Tag("integration")
    fun `Should retrieve a valid latest asset from the remote repo`()
    {
        val responseJson = UpdateManager.queryLatestReleaseJson(DARTZEE_REPOSITORY_URL)!!

        val version = responseJson.getString("tag_name")
        version.shouldStartWith("v")
        responseJson.getJSONArray("assets").length() shouldBe 1

        val asset = responseJson.getJSONArray("assets").getJSONObject(0)
        asset.getLong("id") shouldNotBe null
        asset.getString("name") shouldStartWith "Dartzee"
        asset.getString("name") shouldEndWith ".jar"
        asset.getLong("size") shouldNotBe null
    }

    /**
     * Parsing
     */
    @Test
    fun `Should parse correctly formed JSON`()
    {
        val json = """{
                    "tag_name": "foo",
                    "assets": [
                    {
                        "id": 123456,
                        "name": "Dartzee_v_foo.jar",
                        "size": 1
                    }
                    ]
                }"""

        val metadata = UpdateManager.parseUpdateMetadata(JSONObject(json))!!
        metadata.version shouldBe "foo"
        metadata.assetId shouldBe 123456
        metadata.fileName shouldBe "Dartzee_v_foo.jar"
        metadata.size shouldBe 1
    }

    @Test
    fun `Should log an error if no tag_name is present`()
    {
        val json = "{\"other_tag\":\"foo\"}"
        val metadata = UpdateManager.parseUpdateMetadata(JSONObject(json))
        metadata shouldBe null

        val log = verifyLog(CODE_PARSE_ERROR, Severity.ERROR)
        log.errorObject.shouldBeInstanceOf<JSONException>()
        log.keyValuePairs[KEY_RESPONSE_BODY].toString() shouldBe json
    }

    @Test
    fun `Should log an error if no assets are found`()
    {
        val json = """{"assets":[],"tag_name":"foo"}"""
        val metadata = UpdateManager.parseUpdateMetadata(JSONObject(json))
        metadata shouldBe null

        val log = verifyLog(CODE_PARSE_ERROR, Severity.ERROR)
        log.errorObject.shouldBeInstanceOf<JSONException>()
        log.keyValuePairs[KEY_RESPONSE_BODY].toString() shouldBe json
    }

    /**
     * Should update?
     */
    @Test
    fun `Should not proceed with the update if the versions match`()
    {
        val metadata = UpdateMetadata(DARTS_VERSION_NUMBER, 123456, "Dartzee_x_y.jar", 100)

        UpdateManager.shouldUpdate(DARTS_VERSION_NUMBER, metadata) shouldBe false
        val log = verifyLog(CODE_UPDATE_CHECK_RESULT)
        log.message shouldBe "Up to date"
    }

    @Test
    fun `Should show an info and not proceed to auto update if OS is not windows`()
    {
        DartsClient.operatingSystem = "foo"

        val metadata = UpdateMetadata("v100", 123456, "Dartzee_x_y.jar", 100)
        UpdateManager.shouldUpdate(DARTS_VERSION_NUMBER, metadata) shouldBe false

        val log = verifyLog(CODE_UPDATE_CHECK_RESULT)
        log.message shouldBe "Newer release available - v100"

        dialogFactory.questionsShown.shouldBeEmpty()
        dialogFactory.infosShown.shouldContainExactly("An update is available (v100). You can download it manually from: \n" +
                "$DARTZEE_MANUAL_DOWNLOAD_URL/tags/v100")
    }

    @Test
    fun `Should not proceed with the update if user selects 'No'`()
    {
        DartsClient.operatingSystem = "windows"

        val metadata = UpdateMetadata("foo", 123456, "Dartzee_x_y.jar", 100)

        dialogFactory.questionOption = JOptionPane.NO_OPTION

        UpdateManager.shouldUpdate("bar", metadata) shouldBe false
        dialogFactory.questionsShown.shouldContainExactly("An update is available (foo). Would you like to download it now?")
    }

    @Test
    fun `Should proceed with the update if user selects 'Yes'`()
    {
        DartsClient.operatingSystem = "windows"

        val metadata = UpdateMetadata("foo", 123456, "Dartzee_x_y.jar", 100)

        dialogFactory.questionOption = JOptionPane.YES_OPTION

        UpdateManager.shouldUpdate("bar", metadata) shouldBe true
        dialogFactory.questionsShown.shouldContainExactly("An update is available (foo). Would you like to download it now?")
    }

    /**
     * Prepare batch file
     */
    @Test
    fun `Should overwrite existing batch file with the correct contents`()
    {
        val updateFile = File("update.bat")
        updateFile.writeText("blah")

        UpdateManager.prepareBatchFile()

        updateFile.readText() shouldBe javaClass.getResource("/update/update.bat").readText()
        updateFile.delete()
    }

    /**
     * Run update
     */
    @Test
    fun `Should log an error if batch file goes wrong`()
    {
        val runtime = mockk<Runtime>()
        val error = IOException("Argh")
        every { runtime.exec(any<String>()) } throws error
        UpdateManager.startUpdate("foo", runtime)

        val log = verifyLog(CODE_BATCH_ERROR, Severity.ERROR)
        log.errorObject shouldBe error

        dialogFactory.errorsShown.shouldContainExactly("Failed to launch update.bat - call the following manually to perform the update: \n\nupdate.bat foo")
    }
}