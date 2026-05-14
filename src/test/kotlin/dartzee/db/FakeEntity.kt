package dartzee.db

import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import io.mockk.every
import io.mockk.mockk

class FakeEntity(database: Database = mainDatabase) : AbstractEntity<FakeEntity>(database) {
    var testString = ""

    override fun getTableName(): EntityName {
        val result = mockk<EntityName>(relaxed = true)
        every { result.name } returns "TestTable"
        every { result.toString() } returns "TestTable"
        return result
    }

    override fun getCreateTableSqlSpecific() = "TestString VARCHAR(10) NOT NULL"
}
