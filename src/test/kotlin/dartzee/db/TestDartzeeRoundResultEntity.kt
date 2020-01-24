package dartzee.db

import dartzee.dartzee.DartzeeRoundResult
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test
import kotlin.test.assertNotNull

class TestDartzeeRoundResultEntity: AbstractEntityTest<DartzeeRoundResultEntity>()
{
    override fun factoryDao() = DartzeeRoundResultEntity()

    @Test
    fun `Should be indexed on PlayerId_ParticipantId_RoundNumber`()
    {
        val indexes = mutableListOf<List<String>>()
        factoryDao().addListsOfColumnsForIndexes(indexes)

        indexes.first().shouldContainExactly("PlayerId", "ParticipantId", "RoundNumber")
    }

    @Test
    fun `Should convert to a DTO correctly`()
    {
        val entity = DartzeeRoundResultEntity()
        entity.roundNumber = 5
        entity.ruleNumber = 2
        entity.success = true
        entity.score = 57

        val dto = entity.toDto()

        dto.ruleNumber shouldBe 2
        dto.success shouldBe true
        dto.score shouldBe 57
    }

    @Test
    fun `Should save from a DTO correctly`()
    {
        val dto = DartzeeRoundResult(5, false, -100)

        val player = insertPlayer()
        val pt = insertParticipant(playerId = player.rowId)

        DartzeeRoundResultEntity.factoryAndSave(dto, pt, 10)

        val entity = DartzeeRoundResultEntity().retrieveEntities().first()
        assertNotNull(entity)

        entity.roundNumber shouldBe 10
        entity.participantId shouldBe pt.rowId
        entity.playerId shouldBe pt.playerId
        entity.ruleNumber shouldBe 5
        entity.success shouldBe false
        entity.score shouldBe -100
    }
}