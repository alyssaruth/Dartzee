package dartzee.core.util

import dartzee.logging.CODE_THREAD_STACK
import dartzee.logging.CODE_THREAD_STACKS
import dartzee.logging.KEY_STACK
import dartzee.logging.extractThreadStack
import dartzee.utils.InjectedThings.logger
import javax.swing.SwingUtilities

fun dumpThreadStacks() {
    logger.info(CODE_THREAD_STACKS, "Dumping thread stacks")

    val threads = Thread.getAllStackTraces()
    val it = threads.keys.iterator()
    while (it.hasNext()) {
        val thread = it.next()
        val stack = thread.stackTrace
        val state = thread.state
        if (stack.isNotEmpty()) {
            logger.info(
                CODE_THREAD_STACK,
                "${thread.name} ($state)",
                KEY_STACK to extractThreadStack(stack)
            )
        }
    }
}

fun runOnEventThread(r: (() -> Unit)) {
    if (SwingUtilities.isEventDispatchThread()) {
        r.invoke()
    } else {
        SwingUtilities.invokeLater(r)
    }
}

fun runOnEventThreadBlocking(r: (() -> Unit)) {
    if (SwingUtilities.isEventDispatchThread()) {
        r.invoke()
    } else {
        SwingUtilities.invokeAndWait(r)
    }
}

fun runInOtherThread(r: (() -> Unit)): Thread {
    val t = Thread(r)
    t.start()
    return t
}
