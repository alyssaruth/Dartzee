package dartzee.utils

import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.logging.CODE_NO_STREAMS
import dartzee.logging.Severity
import dartzee.screen.animation.Animation
import dartzee.screen.animation.BRUCEY_BAD_LUCK
import dartzee.screen.animation.BadLuckTrigger
import dartzee.screen.animation.TotalScoreTrigger
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class TestResourceCache : AbstractTest() {
    @Test
    fun `Should pre-load all the WAVs that will be used for animations`() {
        InjectedThings.animations = mapOf(BadLuckTrigger to BRUCEY_BAD_LUCK)
        ResourceCache.initialiseResources()

        ResourceCache.borrowInputStream("badLuck1") shouldNotBe null
        ResourceCache.borrowInputStream("badLuck2") shouldNotBe null
        ResourceCache.borrowInputStream("bull") shouldBe null
    }

    @Test
    fun `Should pre-load 3 instances of each WAV`() {
        InjectedThings.animations =
            mapOf(TotalScoreTrigger(GameType.X01, 100) to Animation("100", null))
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
    fun `Should re-use WAVs that are returned to the pool`() {
        InjectedThings.animations =
            mapOf(TotalScoreTrigger(GameType.X01, 100) to Animation("100", null))
        ResourceCache.initialiseResources()

        val wav1 = ResourceCache.borrowInputStream("100")!!
        ResourceCache.borrowInputStream("100")
        ResourceCache.borrowInputStream("100")

        // Return one and borrow again, check we get the same instance
        ResourceCache.returnInputStream("100", wav1)
        val newWav = ResourceCache.borrowInputStream("100")
        newWav shouldBe wav1

        verifyNoLogs(CODE_NO_STREAMS)
    }
}
