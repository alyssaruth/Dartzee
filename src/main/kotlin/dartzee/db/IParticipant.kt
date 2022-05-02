package dartzee.db

import dartzee.core.util.isEndOfTime
import java.sql.Timestamp

interface IParticipant
{
    var finishingPosition: Int
    var finalScore: Int
    var dtFinished: Timestamp

    fun saveToDatabase()

    fun isActive() = isEndOfTime(dtFinished)

    fun saveFinishingPosition(game: GameEntity, position: Int)
    {
        this.finishingPosition = position
        this.saveToDatabase()
    }
}