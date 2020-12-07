package dartzee.db

import dartzee.`object`.DartsClient
import dartzee.core.util.CoreRegistry
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.string.shouldNotBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp
import java.util.*

class TestSyncAuditEntity: AbstractEntityTest<SyncAuditEntity>()
{
    private val originalDeviceId = CoreRegistry.instance.get(CoreRegistry.INSTANCE_STRING_DEVICE_ID, "")
    private val originalUsername = CoreRegistry.instance.get(CoreRegistry.INSTANCE_STRING_USER_NAME, "")

    override fun afterEachTest()
    {
        super.afterEachTest()
        CoreRegistry.instance.put(CoreRegistry.INSTANCE_STRING_DEVICE_ID, originalDeviceId)
        CoreRegistry.instance.put(CoreRegistry.INSTANCE_STRING_USER_NAME, originalUsername)
    }

    override fun factoryDao() = SyncAuditEntity()

    @Test
    fun `Should report a null lastSyncDate if not recorded before`()
    {
        SyncAuditEntity.getLastSyncDate(mainDatabase, "Test") shouldBe null
    }

    @Test
    fun `Should insert a sync audit with the correct values`()
    {
        val deviceId = UUID.randomUUID().toString()
        DartsClient.operatingSystem = "Funky OS 2.1"
        CoreRegistry.instance.put(CoreRegistry.INSTANCE_STRING_DEVICE_ID, deviceId)
        CoreRegistry.instance.put(CoreRegistry.INSTANCE_STRING_USER_NAME, "Bob")

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
    fun `Should report the correct last sync date, taking into account remote name`()
    {
        makeSyncAudit(mainDatabase, "Goomba").saveToDatabase(Timestamp(50))
        makeSyncAudit(mainDatabase, "Goomba").saveToDatabase(Timestamp(150))

        makeSyncAudit(mainDatabase, "Koopa").saveToDatabase(Timestamp(100))

        SyncAuditEntity.getLastSyncDate(mainDatabase, "Goomba") shouldBe Timestamp(150)
        SyncAuditEntity.getLastSyncDate(mainDatabase, "Koopa") shouldBe Timestamp(100)
        SyncAuditEntity.getLastSyncDate(mainDatabase, "Toad") shouldBe null
    }

    private fun makeSyncAudit(database: Database, remoteName: String)
        = SyncAuditEntity(database).also {
        it.assignRowId()
        it.remoteName = remoteName
    }
}