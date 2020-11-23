package dartzee.sync

import dartzee.helper.AbstractRegistryTest
import dartzee.utils.PREFERENCES_STRING_REMOTE_DATABASE_NAME
import io.kotlintest.shouldBe
import org.junit.Test

class TestSyncUtils: AbstractRegistryTest()
{
    override fun getPreferencesAffected() = listOf(PREFERENCES_STRING_REMOTE_DATABASE_NAME)

    @Test
    fun `Should return an empty string if no remote db name set`()
    {
        saveRemoteName("")
        getRemoteName() shouldBe ""
    }

    @Test
    fun `Should be able to save and retrieve the remote db name`()
    {
        saveRemoteName("Goomba")
        getRemoteName() shouldBe "Goomba"
    }
}