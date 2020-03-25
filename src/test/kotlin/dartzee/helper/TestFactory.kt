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

fun makeDart(score: Int = 20, multiplier: Int = 1, segmentType: Int = SEGMENT_TYPE_OUTER_SINGLE): Dart
{
    val dart = Dart(score, multiplier)
    dart.segmentType = segmentType
    dart.pt = Point(0, 0)
    return dart
}

fun makeDefaultPlayerState(player: PlayerEntity = insertPlayer(),
                           participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                           dartsThrown: List<Dart> = emptyList(),
                           lastRoundNumber: Int = dartsThrown.size): DefaultPlayerState<*>
{
    return DefaultPlayerState(participant, mockk<DartsScorer>(relaxed = true), lastRoundNumber, mutableListOf(dartsThrown))
}