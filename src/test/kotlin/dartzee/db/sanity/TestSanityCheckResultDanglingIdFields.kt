package dartzee.db.sanity

import com.github.alyssaburlton.swingtest.clickNo
import com.github.alyssaburlton.swingtest.clickYes
import dartzee.db.EntityName
import dartzee.db.GameEntity
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.getInfoDialog
import dartzee.getQuestionDialog
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.helper.insertGame
import dartzee.helper.randomGuid
import dartzee.runAsync
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class TestSanityCheckResultDanglingIdFields : AbstractTest() {
    @Test
    fun `Should allow auto fix to be cancelled`() {
        val games = (1..10).map { insertGame() }
        val result = SanityCheckResultDanglingIdFields("dartsMatchId", EntityName.DartsMatch, games)
        runAsync { result.autoFix() }

        val q = getQuestionDialog()
        q.getDialogMessage() shouldBe "Are you sure you want to delete 10 rows from Game?"
        q.clickNo()

        getCountFromTable(EntityName.Game) shouldBe 10
    }

    @Test
    fun `Should auto fix successfully`() {
        val games = (1..10).map { insertGame() }
        val result = SanityCheckResultDanglingIdFields("dartsMatchId", EntityName.DartsMatch, games)
        runAsync { result.autoFix() }

        val q = getQuestionDialog()
        q.getDialogMessage() shouldBe "Are you sure you want to delete 10 rows from Game?"
        q.clickYes(async = true)

        getCountFromTable(EntityName.Game) shouldBe 0
        getCountFromTable(EntityName.DeletionAudit) shouldBe 10

        val i = getInfoDialog()
        i.getDialogMessage() shouldBe
            "Rows deleted successfully. You should re-run the sanity check."
    }

    @Test
    fun `Should show an error if something goes wrong`() {
        val mockEntityName = mockk<EntityName>()
        every { mockEntityName.name } returns "Foo"
        every { mockEntityName.toString() } returns "Foo"

        val g = mockk<GameEntity>(relaxed = true)
        every { g.rowId } returns randomGuid()
        every { g.getTableName() } returns mockEntityName

        val result =
            SanityCheckResultDanglingIdFields("dartsMatchId", EntityName.DartsMatch, listOf(g))
        runAsync { result.autoFix() }

        val q = getQuestionDialog()
        q.getDialogMessage() shouldBe "Are you sure you want to delete 1 rows from Foo?"
        q.clickYes(async = true)

        val e = getErrorDialog()
        e.getDialogMessage() shouldBe "An error occurred deleting the rows."
        errorLogged() shouldBe true
    }
}
