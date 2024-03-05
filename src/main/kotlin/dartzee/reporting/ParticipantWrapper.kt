package dartzee.reporting

/** Lightweight wrapper object to represent a participant used in reporting */
class ParticipantWrapper(
    var playerName: String,
    val finishingPosition: Int,
    private val resigned: Boolean,
    val teamId: String?
) {
    override fun toString() = "$playerName (${getPositionDesc()})"

    private fun getPositionDesc() =
        if (resigned) "R" else if (finishingPosition == -1) "-" else "$finishingPosition"
}
