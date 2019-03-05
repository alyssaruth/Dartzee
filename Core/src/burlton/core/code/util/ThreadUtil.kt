package burlton.core.code.util

import javax.swing.SwingUtilities

fun dumpThreadStacks()
{
    Debug.appendWithoutDate("")
    Debug.appendBanner("STACKS DUMP")

    val threads = Thread.getAllStackTraces()
    val it = threads.keys.iterator()
    while (it.hasNext())
    {
        val thread = it.next()
        val stack = thread.stackTrace
        val state = thread.state
        if (stack.isNotEmpty())
        {
            Debug.append("---- THREAD " + thread.name + "  (" + state + ") ----")
            for (element in stack)
            {
                Debug.appendWithoutDate("" + element)
            }
        }
    }

    Debug.appendWithoutDate("")
}

fun runOnEventThread(r: (() -> Unit))
{
    if (SwingUtilities.isEventDispatchThread())
    {
        r.invoke()
    }
    else
    {
        SwingUtilities.invokeLater(r)
    }
}