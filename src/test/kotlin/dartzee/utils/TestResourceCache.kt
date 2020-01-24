package dartzee.utils

import dartzee.core.helper.getLogs
import dartzee.helper.AbstractRegistryTest
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader

class TestResourceCache : AbstractRegistryTest()
{
    override fun beforeEachTest()
    {
        super.beforeEachTest()
        ResourceCache.resetCache()
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_PRE_LOAD_RESOURCES, true)
    }

    override fun getPreferencesAffected() = listOf(PREFERENCES_BOOLEAN_PRE_LOAD_RESOURCES)

    @Test
    fun `Should not pre-load any WAVs if the preference is disabled`()
    {
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_PRE_LOAD_RESOURCES, false)

        ResourceCache.initialiseResources()
        getLogs() shouldContain "Not pre-loading WAVs as preference is disabled"

        ResourceCache.isInitialised shouldBe false
        val stream = ResourceCache.borrowInputStream("140")
        stream shouldBe null
    }

    @Test
    fun `Should pre-load all the WAVs in the appropriate resource directory`()
    {
        val resources = mutableListOf<String>()

        javaClass.getResourceAsStream("/wav").use { stream ->
            BufferedReader(InputStreamReader(stream)).use { br ->
                var resource = br.readLine()
                while (resource != null)
                {
                    resources.add(resource)
                    resource = br.readLine()
                }
            }
        }

        ResourceCache.initialiseResources()

        resources.forEach {
            val resourceName = it.replace(".wav", "")
            ResourceCache.borrowInputStream(resourceName) shouldNotBe null
        }
    }

    @Test
    fun `Should pre-load 3 instances of each WAV`()
    {
        ResourceCache.initialiseResources()

        ResourceCache.borrowInputStream("100") shouldNotBe null
        ResourceCache.borrowInputStream("100") shouldNotBe null
        ResourceCache.borrowInputStream("100") shouldNotBe null
        getLogs() shouldNotContain "No streams left for WAV [100], will spawn another"

        ResourceCache.borrowInputStream("100") shouldNotBe null
        getLogs() shouldContain "No streams left for WAV [100], will spawn another"
    }

    @Test
    fun `Should re-use WAVs that are returned to the pool`()
    {
        ResourceCache.initialiseResources()

        val wav1 = ResourceCache.borrowInputStream("100")!!
        ResourceCache.borrowInputStream("100")
        ResourceCache.borrowInputStream("100")

        //Return one and borrow again, check we get the same instance
        ResourceCache.returnInputStream("100", wav1)
        val newWav = ResourceCache.borrowInputStream("100")
        newWav shouldBe wav1

        getLogs() shouldNotContain "No streams left for WAV [100], will spawn another"
    }

    @Test
    fun `Should show a loading dialog whilst initialising`()
    {
        ResourceCache.initialiseResources()

        dialogFactory.loadingsShown.shouldContainExactly("Loading resources...")
    }

    @Test
    fun `Should return null for a resource that isn't in the cache`()
    {
        ResourceCache.initialiseResources()

        ResourceCache.borrowInputStream("50") shouldBe null
    }

}