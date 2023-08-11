package dartzee.db

import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestSqlStatementType : AbstractTest()
{
    @Test
    fun `Should parse correct type from statement`()
    {
        SqlStatementType.fromStatement("SELECT * FROM Foo") shouldBe SqlStatementType.SELECT
        SqlStatementType.fromStatement("INSERT INTO Foo") shouldBe SqlStatementType.INSERT
        SqlStatementType.fromStatement("UPDATE Foo") shouldBe SqlStatementType.UPDATE
        SqlStatementType.fromStatement("DELETE FROM Foo") shouldBe SqlStatementType.DELETE
    }
}