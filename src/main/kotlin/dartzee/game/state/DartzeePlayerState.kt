package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.achievements.AchievementType
import dartzee.dartzee.DartzeeRoundResult
import dartzee.db.AchievementEntity
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

        if (!result.success)
        {
            AchievementEntity.updateAchievement(AchievementType.DARTZEE_HALVED, pt.playerId, pt.gameId, -result.score)
        }

        addRoundResult(entity)
    }

    fun addRoundResult(result: DartzeeRoundResultEntity)
    {
        roundResults.add(result)
        fireStateChanged()
    }

    fun getPeakScore() = (1 until currentRoundNumber()).map(::getCumulativeScore).maxOrNull()
    fun getCumulativeScore(roundNumber: Int): Int
    {
        val roundResultTotal = roundResults.filter { it.roundNumber <= roundNumber }.sumOf { it.score }
        return roundResultTotal + sumScore(completedRounds.firstOrNull() ?: emptyList())
    }

    override fun getScoreSoFar() = getCumulativeScore(currentRoundNumber())
}