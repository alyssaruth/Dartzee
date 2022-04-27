
import dartzee.core.util.getSqlDateNow
import dartzee.db.ParticipantEntity
import dartzee.db.TeamEntity
import java.sql.Timestamp

interface IParticipant
{
    fun getParticipant(roundNumber: Int): ParticipantEntity
    fun getTeam(): ICompetitor
}

class SingleParticipant(val pt: ParticipantEntity): IParticipant
{
    override fun getParticipant(roundNumber: Int) = pt
    override fun getTeam() = pt
}

class TeamParticipant(val t: TeamEntity, val participants: List<ParticipantEntity>): IParticipant
{
    private val teamSize = participants.size

    override fun getParticipant(roundNumber: Int) = participants[(roundNumber - 1) % teamSize]
    override fun getTeam() = t
}

interface ICompetitor
{
    var finishingPosition: Int
    var finalScore: Int
    var dtFinished: Timestamp

    fun saveToDatabase(dtLastUpdate: Timestamp = getSqlDateNow())
}