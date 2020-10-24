package dartzee.core.helper

import io.mockk.verify
import java.sql.Timestamp

fun verifyNotCalled(verifyBlock: io.mockk.MockKVerificationScope.() -> Unit)
{
    verify(exactly = 0) { verifyBlock() }
}

fun getPastTime(now: Timestamp): Timestamp
{
    val instant = now.toInstant()
    return Timestamp.from(instant.minusSeconds(2000))
}

fun getFutureTime(now: Timestamp): Timestamp
{
    val instant = now.toInstant()
    return Timestamp.from(instant.plusSeconds(2000))
}