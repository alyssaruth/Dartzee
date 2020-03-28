package dartzee.helper

import dartzee.`object`.Dart
import dartzee.`object`.SEGMENT_TYPE_OUTER_SINGLE
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
             segmentType: Int = SEGMENT_TYPE_OUTER_SINGLE,
             pt: Point = Point(0, 0),
             startingScore: Int = -1): Dart
{
    val dart = Dart(score, multiplier)
    dart.segmentType = segmentType
    dart.pt = pt
    dart.startingScore = startingScore
    return dart
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
    return DefaultPlayerState(participant, mockk(relaxed = true), lastRoundNumber, dartsThrown.toMutableList())
}