package burlton.dartzee.test.db

import burlton.core.code.util.Debug.clearLogs
import burlton.core.test.helper.exceptionLogged
import burlton.core.test.helper.getLogs
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.ai.DartsModelNormalDistribution
import burlton.dartzee.code.db.ParticipantEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.helper.insertPlayer
import burlton.dartzee.test.helper.randomGuid
import io.kotlintest.matchers.string.shouldBeEmpty
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
        val playerId = insertPlayer(name = "Bob", strategy = -1)

        val pt = ParticipantEntity()
        pt.playerId = playerId

        val player = pt.getPlayer()
        player shouldNotBe null
        getLogs().shouldContain("WHERE RowId = '$playerId'")

        clearLogs()

        pt.getPlayer() shouldBe player
        getLogs().shouldBeEmpty()
    }

    @Test
    fun `Should return correct values for an AI player`()
    {
        val aiId = insertPlayer(name = "Robot",
                strategy = AbstractDartsModel.TYPE_NORMAL_DISTRIBUTION,
                strategyXml = DartsModelNormalDistribution().writeXml())

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
        player.strategy = -1

        val pt = ParticipantEntity()
        pt.setPlayer(player)

        pt.isAi() shouldBe false
        pt.getPlayerName() shouldBe "Bob"
        shouldThrow<Exception>{
            pt.getModel()
        }

        exceptionLogged() shouldBe true
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

        pt.retrieveForId(rowId) shouldNotBe null
    }
}