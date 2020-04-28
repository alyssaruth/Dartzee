package dartzee.logging

import dartzee.core.helper.verifyNotCalled
import dartzee.db.PendingLogsEntity
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.makeLogRecord
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowAny
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class TestLogDestinationElasticsearch: AbstractTest()
{
    @Test
    fun `Should queue up logs to be posted in the next run`()
    {
        val poster = mockPoster()
        val dest = makeLogDestination(poster)

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
        val dest = makeLogDestination(poster)

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
        val dest = makeLogDestination(poster)

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
            val dest = makeLogDestination(null)
            dest.log(makeLogRecord())
            dest.postPendingLogs()
        }
    }

    @Test
    fun `Should kick off posting logs immediately`()
    {
        val poster = mockPoster()
        val dest = LogDestinationElasticsearch(poster, Executors.newScheduledThreadPool(1))

        val log = makeLogRecord()

        dest.log(log)
        dest.startPosting()
        verify { poster.postLog(log.toJsonString()) }
    }

    @Test
    fun `Should schedule the posting of logs`()
    {
        val scheduler = mockk<ScheduledExecutorService>(relaxed = true)
        val dest = LogDestinationElasticsearch(mockPoster(), scheduler)

        dest.startPosting()

        verify { scheduler.scheduleAtFixedRate(any(), 0, 5, TimeUnit.SECONDS) }
    }

    @Test
    fun `Should read in and delete from the pending logs table`()
    {
        val logJson = makeLogRecord().toJsonString()
        PendingLogsEntity.factory(logJson).saveToDatabase()

        val poster = mockPoster()
        val dest = makeLogDestination(poster)

        dest.readOldLogs()
        dest.postPendingLogs()

        verify { poster.postLog(logJson) }
        getCountFromTable("PendingLogs") shouldBe 0
    }

    @Test
    fun `Should shut down and write out unsent logs`()
    {
        val scheduler = mockk<ScheduledExecutorService>(relaxed = true)
        val dest = makeLogDestination(mockPoster(), scheduler)

        val record = makeLogRecord(loggingCode = LoggingCode("tooLate"))
        dest.log(record)

        dest.shutDown()

        verify { scheduler.shutdown() }

        val pendingLogs = PendingLogsEntity().retrieveEntities()
        pendingLogs.size shouldBe 1
        pendingLogs.first().logJson shouldBe record.toJsonString()
    }

    private fun makeLogDestination(poster: ElasticsearchPoster?,
                                   scheduler: ScheduledExecutorService = mockk(relaxed = true)) =
            LogDestinationElasticsearch(poster, scheduler)

    private fun mockPoster(success: Boolean = true): ElasticsearchPoster
    {
        val poster = mockk<ElasticsearchPoster>(relaxed = true)
        every { poster.postLog(any()) } returns success
        return poster
    }
}
