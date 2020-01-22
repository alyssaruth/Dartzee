package dartzee.core.helper

import io.mockk.verify

fun verifyNotCalled(verifyBlock: io.mockk.MockKVerificationScope.() -> Unit)
{
    verify(exactly = 0) { verifyBlock() }
}