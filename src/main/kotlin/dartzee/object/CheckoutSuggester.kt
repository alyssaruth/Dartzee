package dartzee.`object`

import dartzee.logging.CODE_FILE_ERROR
import dartzee.utils.InjectedThings.logger

object CheckoutSuggester {
    private val hmScoreToCheckout = readInCheckouts()

    fun suggestCheckout(score: Int, dartsRemaining: Int): List<DartHint>? {
        for (dart in 1..dartsRemaining) {
            val hmKey = "$score-$dart"
            val checkout = hmScoreToCheckout[hmKey]
            if (checkout != null) {
                return checkout
            }
        }

        return null
    }

    private fun readInCheckouts(): MutableMap<String, List<DartHint>> {
        return try {
            val inputStream = javaClass.getResourceAsStream("/Checkouts")
            val checkouts = mutableListOf<String>()

            inputStream.bufferedReader().useLines { lines -> lines.forEach { checkouts.add(it) } }

            parseCheckouts(checkouts)
        } catch (t: Throwable) {
            logger.error(CODE_FILE_ERROR, "Failed to read in checkouts - won't make suggestions", t)
            mutableMapOf()
        }
    }

    fun parseCheckouts(checkouts: List<String>): MutableMap<String, List<DartHint>> {
        val map = mutableMapOf<String, List<DartHint>>()

        checkouts.forEach { checkout ->
            val split = checkout.split("=")

            val score = split[0].toInt()
            val dartStrs = split[1].split(",")
            val darts = dartStrs.map { d -> factoryDartHintFromString(d) }.toList()

            addCheckoutsToMap(score, darts, map)
        }

        return map
    }

    private fun addCheckoutsToMap(
        score: Int,
        darts: List<DartHint>,
        map: MutableMap<String, List<DartHint>>
    ) {
        val currentCheckout = darts.toMutableList()
        var currentScore = score
        while (currentCheckout.isNotEmpty()) {
            val key = "$currentScore-${currentCheckout.size}"
            map[key] = currentCheckout.toList()

            val dart = currentCheckout.removeAt(0)
            currentScore -= dart.getTotal()
        }
    }
}
