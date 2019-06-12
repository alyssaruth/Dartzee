package burlton.dartzee.test.helper

import io.mockk.verify

fun verifyNotCalled(verifyBlock: io.mockk.MockKVerificationScope.() -> kotlin.Unit)
{
    verify(exactly = 0) { verifyBlock() }
}