package burlton.dartzee.code.`object`

import burlton.core.code.util.Debug
import burlton.dartzee.code.screen.DartsApp
import java.io.File

object CheckoutSuggester
{
    private val hmScoreToCheckout = readInCheckouts()

    fun suggestCheckout(score: Int): List<Dart>?
    {
        return hmScoreToCheckout[score]
    }

    private fun readInCheckouts(): MutableMap<Int, List<Dart>>
    {
        return try
        {
            val uri = (DartsApp::class.java).getResource("/Checkouts").toURI()
            val checkouts = mutableListOf<String>()
            File(uri).useLines { lines -> lines.forEach { checkouts.add(it) } }

            parseCheckouts(checkouts)
        }
        catch (t: Throwable)
        {
            Debug.stackTrace(t, "Failed to read in checkouts - won't make suggestions")
            mutableMapOf()
        }
    }

    fun parseCheckouts(checkouts: List<String>): MutableMap<Int, List<Dart>>
    {
        val map = mutableMapOf<Int, List<Dart>>()

        checkouts.forEach {
            val split = it.split("=")

            val score = split[0].toInt()
            val dartStrs = split[1].split(",")
            val darts = dartStrs.map{d -> factoryFromString(d)!!}.toList()

            map[score] = darts
        }

        return map
    }
}