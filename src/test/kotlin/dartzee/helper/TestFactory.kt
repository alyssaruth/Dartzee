package dartzee.helper

import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.state.ClockPlayerState
import dartzee.game.state.GolfPlayerState
import dartzee.game.state.X01PlayerState
import java.awt.Point

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
             golfHole: Int = -1): Dart
{
    val dart = Dart(score, multiplier, pt, segmentType)
    dart.startingScore = startingScore
    dart.roundNumber = golfHole
    return dart
}

fun makeGolfRound(golfHole: Int, darts: List<Dart>): List<Dart>
{
    darts.forEach { it.roundNumber = golfHole }
    return darts
}

fun makeX01Rounds(startingScore: Int = 501, vararg darts: List<Dart>): List<List<Dart>>
{
    val allDarts = darts.toList().flatten()
    return makeX01Rounds(startingScore, *allDarts.toTypedArray())
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

fun makeClockPlayerStateWithRounds(player: PlayerEntity = insertPlayer(),
                                   participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                                   dartsThrown: List<List<Dart>> = emptyList(),
                                   lastRoundNumber: Int = dartsThrown.size): ClockPlayerState
{
    dartsThrown.flatten().forEach { it.participantId = participant.rowId }
    return ClockPlayerState(participant, lastRoundNumber, dartsThrown.toMutableList())
}

fun makeX01PlayerState(player: PlayerEntity = insertPlayer(),
                           participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                           dartsThrown: List<Dart> = listOf(makeDart()),
                           lastRoundNumber: Int = dartsThrown.size): X01PlayerState
{
    return X01PlayerState(participant, lastRoundNumber, mutableListOf(dartsThrown))
}

fun makeX01PlayerStateWithRounds(player: PlayerEntity = insertPlayer(),
                                   participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                                   dartsThrown: List<List<Dart>> = emptyList(),
                                   lastRoundNumber: Int = dartsThrown.size): X01PlayerState
{
    dartsThrown.flatten().forEach { it.participantId = participant.rowId }
    return X01PlayerState(participant, lastRoundNumber, dartsThrown.toMutableList())
}

fun makeGolfPlayerState(player: PlayerEntity = insertPlayer(),
                       participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                       dartsThrown: List<Dart> = emptyList(),
                       lastRoundNumber: Int = dartsThrown.size): GolfPlayerState
{
    return GolfPlayerState(participant, lastRoundNumber, mutableListOf(dartsThrown))
}

fun makeGolfPlayerStateWithRounds(player: PlayerEntity = insertPlayer(),
                                 participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                                 dartsThrown: List<List<Dart>> = emptyList(),
                                 lastRoundNumber: Int = dartsThrown.size): GolfPlayerState
{
    dartsThrown.flatten().forEach { it.participantId = participant.rowId }
    return GolfPlayerState(participant, lastRoundNumber, dartsThrown.toMutableList())
}