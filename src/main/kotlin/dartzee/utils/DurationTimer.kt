package dartzee.utils

class DurationTimer {
    val start = System.currentTimeMillis()

    fun getDuration() = System.currentTimeMillis() - start
}
