package dartzee.db

import dartzee.helper.REMOTE_NAME
import dartzee.helper.makeSyncAudit
import dartzee.`object`.DartsClient
import dartzee.preferences.Preferences
import dartzee.sync.LastSyncData
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.InjectedThings.preferenceService
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import java.sql.Timestamp
import java.util.*
import org.junit.jupiter.api.Test

class TestSyncAuditEntity : AbstractEntityTest<SyncAuditEntity>() {
    override fun factoryDao() = SyncAuditEntity()

    @Test
    fun `Should report a null lastSyncDate if not recorded before`() {
        SyncAuditEntity.getLastSyncData(mainDatabase) shouldBe null
    }

    @Test
    fun `Should insert a sync audit with the correct values`() {
        val deviceId = UUID.randomUUID().toString()
        DartsClient.operatingSystem = "Funky OS 2.1"
        preferenceService.save(Preferences.deviceId, deviceId)
        System.setProperty("user.name", "Bob")

        SyncAuditEntity.insertSyncAudit(mainDatabase, "Goomba")

        val entry = SyncAuditEntity(mainDatabase).retrieveEntity("1 = 1")!!
        entry.rowId.shouldNotBeEmpty()
        entry.operatingSystem shouldBe "Funky OS 2.1"
        entry.username shouldBe "Bob"
        entry.deviceId shouldBe deviceId
        entry.appVersion shouldBe DARTS_VERSION_NUMBER
        entry.remoteName shouldBe "Goomba"
    }

    @Test
    fun `Should report the correct last sync date and remote name`() {
        makeSyncAudit(mainDatabase).saveToDatabase(Timestamp(50))
        makeSyncAudit(mainDatabase).saveToDatabase(Timestamp(150))

        SyncAuditEntity.getLastSyncData(mainDatabase) shouldBe
            LastSyncData(REMOTE_NAME, Timestamp(150))
    }
}
