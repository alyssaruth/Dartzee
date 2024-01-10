package dartzee.db

import dartzee.core.util.DateStatics
import dartzee.helper.randomGuid
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class TestTeamEntity : AbstractEntityTest<TeamEntity>() {
    override fun factoryDao() = TeamEntity()

    @Test
    fun `Factory and save`() {
        val gameId = randomGuid()

        val tean = TeamEntity.factoryAndSave(gameId, 2)
        val rowId = tean.rowId

        tean.gameId shouldBe gameId
        tean.ordinal shouldBe 2
        tean.dtFinished shouldBe DateStatics.END_OF_TIME
        tean.finalScore shouldBe -1
        tean.finishingPosition shouldBe -1

        tean.retrieveForId(rowId) shouldNotBe null
    }
}
