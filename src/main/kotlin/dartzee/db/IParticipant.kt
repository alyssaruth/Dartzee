
import dartzee.achievements.getWinAchievementType
import dartzee.core.util.isEndOfTime
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.TeamEntity
import java.sql.Timestamp

interface IWrappedParticipant
{
    val individuals: List<ParticipantEntity>

    fun getIndividual(roundNumber: Int): ParticipantEntity
    fun getTeam(): IParticipant
    fun getTeamName(): String
}

class SingleParticipant(val pt: ParticipantEntity): IWrappedParticipant
{
    override val individuals = listOf(pt)

    override fun getIndividual(roundNumber: Int) = pt
    override fun getTeam() = pt
    override fun getTeamName() = pt.getPlayerName()
}

class TeamParticipant(val t: TeamEntity, override val individuals: List<ParticipantEntity>): IWrappedParticipant
{
    private val teamSize = individuals.size

    override fun getIndividual(roundNumber: Int) = individuals[(roundNumber - 1) % teamSize]
    override fun getTeam() = t
    override fun getTeamName() = individuals.map { it.getPlayerName() }.sorted().joinToString(" & ")
}

interface IParticipant
{
    var finishingPosition: Int
    var finalScore: Int
    var dtFinished: Timestamp

    fun saveToDatabase()

    fun isActive() = isEndOfTime(dtFinished)

    open fun saveFinishingPosition(game: GameEntity, position: Int)
    {
        this.finishingPosition = position
        this.saveToDatabase()
    }
}