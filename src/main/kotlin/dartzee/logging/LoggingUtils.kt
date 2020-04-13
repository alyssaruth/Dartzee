package dartzee.logging

import java.io.PrintWriter
import java.io.StringWriter

fun extractStackTrace(t: Throwable): String
{
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    t.printStackTrace(pw)
    return sw.toString()
}