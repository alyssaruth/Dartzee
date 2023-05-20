package dartzee.db

import dartzee.helper.AbstractTest
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.runConversion
import dartzee.`object`.SegmentType
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class TestDatabaseMIgrationV21toV22 : AbstractTest()
{
    @Test
    fun `Should update MISSED_BOARD darts to just MISS`()
    {
        val segmentType = mockk<SegmentType>()
        every { segmentType.toString() } returns "MISSED_BOARD"
        val dart = insertDart(insertParticipant(), segmentType = segmentType)

        runConversion(21)
        val retrieved = DartEntity().retrieveForId(dart.rowId)
        retrieved.shouldNotBeNull()
        retrieved.segmentType shouldBe SegmentType.MISS
    }

    @Test
    fun `Should leave other segment types alone`()
    {
        SegmentType.values().forEach { segmentType ->
            wipeDatabase()
            val dart = insertDart(insertParticipant(), segmentType = segmentType)

            runConversion(21)
            val retrieved = DartEntity().retrieveForId(dart.rowId)!!
            retrieved.segmentType shouldBe segmentType
        }
    }
}