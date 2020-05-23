package dartzee.helper

import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.state.DefaultPlayerState
import dartzee.screen.game.scorer.DartsScorer
import io.mockk.mockk
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
    dart.golfHole = golfHole
    return dart
}

fun makeGolfRound(golfHole: Int, darts: List<Dart>): List<Dart>
{
    darts.forEach { it.golfHole = golfHole }
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

inline fun <reified S: DartsScorer> makeDefaultPlayerState(player: PlayerEntity = insertPlayer(),
                           participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                           dartsThrown: List<Dart> = listOf(makeDart()),
                           lastRoundNumber: Int = dartsThrown.size): DefaultPlayerState<S>
{
    return DefaultPlayerState(participant, mockk(relaxed = true), lastRoundNumber, mutableListOf(dartsThrown))
}

inline fun <reified S: DartsScorer> makeDefaultPlayerStateWithRounds(player: PlayerEntity = insertPlayer(),
                                                           participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                                                           dartsThrown: List<List<Dart>> = emptyList(),
                                                           lastRoundNumber: Int = dartsThrown.size): DefaultPlayerState<S>
{
    dartsThrown.flatten().forEach { it.participantId = participant.rowId }
    return DefaultPlayerState(participant, mockk(relaxed = true), lastRoundNumber, dartsThrown.toMutableList())
}