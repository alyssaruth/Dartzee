package burlton.dartzee.code.reporting

/**
 * Lightweight wrapper object to represent a participant
 * Used in reporting
 */
class ParticipantWrapper(private val playerName: String, val finishingPosition: Int)
{
    override fun toString() = "$playerName (${getPositionDesc()})"

    private fun getPositionDesc() = if (finishingPosition == -1) "-" else "$finishingPosition"
}