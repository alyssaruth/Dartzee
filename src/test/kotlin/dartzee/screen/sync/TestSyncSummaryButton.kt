package dartzee.screen.sync

import com.github.alexburlton.swingtest.doHover
import com.github.alexburlton.swingtest.doHoverAway
import dartzee.helper.AbstractTest
import dartzee.helper.REMOTE_NAME
import dartzee.sync.SyncSummary
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestSyncSummaryButton: AbstractTest()
{
    @Test
    fun `Should change text on hover`()
    {
        val button = SyncSummaryButton()
        button.refreshSummary(SyncSummary("-", "-", "-"))

        val text = button.text
        button.doHover()
        button.text shouldBe "<html><h1>Sync Settings &gt;</h1></html>"

        button.doHoverAway()
        button.text shouldBe text
    }

    @Test
    fun `Should set text based on sync summary`()
    {
        val button = SyncSummaryButton()
        button.refreshSummary(SyncSummary(REMOTE_NAME, "yesterday", "6"))

        val text = button.text
        text shouldContain "<b>Syncing with: </b> $REMOTE_NAME"
        text shouldContain "<b>Last synced: </b> yesterday"
        text shouldContain "<b>Pending games: </b> 6"
    }
}