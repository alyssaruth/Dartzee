package dartzee.helper

import dartzee.core.obj.HashMapList
import dartzee.core.util.DateStatics
import dartzee.db.DartEntity
import dartzee.db.EntityName
import dartzee.db.LocalIdGenerator
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.ClockType
import dartzee.game.RoundTheClockConfig
import dartzee.game.state.ClockPlayerState
import dartzee.game.state.GolfPlayerState
import dartzee.game.state.SingleParticipant
import dartzee.game.state.X01PlayerState
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.stats.GameWrapper
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.isBust
import io.kotest.matchers.shouldBe
import java.sql.Timestamp

fun factoryClockHit(score: Int, multiplier: Int = 1): Dart
{
    val dart = Dart(score, multiplier)
    dart.startingScore = score
    return dart
}

fun factoryClockMiss(score: Int): Dart
{
    val dart = Dart(score, 0)
    dart.startingScore = score
    return dart
}

fun List<Dart>.insertDarts(pt: ParticipantEntity, roundNumber: Int)
{
    val entities = mapIndexed { ix, drt -> DartEntity.factory(drt, pt.playerId, pt.rowId, roundNumber, ix) }
    entities.forEach { it.saveToDatabase() }
}

fun makeDart(score: Int = 20,
             multiplier: Int = 1,
             segmentType: SegmentType = getSegmentTypeForMultiplier(multiplier),
             startingScore: Int = -1,
             golfHole: Int = -1,
             clockTargets: List<Int> = emptyList()): Dart
{
    val dart = Dart(score, multiplier, segmentType)
    dart.startingScore = startingScore
    dart.roundNumber = golfHole
    dart.clockTargets = clockTargets
    return dart
}

fun setRoundNumbers(rounds: List<List<Dart>>) {
    rounds.forEachIndexed { ix, round ->
        round.forEach { dart ->
            dart.roundNumber = ix + 1
        }
    }
}

fun makeGolfRound(golfHole: Int, darts: List<Dart>): List<Dart>
{
    darts.forEach { it.roundNumber = golfHole }
    return darts
}

fun makeX01RoundsMap(startingScore: Int = 501, vararg darts: List<Dart>): HashMapList<Int, Dart>
{
    val rounds = makeX01Rounds(startingScore, *darts)

    val map = HashMapList<Int, Dart>()
    rounds.forEachIndexed { ix, round ->
        map[ix+1] = round.toMutableList()
    }

    return map
}

fun makeX01Rounds(startingScore: Int = 501, vararg darts: List<Dart>): List<List<Dart>>
{
    var currentScore = startingScore
    darts.forEachIndexed { ix, it ->
        var roundScore = currentScore
        it.forEach { dart ->
            dart.startingScore = roundScore
            dart.roundNumber = ix + 1
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

fun makeClockRounds(inOrder: Boolean, vararg darts: Dart): List<Dart>
{
    val targets: MutableSet<Int> = (1..20).toMutableSet()

    darts.forEach { dart ->
        dart.startingScore = targets.minOf { it }
        dart.clockTargets = targets.toList()

        val hit = if (inOrder) dart.hitClockTarget(ClockType.Standard) else dart.hitAnyClockTarget(ClockType.Standard)
        if (hit) {
            targets.remove(dart.score)
        }
    }

    return darts.toList()
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
    return ClockPlayerState(config, SingleParticipant(participant), completedRounds.toMutableList(), currentRound.toMutableList(), isActive)
}

fun makeX01PlayerState(startingScore: Int = 501,
                       player: PlayerEntity = insertPlayer(),
                       participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                       completedRound: List<Dart> = listOf()): X01PlayerState
{
    return X01PlayerState(startingScore, SingleParticipant(participant), mutableListOf(completedRound))
}

fun makeX01PlayerStateWithRounds(startingScore: Int = 501,
                                 player: PlayerEntity = insertPlayer(),
                                 participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                                 completedRounds: List<List<Dart>> = emptyList(),
                                 isActive: Boolean = false): X01PlayerState
{
    completedRounds.flatten().forEach { it.participantId = participant.rowId }
    return X01PlayerState(startingScore, SingleParticipant(participant), completedRounds.toMutableList(), mutableListOf(), isActive)
}

fun makeGolfPlayerState(player: PlayerEntity = insertPlayer(),
                        participant: ParticipantEntity = insertParticipant(playerId = player.rowId),
                        completedRounds: List<List<Dart>> = emptyList(),
                        currentRound: List<Dart> = emptyList()): GolfPlayerState
{
    return GolfPlayerState(SingleParticipant(participant), completedRounds.toMutableList(), currentRound.toMutableList())
}

fun makeGameWrapper(
    localId: Long = LocalIdGenerator(mainDatabase).generateLocalId(EntityName.Game),
    gameParams: String = "501",
    dtStart: Timestamp = Timestamp(1000),
    dtFinish: Timestamp = DateStatics.END_OF_TIME,
    finalScore: Int = -1,
    dartRounds: HashMapList<Int, Dart> = HashMapList(),
    totalRounds: Int = dartRounds.size,
    teamGame: Boolean = false
): GameWrapper
{
    return GameWrapper(localId, gameParams, dtStart, dtFinish, finalScore, teamGame).also {
        it.setHmRoundNumberToDartsThrown(dartRounds)
        it.setTotalRounds(totalRounds)
    }
}

fun makeGolfGameWrapper(
    localId: Long = 1L,
    gameParams: String = "18",
    dartRounds: List<List<Dart>>,
    expectedScore: Int,
    dtStart: Timestamp = Timestamp(1000)): GameWrapper
{
    val golfRounds = dartRounds.mapIndexed { ix, round -> makeGolfRound(ix+1, round) }

    val score = golfRounds.sumOf { it.last().getGolfScore() }
    score shouldBe expectedScore

    val wrapper = makeGameWrapper(localId = localId, gameParams = gameParams, finalScore = score, dtStart = dtStart)
    golfRounds.flatten().forEach(wrapper::addDart)
    return wrapper
}

fun makeClockGameWrapper(
    localId: Long = 1L,
    config: RoundTheClockConfig = RoundTheClockConfig(ClockType.Standard, true),
    dartRounds: List<Dart> = emptyList(),
    finalScore: Int = -1,
    dtStart: Timestamp = Timestamp(1000)): GameWrapper
{
    val rounds = makeClockRounds(config.inOrder, *dartRounds.toTypedArray())

    val wrapper = makeGameWrapper(localId = localId, gameParams = config.toJson(), finalScore = finalScore, dtStart = dtStart)
    rounds.forEach(wrapper::addDart)
    return wrapper
}