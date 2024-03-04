package dartzee.game.state

import dartzee.db.IParticipant
import dartzee.db.ParticipantEntity
import dartzee.db.TeamEntity
import dartzee.game.UniqueParticipantName
import dartzee.utils.greyscale
import dartzee.utils.splitAvatar
import javax.swing.ImageIcon

/**
 * Wraps up either a Team or an individual Participant, granting access to either:
 * - The individual player of a round, e.g. to save their darts, unlock achievements and so on
 * - The top-level "participant" (a player or team), for checking/setting finishing position, score
 *   etc
 */
sealed interface IWrappedParticipant {
    val individuals: List<ParticipantEntity>
    val participant: IParticipant

    fun ordinal() = participant.ordinal

    fun getIndividual(roundNumber: Int): ParticipantEntity

    fun getUniqueParticipantName() =
        UniqueParticipantName(individuals.map { it.getPlayerName() }.sorted().joinToString(" & "))

    fun getParticipantName() = individuals.joinToString(" & ") { it.getPlayerName() }

    fun getParticipantNameHtml(
        active: Boolean,
        currentParticipant: ParticipantEntity? = null
    ): String {
        val contents =
            individuals.joinToString(" &#38; ") { pt ->
                if (active && pt == currentParticipant) {
                    "<b>${pt.getPlayerName()}</b>"
                } else {
                    pt.getPlayerName()
                }
            }

        if (active && currentParticipant == null) {
            return "<html><b>$contents</b></html>"
        }

        return "<html>$contents</html>"
    }

    fun getAvatar(roundNumber: Int, selected: Boolean, gameFinished: Boolean): ImageIcon
}

class SingleParticipant(override val participant: ParticipantEntity) : IWrappedParticipant {
    override val individuals = listOf(participant)

    override fun getIndividual(roundNumber: Int) = participant

    override fun getAvatar(roundNumber: Int, selected: Boolean, gameFinished: Boolean): ImageIcon {
        val avatar = participant.getPlayer().getAvatar()
        if (gameFinished || selected) {
            return avatar
        }

        return avatar.greyscale()
    }
}

class TeamParticipant(
    override val participant: TeamEntity,
    override val individuals: List<ParticipantEntity>
) : IWrappedParticipant {
    private val teamSize = individuals.size

    override fun getIndividual(roundNumber: Int) = individuals[(roundNumber - 1) % teamSize]

    override fun getAvatar(roundNumber: Int, selected: Boolean, gameFinished: Boolean): ImageIcon {
        val selectedPlayer = if (selected) getIndividual(roundNumber).getPlayer() else null
        return splitAvatar(
            individuals[0].getPlayer(),
            individuals[1].getPlayer(),
            selectedPlayer,
            gameFinished
        )
    }
}
