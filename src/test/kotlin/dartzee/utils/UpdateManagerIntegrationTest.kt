package dartzee.utils

import dartzee.helper.AbstractTest
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import java.io.File
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

@Tag("integration")
class UpdateManagerIntegrationTest : AbstractTest() {
    @Test
    fun `Should retrieve a valid latest asset from the remote repo`() {
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
    @Timeout(2, unit = TimeUnit.MINUTES)
    fun `Should successfully download jar for latest release`() {
        val responseJson = UpdateManager.queryLatestReleaseJson(DARTZEE_REPOSITORY_URL)!!
        val metadata = UpdateManager.parseUpdateMetadata(responseJson)!!
        val expectedLength = responseJson.getJSONArray("assets").getJSONObject(0).getLong("size")

        try {
            val result = UpdateManager.downloadJar(DARTZEE_REPOSITORY_URL, metadata)
            result shouldBe true

            val file = File(metadata.fileName)
            file.shouldExist()
            file.length() shouldBe expectedLength
        } finally {
            File(metadata.fileName).delete()
        }
    }
}
