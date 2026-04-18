package dartzee.utils

import com.github.alyssaburlton.swingtest.clickNo
import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.clickYes
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import dartzee.core.bean.LinkLabel
import dartzee.core.helper.verifyNotCalled
import dartzee.findLoadingDialog
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.getQuestionDialog
import dartzee.helper.AbstractTest
import dartzee.helper.assertDoesNotExit
import dartzee.helper.assertExits
import dartzee.logging.CODE_EXEC_ERROR
import dartzee.logging.CODE_PARSE_ERROR
import dartzee.logging.CODE_UPDATE_CHECK_RESULT
import dartzee.logging.CODE_UPDATE_ERROR
import dartzee.logging.KEY_RESPONSE_BODY
import dartzee.logging.Severity
import dartzee.`object`.DartsClient
import dartzee.runAsync
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.SwingUtilities
import kong.unirest.HttpStatus
import kong.unirest.Unirest
import kong.unirest.UnirestException
import kong.unirest.json.JSONException
import kong.unirest.json.JSONObject
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateManagerTest : AbstractTest() {
    private val TEST_JAR_FILE_NAME = "Dartzee_v7_3_0.jar"

    @BeforeEach
    fun beforeEach() {
        Unirest.config().automaticRetries(false)
    }

    @AfterEach
    fun afterEach() {
        File("update.sh").delete()
        File("update.bat").delete()
        File(TEST_JAR_FILE_NAME).delete()
    }

    @Test
    fun `Should log out an unexpected HTTP response, along with the full JSON payload`() {
        val response =
            MockResponse().setResponseCode(HttpStatus.NOT_FOUND).setBody("{ \"foo\": \"bar\" }")
        val server = startWebServer(response)

        val errorMessage = queryLatestReleaseJsonExpectingError(server.url("root").toString())
        errorMessage shouldBe "Failed to check for updates (unable to connect)."

        val log = verifyLog(CODE_UPDATE_ERROR, Severity.ERROR)
        log.message shouldBe "Received non-success HTTP status: 404 - Client Error"
        log.keyValuePairs[KEY_RESPONSE_BODY].toString() shouldBe "{\"foo\":\"bar\"}"

        findLoadingDialog("Checking for updates...")!!.shouldNotBeVisible()
    }

    @Test
    fun `Should catch and log any exceptions communicating over HTTPS`() {
        val server =
            startWebServer(MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AT_START })

        val errorMessage = queryLatestReleaseJsonExpectingError(server.url("root").toString())
        errorMessage shouldBe "Failed to check for updates (unable to connect)."

        val errorLog = verifyLog(CODE_UPDATE_ERROR, Severity.ERROR)
        errorLog.errorObject.shouldBeInstanceOf<UnirestException>()

        findLoadingDialog("Checking for updates...")!!.shouldNotBeVisible()
    }

    private fun queryLatestReleaseJsonExpectingError(repositoryUrl: String): String {
        val result = runAsync { UpdateManager.queryLatestReleaseJson(repositoryUrl) }

        val error = getErrorDialog()
        val errorText = error.getDialogMessage()

        error.clickOk()
        flushEdt()

        result shouldBe null
        return errorText
    }

    @Test
    fun `Should parse correctly formed JSON`() {
        val json =
            """{
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
    }

    @Test
    fun `Should log an error if no tag_name is present`() {
        val json = "{\"other_tag\":\"foo\"}"
        val metadata = UpdateManager.parseUpdateMetadata(JSONObject(json))
        metadata shouldBe null

        val log = verifyLog(CODE_PARSE_ERROR, Severity.ERROR)
        log.errorObject.shouldBeInstanceOf<JSONException>()
        log.keyValuePairs[KEY_RESPONSE_BODY].toString() shouldBe json
    }

    @Test
    fun `Should log an error if no assets are found`() {
        val json = """{"assets":[],"tag_name":"foo"}"""
        val metadata = UpdateManager.parseUpdateMetadata(JSONObject(json))
        metadata shouldBe null

        val log = verifyLog(CODE_PARSE_ERROR, Severity.ERROR)
        log.errorObject.shouldBeInstanceOf<JSONException>()
        log.keyValuePairs[KEY_RESPONSE_BODY].toString() shouldBe json
    }

    @Test
    fun `Should not proceed with the update if the versions match`() {
        val metadata = UpdateMetadata(DARTS_VERSION_NUMBER, 123456, "Dartzee_x_y.jar")

        UpdateManager.shouldUpdate(DARTS_VERSION_NUMBER, metadata) shouldBe false
        val log = verifyLog(CODE_UPDATE_CHECK_RESULT)
        log.message shouldBe "Up to date"
    }

    @Test
    fun `Should not proceed with the update if user selects 'No'`() {
        DartsClient.operatingSystem = "windows"

        val metadata = UpdateMetadata("foo", 123456, "Dartzee_x_y.jar")
        val result = shouldUpdateAsync("bar", metadata)

        val question = getQuestionDialog()
        question.getDialogMessage() shouldBe
            "An update is available (foo). Would you like to download it now?"
        question.clickNo()
        flushEdt()

        result.get() shouldBe false
    }

    @Test
    fun `Should proceed with the update if user selects 'Yes'`() {
        DartsClient.operatingSystem = "windows"

        val metadata = UpdateMetadata("foo", 123456, "Dartzee_x_y.jar")
        val result = shouldUpdateAsync("bar", metadata)

        val question = getQuestionDialog()
        question.getDialogMessage() shouldBe
            "An update is available (foo). Would you like to download it now?"
        question.clickYes()
        flushEdt()

        result.get() shouldBe true
    }

    private fun shouldUpdateAsync(currentVersion: String, metadata: UpdateMetadata): AtomicBoolean {
        val result = AtomicBoolean(false)
        SwingUtilities.invokeLater {
            result.set(UpdateManager.shouldUpdate(currentVersion, metadata))
        }

        flushEdt()
        return result
    }

    @Test
    fun `Should log an error and not exit if download has non-success status`() {
        val runtime = mockk<Runtime>()
        val server = startWebServer(MockResponse().setResponseCode(HttpStatus.NOT_FOUND))

        runAsync {
            assertDoesNotExit {
                UpdateManager.doUpdate(
                    server.url("root").toString(),
                    UpdateMetadata("v7.3.0", 12345, "Dartzee_v7_3_0.jar"),
                    runtime,
                )
            }
        }

        flushEdt()
        findLoadingDialog("Downloading v7.3.0...")!!.shouldNotBeVisible()
        val errorDialog = getErrorDialog()
        val linkLabel = errorDialog.getChild<LinkLabel>()
        linkLabel.text shouldBe "<html><u>$DARTZEE_MANUAL_DOWNLOAD_URL/tag/v7.3.0</u></html>"
        errorDialog.clickOk(async = true)

        verifyNotCalled { runtime.exec(any<Array<String>>()) }

        val log = verifyLog(CODE_UPDATE_ERROR, Severity.ERROR)
        log.message shouldBe "Received non-success HTTP status: 404 - Client Error"
    }

    @Test
    fun `Should log an error and not exit if download throws an error`() {
        val runtime = mockk<Runtime>()
        val server =
            startWebServer(MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AT_START })

        runAsync {
            assertDoesNotExit {
                UpdateManager.doUpdate(
                    server.url("root").toString(),
                    UpdateMetadata("v7.3.0", 12345, "Dartzee_v7_3_0.jar"),
                    runtime,
                )
            }
        }

        flushEdt()
        findLoadingDialog("Downloading v7.3.0...")!!.shouldNotBeVisible()
        val errorDialog = getErrorDialog()
        val linkLabel = errorDialog.getChild<LinkLabel>()
        linkLabel.text shouldBe "<html><u>$DARTZEE_MANUAL_DOWNLOAD_URL/tag/v7.3.0</u></html>"
        errorDialog.clickOk(async = true)

        verifyNotCalled { runtime.exec(any<Array<String>>()) }

        val log = verifyLog(CODE_UPDATE_ERROR, Severity.ERROR)
        log.message.shouldStartWith("Caught kong.unirest.UnirestException")
    }

    @Test
    fun `Should show an error and log if launching shell script fails, but still exit`() {
        val server = startWebServer(MockResponse().setResponseCode(HttpStatus.OK))

        val runtime = mockk<Runtime>()
        val error = IOException("Argh")
        every { runtime.exec(any<Array<String>>()) } throws error

        runAsync {
            assertExits(0) {
                UpdateManager.doUpdate(
                    server.url("root").toString(),
                    UpdateMetadata("v7.3.0", 12345, TEST_JAR_FILE_NAME),
                    runtime,
                )
            }
        }

        val errorDialog = getErrorDialog()
        errorDialog.getDialogMessage() shouldBe
            "Failed to swap in updated file. \n\nDelete the old Dartzee.jar and rename $TEST_JAR_FILE_NAME -> Dartzee.jar"
        errorDialog.clickOk(async = true)

        val log = verifyLog(CODE_EXEC_ERROR, Severity.ERROR)
        log.errorObject shouldBe error
    }

    @Test
    fun `Should successfully update and launch update batch file on windows`() {
        DartsClient.operatingSystem = "windows"

        val updateFile = File("update.bat")
        updateFile.writeText("blah")

        val server = startWebServer(MockResponse().setResponseCode(HttpStatus.OK))
        val runtime = mockk<Runtime>(relaxed = true)

        runAsync {
            assertExits(0) {
                UpdateManager.doUpdate(
                    server.url("root").toString(),
                    UpdateMetadata("v7.3.0", 12345, TEST_JAR_FILE_NAME),
                    runtime,
                )
            }
        }

        File(TEST_JAR_FILE_NAME).shouldExist()

        val request = server.takeRequest()
        request.method shouldBe "GET"
        request.path shouldBe "/root/releases/assets/12345"

        verify {
            runtime.exec(arrayOf("cmd", "/c", "start", updateFile.absolutePath, TEST_JAR_FILE_NAME))
        }

        updateFile.readText() shouldBe javaClass.getResource("/update/update.bat")!!.readText()
    }

    @Test
    fun `Should successfully update and launch update shell script on linux`() {
        DartsClient.operatingSystem = "linux"

        val updateFile = File("update.sh")
        updateFile.writeText("blah")

        val server = startWebServer(MockResponse().setResponseCode(HttpStatus.OK))
        val runtime = mockk<Runtime>(relaxed = true)

        runAsync {
            assertExits(0) {
                UpdateManager.doUpdate(
                    server.url("root").toString(),
                    UpdateMetadata("v7.3.0", 12345, TEST_JAR_FILE_NAME),
                    runtime,
                )
            }
        }

        val request = server.takeRequest()
        request.method shouldBe "GET"
        request.path shouldBe "/root/releases/assets/12345"

        verify { runtime.exec(arrayOf("sh", updateFile.absolutePath, TEST_JAR_FILE_NAME)) }

        updateFile.readText() shouldBe javaClass.getResource("/update/update.sh")!!.readText()
    }

    @Test
    fun `Should download the jar then exit with the swap message on unsupported OS`() {
        DartsClient.operatingSystem = "foo"

        val server = startWebServer(MockResponse().setResponseCode(HttpStatus.OK))
        val runtime = mockk<Runtime>(relaxed = true)

        runAsync {
            assertExits(0) {
                UpdateManager.doUpdate(
                    server.url("root").toString(),
                    UpdateMetadata("v7.3.0", 12345, TEST_JAR_FILE_NAME),
                    runtime,
                )
            }
        }

        File(TEST_JAR_FILE_NAME).shouldExist()

        val request = server.takeRequest()
        request.method shouldBe "GET"
        request.path shouldBe "/root/releases/assets/12345"

        val errorDialog = getErrorDialog()
        errorDialog.getDialogMessage() shouldBe
            "Failed to swap in updated file. \n\nDelete the old Dartzee.jar and rename $TEST_JAR_FILE_NAME -> Dartzee.jar"
        errorDialog.clickOk(async = true)

        val log = verifyLog(CODE_EXEC_ERROR, Severity.ERROR)
        log.message shouldBe "Operating system unsupported: foo"
    }

    private fun startWebServer(response: MockResponse): MockWebServer {
        val server = MockWebServer()
        server.start()
        server.enqueue(response)

        return server
    }
}
