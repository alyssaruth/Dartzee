package dartzee.logging

import java.io.PrintWriter
import java.io.StringWriter
import java.sql.SQLException

fun extractStackTrace(t: Throwable): String
{
    if (t is SQLException)
    {
        return extractSqlException(t)
    }

    return getStackString(t)
}

private fun getStackString(t: Throwable): String
{
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    t.printStackTrace(pw)
    return sw.toString()
}

fun extractThreadStack(stack: Array<StackTraceElement>): String
{
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    stack.forEach {
        pw.println("\tat $it")
    }

    return sw.toString()
}

private fun extractSqlException(sqle: SQLException): String
{
    val sb = StringBuilder()
    sb.append(getStackString(sqle))

    var childSqle: SQLException? = sqle.nextException
    while (childSqle != null)
    {
        sb.append("Child: ${getStackString(childSqle)}")
        childSqle = childSqle.nextException
    }

    return sb.toString()
}