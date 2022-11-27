package dartzee.db

import dartzee.helper.AbstractTest
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.runConversion
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test

class TestDatabaseMigrationV20toV21: AbstractTest()
{
    @Test
    fun `Should correctly drop old columns`()
    {
        val dart = insertDart(insertParticipant())

        mainDatabase.executeUpdate("ALTER TABLE Dart ADD COLUMN PosX INT DEFAULT 0")
        mainDatabase.executeUpdate("ALTER TABLE Dart ADD COLUMN PosY INT DEFAULT 0")

        runConversion(20)

        val retrieved = DartEntity().retrieveForId(dart.rowId)
        retrieved.shouldNotBeNull()
    }
}