package dartzee.logging

import dartzee.core.helper.verifyNotCalled
import dartzee.helper.AbstractTest
import dartzee.makeLogRecord
import io.kotlintest.shouldNotThrowAny
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestLogDestinationElasticsearch: AbstractTest()
{
    @Test
    fun `Should queue up logs to be posted in the next run`()
    {
        val poster = mockPoster()
        val dest = LogDestinationElasticsearch(poster)

        val recordOne = makeLogRecord(loggingCode = LoggingCode("foo"))
        val recordTwo = makeLogRecord(loggingCode = LoggingCode("bar"))

        dest.log(recordOne)
        dest.log(recordTwo)

        verifyNotCalled { poster.postLog(any()) }

        dest.postPendingLogs()

        verify {
            poster.postLog(recordOne.toJsonString())
            poster.postLog(recordTwo.toJsonString())
        }
    }

    @Test
    fun `Should remove a log from the queue if it is successful`()
    {
        val poster = mockPoster()
        val dest = LogDestinationElasticsearch(poster)

        val log = makeLogRecord()

        dest.log(log)
        dest.postPendingLogs()
        verify { poster.postLog(log.toJsonString()) }

        clearAllMocks()

        dest.postPendingLogs()
        verifyNotCalled { poster.postLog(log.toJsonString()) }
    }

    @Test
    fun `Should leave a log on the queue to be reattempted if it fails`()
    {
        val poster = mockPoster(false)
        val dest = LogDestinationElasticsearch(poster)

        val log = makeLogRecord()

        dest.log(log)
        dest.postPendingLogs()
        verify { poster.postLog(log.toJsonString()) }

        clearAllMocks()

        dest.postPendingLogs()
        verify { poster.postLog(log.toJsonString()) }
    }

    @Test
    fun `Should handle not having a poster if something goes wrong during startup`()
    {
        shouldNotThrowAny {
            val dest = LogDestinationElasticsearch(null)
            dest.log(makeLogRecord())
            dest.postPendingLogs()
        }
    }

    @Test
    fun `Should kick off posting logs immediately`()
    {
        val poster = mockPoster()
        val dest = LogDestinationElasticsearch(poster)

        val log = makeLogRecord()

        dest.log(log)
        dest.startPosting()
        verify { poster.postLog(log.toJsonString()) }
    }

    private fun mockPoster(success: Boolean = true): ElasticsearchPoster
    {
        val poster = mockk<ElasticsearchPoster>(relaxed = true)
        every { poster.postLog(any()) } returns success
        return poster
    }
}
