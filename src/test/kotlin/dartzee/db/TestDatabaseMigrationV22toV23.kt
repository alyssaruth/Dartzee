package dartzee.db

import dartzee.game.FinishType
import dartzee.game.GameType
import dartzee.game.X01Config
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.runConversion
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDatabaseMigrationV22toV23 : AbstractTest() {
    @Test
    fun `Should convert games of X01`() {
        val g = insertGame(gameType = GameType.X01, gameParams = "701")

        runConversion(22)

        val retrieved = GameEntity().retrieveForId(g.rowId)!!
        retrieved.gameParams shouldBe X01Config(701, FinishType.Doubles).toJson()
    }

    @Test
    fun `Should leave other game types alone`() {
        val g = insertGame(gameType = GameType.GOLF, gameParams = "18")

        runConversion(22)

        val retrieved = GameEntity().retrieveForId(g.rowId)!!
        retrieved.gameParams shouldBe "18"
    }
}
