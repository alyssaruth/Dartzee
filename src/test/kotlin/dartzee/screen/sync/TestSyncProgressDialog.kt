package dartzee.screen.sync

import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import dartzee.helper.AbstractTest
import dartzee.sync.SyncStage
import io.kotest.matchers.shouldBe
import javax.swing.JProgressBar
import org.junit.jupiter.api.Test

class TestSyncProgressDialog : AbstractTest() {
    @Test
    fun `Should show and hide`() {
        val dlg = SyncProgressDialog()
        dlg.setVisibleLater()
        flushEdt()
        dlg.shouldBeVisible()

        dlg.disposeLater()
        flushEdt()
        dlg.shouldNotBeVisible()
    }

    @Test
    fun `Should update correctly when progressing to stages`() {
        val dlg = SyncProgressDialog()
        val progressBar = dlg.getChild<JProgressBar>()

        dlg.progressToStage(SyncStage.PULL_REMOTE)
        flushEdt()
        progressBar.value shouldBe 0
        progressBar.string shouldBe "Stage 1: Download data"

        dlg.progressToStage(SyncStage.VALIDATE_REMOTE)
        flushEdt()
        progressBar.value shouldBe 1
        progressBar.string shouldBe "Stage 2: Validate database"

        dlg.progressToStage(SyncStage.MERGE_LOCAL_CHANGES)
        flushEdt()
        progressBar.value shouldBe 2
        progressBar.string shouldBe "Stage 3: Merge changes"

        dlg.progressToStage(SyncStage.UPDATE_ACHIEVEMENTS)
        flushEdt()
        progressBar.value shouldBe 3
        progressBar.string shouldBe "Stage 4: Update achievements"

        dlg.progressToStage(SyncStage.PUSH_TO_REMOTE)
        flushEdt()
        progressBar.value shouldBe 4
        progressBar.string shouldBe "Stage 5: Upload new version"

        dlg.progressToStage(SyncStage.PUSH_BACKUP_TO_REMOTE)
        flushEdt()
        progressBar.value shouldBe 5
        progressBar.string shouldBe "Stage 6: Upload backup"

        dlg.progressToStage(SyncStage.OVERWRITE_LOCAL)
        flushEdt()
        progressBar.value shouldBe 6
        progressBar.string shouldBe "Stage 7: Finalise"
    }
}
