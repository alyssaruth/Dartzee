package dartzee.helper

import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.core.obj.HashMapList
import dartzee.core.util.DateStatics
import dartzee.db.EntityName
import dartzee.db.LocalIdGenerator
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.ClockType
import dartzee.game.RoundTheClockConfig
import dartzee.game.state.ClockPlayerState
import dartzee.game.state.GolfPlayerState
import dartzee.game.state.X01PlayerState
import dartzee.stats.GameWrapper
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.isBust
import java.awt.Point
import java.sql.Timestamp

fun factoryClockHit(score: Int, multiplier: Int = 1): Dart
{
    val dart = Dart(score, multiplier)
    dart.startingScore = score
    return dart
}

fun makeDart(score: Int = 20,
             multiplier: Int = 1,
             segmentType: SegmentType = getSegmentTypeForMultiplier(multiplier),
             pt: Point = Point(0, 0),
             startingScore: Int = -1,
             golfHole: Int = -1,
             clockTargets: List<Int> = emptyList()): Dart
{
    val dart = Dart(score, multiplier, pt, segmentType)
    dart.startingScore = startingScore
    dart.roundNumber = golfHole
    dart.clockTargets = clockTargets
    return dart
}

fun makeGolfRound(golfHole: Int, darts: List<Dart>): List<Dart>
{
    darts.forEach { it.roundNumber = golfHole }
    return darts
}

fun makeX01Rounds(startingScore: Int = 501, vararg darts: List<Dart>): List<List<Dart>>
{
    var currentScore = startingScore
    darts.forEach {
        var roundScore = currentScore
        it.forEach { dart ->
            dart.startingScore = roundScore
            roundScore -= dart.getTotal()
        }

        val lastDartForRound = it.last()
        if (!isBust(lastDartForRound))
        {
            currentScore = roundScore
        }
    }

    return darts.toList()
}
fun makeX01Rounds(startingScore: Int = 501, vararg darts: Dart): List<List<Dart>>
{
    var currentTotal = startingScore
    darts.forEach {
        it.startingScore = currentTotal
        currentTotal -= it.getTotal()
    }

    return darts.toList().chunked(3)
}

fun makeClockPlayerState(clockType: ClockType = ClockType.Standard,
                         inOrder: Boolean = true,
                         isActive: Boolean = false,
                         player: PlayerEntity = insertPlayer(),
                         participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                         completedRounds: List<List<Dart>> = emptyList(),
                         currentRound: List<Dart> = emptyList()): ClockPlayerState
{
    val config = RoundTheClockConfig(clockType, inOrder)
    completedRounds.flatten().forEach { it.participantId = participant.rowId }
    return ClockPlayerState(config, participant, completedRounds.toMutableList(), currentRound.toMutableList(), isActive)
}

fun makeX01PlayerState(startingScore: Int = 501,
                       player: PlayerEntity = insertPlayer(),
                       participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                       completedRound: List<Dart> = listOf()): X01PlayerState
{
    return X01PlayerState(startingScore, participant, mutableListOf(completedRound))
}

fun makeX01PlayerStateWithRounds(startingScore: Int = 501,
                                 player: PlayerEntity = insertPlayer(),
                                 participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                                 completedRounds: List<List<Dart>> = emptyList(),
                                 isActive: Boolean = false): X01PlayerState
{
    completedRounds.flatten().forEach { it.participantId = participant.rowId }
    return X01PlayerState(startingScore, participant, completedRounds.toMutableList(), mutableListOf(), isActive)
}

fun makeGolfPlayerState(player: PlayerEntity = insertPlayer(),
                        participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                        completedRounds: List<List<Dart>> = emptyList(),
                        currentRound: List<Dart> = emptyList()): GolfPlayerState
{
    return GolfPlayerState(participant, completedRounds.toMutableList(), currentRound.toMutableList())
}

fun makeGameWrapper(
    localId: Long = LocalIdGenerator(mainDatabase).generateLocalId(EntityName.Game),
    gameParams: String = "501",
    dtStart: Timestamp = Timestamp(1000),
    dtFinish: Timestamp = DateStatics.END_OF_TIME,
    finalScore: Int = -1,
    dartRounds: HashMapList<Int, Dart> = HashMapList(),
    totalRounds: Int = dartRounds.size
): GameWrapper
{
    return GameWrapper(localId, gameParams, dtStart, dtFinish, finalScore).also {
        it.setHmRoundNumberToDartsThrown(dartRounds)
        it.setTotalRounds(totalRounds)
    }
}