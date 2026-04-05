package dartzee.db

import dartzee.core.util.DateStatics
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDatabaseMigrationV23toV24 : AbstractTest() {
    @Test
    fun `Should add DateOfBirth column with end of time as default`() {
        insertPlayer()

        mainDatabase.executeUpdate("ALTER TABLE Player DROP COLUMN DateOfBirth")
        dartzee.helper.runConversion(23)

        val retrieved = PlayerEntity().retrieveEntities().first()
        retrieved.dateOfBirth shouldBe DateStatics.END_OF_TIME
    }
}
