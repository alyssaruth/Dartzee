package dartzee.db.sanity

import dartzee.core.util.DateStatics.END_OF_TIME
import dartzee.core.util.getSqlDateNow
import dartzee.helper.AbstractTest
import dartzee.helper.insertParticipant
import dartzee.only
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestSanityCheckFinishedParticipantNoScore : AbstractTest() {
    @Test
    fun `should flag unexpected rows`() {
        insertParticipant(dtFinished = getSqlDateNow(), finalScore = -1, resigned = false)

        val result = SanityCheckFinishedParticipantsNoScore().runCheck().only()
        result.getCount() shouldBe 1
    }

    @Test
    fun `should not flag rows that are ok`() {
        insertParticipant(dtFinished = END_OF_TIME, finalScore = -1, resigned = false)
        insertParticipant(dtFinished = getSqlDateNow(), finalScore = -1, resigned = true)
        insertParticipant(dtFinished = getSqlDateNow(), finalScore = 10, resigned = false)

        val result = SanityCheckFinishedParticipantsNoScore().runCheck()
        result.shouldBeEmpty()
    }
}
