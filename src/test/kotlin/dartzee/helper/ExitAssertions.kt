package dartzee.helper

import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import java.security.Permission

private class ExitException(val status: Int): SecurityException("Nope")
private class NoExitSecurityManager(val originalSecurityManager: SecurityManager?): SecurityManager()
{
    override fun checkPermission(perm: Permission?)
    {
        originalSecurityManager?.checkPermission(perm)
    }

    override fun checkPermission(perm: Permission?, context: Any?)
    {
        originalSecurityManager?.checkPermission(perm, context)
    }

    override fun checkExit(status: Int)
    {
        super.checkExit(status)
        throw ExitException(status)
    }
}


fun assertExits(expectedStatus: Int, fn: () -> Unit)
{
    val originalSecurityManager = System.getSecurityManager()
    System.setSecurityManager(NoExitSecurityManager(originalSecurityManager))

    try
    {
        fn()
        fail("Expected exitProcess($expectedStatus), but it wasn't called")
    }
    catch (e: ExitException)
    {
        e.status shouldBe expectedStatus
    }
    finally
    {
        System.setSecurityManager(originalSecurityManager)
    }
}

fun assertDoesNotExit(fn: () -> Unit)
{
    val originalSecurityManager = System.getSecurityManager()
    System.setSecurityManager(NoExitSecurityManager(originalSecurityManager))

    try
    {
        fn()
    }
    catch (e: ExitException)
    {
        fail("Called exitProcess(${e.status})")
    }
    finally
    {
        System.setSecurityManager(originalSecurityManager)
    }
}