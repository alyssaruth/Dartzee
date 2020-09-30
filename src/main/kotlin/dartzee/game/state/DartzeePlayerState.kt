package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.dartzee.DartzeeRoundResult
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.ParticipantEntity
import dartzee.utils.sumScore

data class DartzeePlayerState(override val pt: ParticipantEntity,
                              override val completedRounds: MutableList<List<Dart>> = mutableListOf(),
                              override val currentRound: MutableList<Dart> = mutableListOf(),
                              override var isActive: Boolean = false,
                              val roundResults: MutableList<DartzeeRoundResultEntity> = mutableListOf()): AbstractPlayerState<DartzeePlayerState>()
{
    fun saveRoundResult(result: DartzeeRoundResult)
    {
        val entity = DartzeeRoundResultEntity.factoryAndSave(result, pt, currentRoundNumber())
        addRoundResult(entity)
    }

    fun addRoundResult(result: DartzeeRoundResultEntity)
    {
        roundResults.add(result)
        fireStateChanged()
    }

    fun getPeakScore() = (1 until currentRoundNumber()).map(::getCumulativeScore).max()
    fun getCumulativeScore(roundNumber: Int): Int
    {
        val roundResultTotal = roundResults.filter { it.roundNumber <= roundNumber }.sumBy { it.score }
        return roundResultTotal + sumScore(completedRounds.firstOrNull() ?: emptyList())
    }

    override fun getScoreSoFar() = getCumulativeScore(currentRoundNumber())
}