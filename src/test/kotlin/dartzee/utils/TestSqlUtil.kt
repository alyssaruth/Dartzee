package dartzee.utils

import dartzee.helper.AbstractTest
import dartzee.helper.insertParticipant
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestSqlUtil: AbstractTest()
{
    @Test
    fun `Should get quoted ID strings`()
    {
        val list = listOf("foo", "bar")
        list.getQuotedIdStr() shouldBe "('foo', 'bar')"
    }

    @Test
    fun `Should get correct quoted ID strings of objects`()
    {
        val ptOne = insertParticipant(uuid = "foo", playerId = "playerA")
        val ptTwo = insertParticipant(uuid = "bar", playerId = "playerB")

        val pts = listOf(ptOne, ptTwo)
        pts.getQuotedIdStr { it.rowId } shouldBe "('foo', 'bar')"
        pts.getQuotedIdStr { it.playerId } shouldBe "('playerA', 'playerB')"

        listOf(ptOne).getQuotedIdStr { it.rowId } shouldBe "('foo')"
    }
}