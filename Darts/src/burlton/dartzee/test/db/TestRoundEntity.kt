package burlton.dartzee.test.db

import burlton.dartzee.code.db.ParticipantEntity
import burlton.dartzee.code.db.RoundEntity
import burlton.dartzee.test.helper.getCountFromTable
import burlton.dartzee.test.helper.wipeTable
import io.kotlintest.matchers.string.shouldNotBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test

class TestRoundEntity: AbstractEntityTest<RoundEntity>()
{
    override fun factoryDao() = RoundEntity()

    @Test
    fun `Should check participantId and compare to participant`()
    {
        val pt = ParticipantEntity()
        val ptId = pt.assignRowId()

        val round = RoundEntity()
        round.isForParticipant(pt) shouldBe false

        round.participantId = ptId
        round.isForParticipant(pt) shouldBe true
    }

    @Test
    fun `Factory should set the right fields but NOT save`()
    {
        wipeTable("Round")

        val pt = ParticipantEntity()
        val ptId = pt.assignRowId()


        val round = RoundEntity.factory(pt, 8)
        round.rowId.shouldNotBeEmpty()
        round.participantId shouldBe ptId
        round.roundNumber shouldBe 8

        getCountFromTable("Round") shouldBe 0
    }
}
