package dartzee.db

import dartzee.ai.DartsAiModel
import dartzee.game.FinishType
import dartzee.game.GameType
import dartzee.game.X01Config
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.insertTeam
import dartzee.helper.runConversion
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDatabaseMigrationV22toV23 : AbstractTest() {
    @Test
    fun `Should drop old X01 aim parameter`() {
        val raw =
            """{"standardDeviation":155.0,"standardDeviationDoubles":160.0,"standardDeviationCentral":80.0,"maxRadius":136,"scoringDart":20,"hmScoreToDart":{},"hmDartNoToSegmentType":{"1":"DOUBLE","2":"TREBLE","3":"OUTER_SINGLE"},"hmDartNoToStopThreshold":{"1":3,"2":4},"dartzeePlayStyle":"AGGRESSIVE"}"""

        val expected =
            """{"standardDeviation":155.0,"standardDeviationDoubles":160.0,"standardDeviationCentral":80.0,"maxRadius":136,"scoringDart":20,"hmDartNoToSegmentType":{"1":"DOUBLE","2":"TREBLE","3":"OUTER_SINGLE"},"hmDartNoToStopThreshold":{"1":3,"2":4},"dartzeePlayStyle":"AGGRESSIVE"}"""

        val p = insertPlayer(strategy = raw)

        runConversion()

        val retrieved = PlayerEntity().retrieveForId(p.rowId)!!
        retrieved.strategy shouldBe expected

        shouldNotThrowAny { DartsAiModel.fromJson(retrieved.strategy) }
    }

    @Test
    fun `Should convert games of X01`() {
        val g = insertGame(gameType = GameType.X01, gameParams = "701")

        runConversion()

        val retrieved = GameEntity().retrieveForId(g.rowId)!!
        retrieved.gameParams shouldBe X01Config(701, FinishType.Doubles).toJson()
    }

    @Test
    fun `Should leave other game types alone`() {
        val g = insertGame(gameType = GameType.GOLF, gameParams = "18")

        runConversion()

        val retrieved = GameEntity().retrieveForId(g.rowId)!!
        retrieved.gameParams shouldBe "18"
    }

    @Test
    fun `Should add resigned columns`() {
        val pt = insertParticipant()
        val t = insertTeam()

        runConversion()

        val retrievedPt = ParticipantEntity().retrieveForId(pt.rowId)!!
        retrievedPt.resigned shouldBe false

        val retrievedTeam = TeamEntity().retrieveForId(t.rowId)!!
        retrievedTeam.resigned shouldBe false
    }

    private fun runConversion() {
        mainDatabase.executeUpdate("ALTER TABLE Participant DROP COLUMN Resigned")
        mainDatabase.executeUpdate("ALTER TABLE Team DROP COLUMN Resigned")
        runConversion(22)
    }
}
