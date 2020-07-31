package dartzee.db

import dartzee.ai.DartsAiModel
import dartzee.core.util.DateStatics
import dartzee.helper.insertPlayer
import dartzee.helper.randomGuid
import dartzee.logging.CODE_SQL
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldThrow
import org.junit.Test

class TestParticipantEntity: AbstractEntityTest<ParticipantEntity>()
{
    override fun factoryDao() = ParticipantEntity()

    @Test
    fun `Should cache the player on first query then use it from then on`()
    {
        val playerId = insertPlayer(name = "Bob", strategy = "").rowId

        val pt = ParticipantEntity()
        pt.playerId = playerId

        val player = pt.getPlayer()
        player shouldNotBe null

        val log = getLastLog()
        log.message shouldContain "WHERE RowId = '$playerId'"

        clearLogs()

        pt.getPlayer() shouldBe player
        verifyNoLogs(CODE_SQL)
    }

    @Test
    fun `Should return correct values for an AI player`()
    {
        val aiId = insertPlayer(name = "Robot",
                strategy = DartsAiModel().writeXml()).rowId

        val pt = ParticipantEntity()
        pt.playerId = aiId

        pt.isAi() shouldBe true
        pt.getPlayerName() shouldBe "Robot"
        pt.getModel() shouldNotBe null
    }

    @Test
    fun `Should return correct values for a human player`()
    {
        val player = PlayerEntity()
        player.name = "Bob"
        player.strategy = ""

        val pt = ParticipantEntity()
        pt.setPlayer(player)

        pt.isAi() shouldBe false
        pt.getPlayerName() shouldBe "Bob"
        shouldThrow<Exception>{
            pt.getModel()
        }
    }

    @Test
    fun `Factory and save`()
    {
        val player = PlayerEntity()
        val playerId = player.assignRowId()
        player.name = "Stuart"

        val gameId = randomGuid()

        val pt = ParticipantEntity.factoryAndSave(gameId, player, 2)
        val rowId = pt.rowId

        pt.gameId shouldBe gameId
        pt.playerId shouldBe playerId
        pt.ordinal shouldBe 2
        pt.getPlayer() shouldBe player
        pt.dtFinished shouldBe DateStatics.END_OF_TIME
        pt.finalScore shouldBe -1
        pt.finishingPosition shouldBe -1

        pt.retrieveForId(rowId) shouldNotBe null
    }
}