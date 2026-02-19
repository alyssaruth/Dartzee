package dartzee.helper

import dartzee.core.helper.verifyNotCalled
import dartzee.main.IExiter
import dartzee.utils.InjectedThings
import io.mockk.mockk
import io.mockk.verify

fun assertExits(expectedStatus: Int, fn: () -> Unit) {
    val exiter = mockk<IExiter>(relaxed = true)
    InjectedThings.exiter = exiter

    fn()

    verify { exiter.exit(expectedStatus) }
}

fun assertDoesNotExit(fn: () -> Unit) {
    val exiter = mockk<IExiter>(relaxed = true)
    InjectedThings.exiter = exiter

    fn()

    verifyNotCalled { exiter.exit(any()) }
}
