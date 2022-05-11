package dartzee.game.state

import dartzee.core.bean.paint
import dartzee.db.IParticipant
import dartzee.db.ParticipantEntity
import dartzee.db.TeamEntity
import dartzee.game.ParticipantName
import dartzee.utils.ResourceCache
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
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

    val ordinal: Int
        get() = participant.ordinal

    fun getIndividual(roundNumber: Int): ParticipantEntity
    fun getParticipantName() = ParticipantName(individuals.map { it.getPlayerName() }.sorted().joinToString(" & "))
    fun getParticipantNameOrdered() = ParticipantName(individuals.joinToString(" & ") { it.getPlayerName() })
    fun getCombinedAvatar(): ImageIcon
}

class SingleParticipant(override val participant: ParticipantEntity): IWrappedParticipant
{
    override val individuals = listOf(participant)

    override fun getIndividual(roundNumber: Int) = participant
    override fun getCombinedAvatar() = participant.getPlayer().getAvatar() ?: ResourceCache.AVATAR_UNSET
}

class TeamParticipant(override val participant: TeamEntity, override val individuals: List<ParticipantEntity>): IWrappedParticipant
{
    private val teamSize = individuals.size

    override fun getIndividual(roundNumber: Int) = individuals[(roundNumber - 1) % teamSize]
    override fun getCombinedAvatar(): ImageIcon {
        val avatars = individuals.map { it.getPlayer().getAvatar() ?: ResourceCache.AVATAR_UNSET }
        val first = avatars[0].image.toBufferedImage()
        val second = avatars[1].image.toBufferedImage()

        val newImage = BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB)
        newImage.paint { pt ->
            if (pt.x + pt.y == 150) {
                Color.BLACK
            } else if (pt.x < 150 - pt.y) {
                Color(first.getRGB(pt.x, pt.y))
            } else {
                Color(second.getRGB(pt.x, pt.y))
            }
        }

        return ImageIcon(newImage)
    }

    private fun Image.toBufferedImage(): BufferedImage
    {
        val bi = BufferedImage(150, 150, BufferedImage.TYPE_INT_RGB)
        val g = bi.createGraphics()
        g.drawImage(this, 0, 0, null)
        g.dispose()
        return bi
    }
}