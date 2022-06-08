package dartzee.game.state

import dartzee.db.IParticipant
import dartzee.db.ParticipantEntity
import dartzee.db.TeamEntity
import dartzee.game.ParticipantName
import dartzee.game.UniqueParticipantName
import dartzee.utils.splitAvatar
import javax.swing.ImageIcon

/**
 * Wraps up either a Team or an individual Participant, granting access to either:
 *
 *  - The individual player of a round, e.g. to save their darts, unlock achievements and so on
 *  - The top-level "participant" (a player or team), for checking/setting finishing position, score etc
 */
sealed interface IWrappedParticipant
{
    val individuals: List<ParticipantEntity>
    val participant: IParticipant

    fun getIndividual(roundNumber: Int): ParticipantEntity
    fun getUniqueParticipantName() = UniqueParticipantName(individuals.map { it.getPlayerName() }.sorted().joinToString(" & "))
    fun getParticipantName() = ParticipantName(individuals.joinToString(" & ") { it.getPlayerName() })
    fun getAvatar(roundNumber: Int, selected: Boolean): ImageIcon
}

class SingleParticipant(override val participant: ParticipantEntity): IWrappedParticipant
{
    override val individuals = listOf(participant)

    override fun getIndividual(roundNumber: Int) = participant

    override fun getAvatar(roundNumber: Int, selected: Boolean) = participant.getPlayer().getAvatar()
}

class TeamParticipant(override val participant: TeamEntity, override val individuals: List<ParticipantEntity>): IWrappedParticipant
{
    private val teamSize = individuals.size

    override fun getIndividual(roundNumber: Int) = individuals[(roundNumber - 1) % teamSize]

    override fun getAvatar(roundNumber: Int, selected: Boolean): ImageIcon
    {
        val selectedPlayer = if (selected) getIndividual(roundNumber).getPlayer() else null
        return splitAvatar(individuals[0].getPlayer(), individuals[1].getPlayer(), selectedPlayer)
    }
}