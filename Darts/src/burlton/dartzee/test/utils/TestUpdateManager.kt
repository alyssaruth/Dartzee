package burlton.dartzee.test.utils

import burlton.core.test.helper.exceptionLogged
import burlton.core.test.helper.getLogs
import burlton.dartzee.code.utils.DARTS_VERSION_NUMBER
import burlton.dartzee.code.utils.DARTZEE_REPOSITORY_URL
import burlton.dartzee.code.utils.UpdateManager
import burlton.dartzee.test.helper.AbstractDartsTest
import com.mashape.unirest.http.Unirest
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldEndWith
import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.json.JSONObject
import org.junit.Test
import javax.swing.JOptionPane

class TestUpdateManager: AbstractDartsTest()
{
    @Test
    fun `Should log out an unexpected HTTP response, along with the full JSON payload`()
    {
        val result = UpdateManager.queryLatestReleaseJson("https://api.github.com/repos/alexburlton/foo")

        result shouldBe null

        getLogs().shouldContain("Received non-success HTTP status: 404 - Not Found")
        getLogs().shouldContain("{\"message\":\"Not Found\"")
        dialogFactory.errorsShown.shouldBeEmpty()

        dialogFactory.loadingsShown.shouldContainExactly("Checking for updates...")
        dialogFactory.loadingVisible shouldBe false
    }

    @Test
    fun `Should catch and log any exceptions communicating over HTTPS`()
    {
        Unirest.setTimeouts(250, 250)

        val result = UpdateManager.queryLatestReleaseJson("https://ww.blargh.zcss.w")

        result shouldBe null
        exceptionLogged() shouldBe true
        getLogs() shouldContain("java.net.SocketTimeoutException: connect timed out")
        dialogFactory.errorsShown.shouldBeEmpty()

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

    @Test
    fun `Should show an error and abort if failed to communicate with the API`()
    {
        UpdateManager.shouldUpdate(null) shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("Failed to check for updates (unable to connect).")
    }

    @Test
    fun `Should not proceed with the update if the versions match`()
    {
        val json = "{\"tag_name\": \"$DARTS_VERSION_NUMBER\"}"

        UpdateManager.shouldUpdate(JSONObject(json)) shouldBe false
        getLogs() shouldContain "I am up to date"
    }

    @Test
    fun `Should not proceed with the update if user selects 'No'`()
    {
        val json = "{\"tag_name\": \"foo\"}"

        dialogFactory.questionOption = JOptionPane.NO_OPTION

        UpdateManager.shouldUpdate(JSONObject(json)) shouldBe false
        dialogFactory.questionsShown.shouldContainExactly("An update is available (foo). Would you like to download it now?")
    }

    @Test
    fun `Should proceed with the update if user selects 'Yes'`()
    {
        val json = "{\"tag_name\": \"foo\"}"

        dialogFactory.questionOption = JOptionPane.YES_OPTION

        UpdateManager.shouldUpdate(JSONObject(json)) shouldBe true
        dialogFactory.questionsShown.shouldContainExactly("An update is available (foo). Would you like to download it now?")
    }
}