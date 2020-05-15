package dartzee.utils

import dartzee.helper.AbstractTest
import dartzee.logging.CODE_NO_STREAMS
import dartzee.logging.Severity
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader

class TestResourceCache : AbstractTest()
{
    override fun beforeEachTest()
    {
        super.beforeEachTest()
        ResourceCache.resetCache()
    }

    override fun afterEachTest()
    {
        super.afterEachTest()
        ResourceCache.resetCache()
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
        verifyNoLogs(CODE_NO_STREAMS)

        ResourceCache.borrowInputStream("100") shouldNotBe null
        val log = verifyLog(CODE_NO_STREAMS, Severity.WARN)
        log.message shouldBe "No streams left for WAV [100], will spawn another"
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

        verifyNoLogs(CODE_NO_STREAMS)
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