package burlton.dartzee.test.utils

import burlton.core.test.helper.exceptionLogged
import burlton.core.test.helper.getLogs
import burlton.dartzee.code.utils.DARTS_VERSION_NUMBER
import burlton.dartzee.code.utils.DARTZEE_REPOSITORY_URL
import burlton.dartzee.code.utils.UpdateManager
import burlton.dartzee.code.utils.UpdateMetadata
import burlton.dartzee.test.helper.AbstractDartsTest
import com.mashape.unirest.http.Unirest
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldEndWith
import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.json.JSONObject
import org.junit.Test
import java.io.File
import java.io.IOException
import javax.swing.JOptionPane

class TestUpdateManager: AbstractDartsTest()
{
    /**
     * Communication
     */
    @Test
    fun `Should log out an unexpected HTTP response, along with the full JSON payload`()
    {
        Unirest.setTimeouts(2000, 2000)
        val result = UpdateManager.queryLatestReleaseJson("https://api.github.com/repos/alexburlton/foo")

        result shouldBe null

        getLogs().shouldContain("Received non-success HTTP status: 404 - Not Found")
        getLogs().shouldContain("{\"message\":\"Not Found\"")
        dialogFactory.errorsShown.shouldContainExactly("Failed to check for updates (unable to connect).")

        dialogFactory.loadingsShown.shouldContainExactly("Checking for updates...")
        dialogFactory.loadingVisible shouldBe false
    }

    @Test
    fun `Should catch and log any exceptions communicating over HTTPS`()
    {
        val result = UpdateManager.queryLatestReleaseJson("https://ww.blargh.zcss.w")

        result shouldBe null
        exceptionLogged() shouldBe true
        getLogs() shouldContain("java.net.UnknownHostException")
        dialogFactory.errorsShown.shouldContainExactly("Failed to check for updates (unable to connect).")

        dialogFactory.loadingsShown.shouldContainExactly("Checking for updates...")
        dialogFactory.loadingVisible shouldBe false
    }

    @Test
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
        exceptionLogged() shouldBe true
        getLogs() shouldContain json
        getLogs() shouldContain "org.json.JSONException"
    }

    @Test
    fun `Should log an error if no assets are found`()
    {
        val json = """{"assets":[],"tag_name":"foo"}"""
        val metadata = UpdateManager.parseUpdateMetadata(JSONObject(json))
        metadata shouldBe null
        exceptionLogged() shouldBe true
        getLogs() shouldContain json
        getLogs() shouldContain "org.json.JSONException"
    }

    /**
     * Should update?
     */
    @Test
    fun `Should not proceed with the update if the versions match`()
    {
        val metadata = UpdateMetadata(DARTS_VERSION_NUMBER, 123456, "Dartzee_x_y.jar", 100)

        UpdateManager.shouldUpdate(DARTS_VERSION_NUMBER, metadata) shouldBe false
        getLogs() shouldContain "I am up to date"
    }

    @Test
    fun `Should not proceed with the update if user selects 'No'`()
    {
        val metadata = UpdateMetadata("foo", 123456, "Dartzee_x_y.jar", 100)

        dialogFactory.questionOption = JOptionPane.NO_OPTION

        UpdateManager.shouldUpdate("bar", metadata) shouldBe false
        dialogFactory.questionsShown.shouldContainExactly("An update is available (foo). Would you like to download it now?")
    }

    @Test
    fun `Should proceed with the update if user selects 'Yes'`()
    {
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
        every {runtime.exec("foo") } throws IOException("Argh")
        UpdateManager.startUpdate("foo", runtime)

        exceptionLogged() shouldBe true

        dialogFactory.errorsShown.shouldContainExactly("Failed to launch update.bat - call the following manually to perform the update: \n\nupdate.bat foo")
    }

    /**
     * E2E - run to actually download+run the latest JAR
     */
    /*@Test
    fun `Should perform the whole download without error`()
    {
        dialogFactory.questionOption = JOptionPane.YES_OPTION

        UpdateManager.checkForUpdates("foo")
    }*/
}