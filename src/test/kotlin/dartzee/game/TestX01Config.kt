package dartzee.game

import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestX01Config : AbstractTest() {
    @Test
    fun `Should serialize and deserialize correctly`() {
        val config = X01Config(701, FinishType.Any)
        val json = config.toJson()

        val newConfig = X01Config.fromJson(json)
        newConfig shouldBe config
    }

    @Test
    fun `Should deserialize correctly`() {
        val json = """{ "target": 301, "finishType": "Doubles" }"""
        val config = X01Config.fromJson(json)
        config.target shouldBe 301
        config.finishType shouldBe FinishType.Doubles
    }
}
