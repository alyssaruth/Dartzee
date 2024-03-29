package dartzee.game.state

import dartzee.core.helper.verifyNotCalled
import dartzee.core.util.DateStatics
import dartzee.db.DartEntity
import dartzee.db.ParticipantEntity
import dartzee.drtDoubleEight
import dartzee.drtInnerSeven
import dartzee.drtOuterFive
import dartzee.drtOuterNineteen
import dartzee.drtOuterOne
import dartzee.drtOuterTwenty
import dartzee.drtTrebleOne
import dartzee.helper.AbstractTest
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.insertTeam
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TestAbstractPlayerState : AbstractTest() {
    @Test
    fun `it should take a copy of the darts that are added`() {
        val state = TestPlayerState(insertParticipant())
        val darts = mutableListOf(Dart(20, 1))

        state.addCompletedRound(darts)

        darts.clear()
        state.completedRounds.first().shouldContainExactly(Dart(20, 1))
    }

    @Test
    fun `should populate darts with the ParticipantId and RoundNumber`() {
        val pt = insertParticipant()
        val state = TestPlayerState(pt)

        val dart = Dart(20, 1)
        dart.participantId shouldBe ""
        dart.roundNumber shouldBe -1

        state.dartThrown(dart)

        dart.participantId shouldBe pt.rowId
        dart.roundNumber shouldBe 1
    }

    @Test
    fun `It should populate completed rounds with the ParticipantId`() {
        val pt = insertParticipant()
        val state = TestPlayerState(pt)

        val dart = Dart(20, 1)
        dart.participantId shouldBe ""

        state.addCompletedRound(mutableListOf(dart))

        dart.participantId shouldBe pt.rowId
    }

    @Test
    fun `Should support resetting the currently thrown darts`() {
        val state = TestPlayerState(insertParticipant())

        state.dartThrown(Dart(20, 1))
        state.currentRound.shouldContainExactly(Dart(20, 1))

        state.resetRound()
        state.currentRound.shouldBeEmpty()
    }

    @Test
    fun `Should support committing a round of darts and saving them to the database`() {
        val pt = insertParticipant()
        val dartOne = Dart(20, 1, SegmentType.OUTER_SINGLE)
        val dartTwo = Dart(5, 1, SegmentType.OUTER_SINGLE)
        val dartThree = Dart(1, 1, SegmentType.OUTER_SINGLE)

        val state = TestPlayerState(pt)
        state.dartThrown(dartOne)
        state.dartThrown(dartTwo)
        state.dartThrown(dartThree)

        state.commitRound()
        state.currentRound.shouldBeEmpty()
        state.completedRounds.shouldContainExactly(
            listOf(listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)))
        )

        val entities = DartEntity().retrieveEntities()
        entities.forEach {
            it.playerId shouldBe pt.playerId
            it.participantId shouldBe pt.rowId
            it.roundNumber shouldBe 1
        }

        val entityOne = entities.find { it.ordinal == 1 }!!
        validateDartEntity(entityOne, dartOne)

        val entityTwo = entities.find { it.ordinal == 2 }!!
        validateDartEntity(entityTwo, dartTwo)

        val entityThree = entities.find { it.ordinal == 3 }!!
        validateDartEntity(entityThree, dartThree)
    }

    private fun validateDartEntity(dartEntity: DartEntity, originalDart: Dart) {
        dartEntity.multiplier shouldBe originalDart.multiplier
        dartEntity.score shouldBe originalDart.score
        dartEntity.segmentType shouldBe originalDart.segmentType
        dartEntity.roundNumber shouldBe 1
    }

    @Test
    fun `Should correctly compute the current round number`() {
        val state = TestPlayerState(insertParticipant())
        state.currentRoundNumber() shouldBe 1

        state.dartThrown(Dart(1, 1))
        state.currentRoundNumber() shouldBe 1

        state.commitRound()
        state.currentRoundNumber() shouldBe 2

        state.dartThrown(Dart(20, 1))
        state.currentRoundNumber() shouldBe 2

        state.resetRound()
        state.currentRoundNumber() shouldBe 2
    }

    @Test
    fun `Should mark a participant as finished`() {
        val participant =
            insertParticipant(
                dtFinished = DateStatics.END_OF_TIME,
                finalScore = -1,
                finishingPosition = -1
            )
        val state = TestPlayerState(participant)

        state.participantFinished(1, 100)
        participant.finalScore shouldBe 100
        participant.finishingPosition shouldBe 1
        participant.dtFinished shouldNotBe DateStatics.END_OF_TIME

        val reretrieved = ParticipantEntity().retrieveForId(participant.rowId)!!
        reretrieved.finalScore shouldBe 100
        reretrieved.finishingPosition shouldBe 1
        reretrieved.dtFinished shouldNotBe DateStatics.END_OF_TIME
    }

    @Test
    fun `Should mark a participant as resigned`() {
        val participant =
            insertParticipant(
                dtFinished = DateStatics.END_OF_TIME,
                finalScore = -1,
                finishingPosition = -1
            )
        val state = TestPlayerState(participant)
        state.hasResigned() shouldBe false

        state.participantResigned(4)
        state.hasResigned() shouldBe true
        participant.finalScore shouldBe -1
        participant.finishingPosition shouldBe 4
        participant.dtFinished shouldNotBe DateStatics.END_OF_TIME
        participant.resigned shouldBe true

        val reretrieved = ParticipantEntity().retrieveForId(participant.rowId)!!
        reretrieved.finalScore shouldBe -1
        reretrieved.finishingPosition shouldBe 4
        reretrieved.dtFinished shouldNotBe DateStatics.END_OF_TIME
        reretrieved.resigned shouldBe true
    }

    @Test
    fun `Should update the finishing position`() {
        val participant =
            insertParticipant(
                dtFinished = DateStatics.END_OF_TIME,
                finalScore = -1,
                finishingPosition = -1
            )
        val state = TestPlayerState(participant)

        state.setParticipantFinishPosition(4)
        participant.finalScore shouldBe -1
        participant.finishingPosition shouldBe 4
        participant.dtFinished shouldBe DateStatics.END_OF_TIME

        val reretrieved = ParticipantEntity().retrieveForId(participant.rowId)!!
        reretrieved.finalScore shouldBe -1
        reretrieved.finishingPosition shouldBe 4
        reretrieved.dtFinished shouldBe DateStatics.END_OF_TIME
    }

    @Test
    fun `Should fire state changed`() {
        val state = TestPlayerState(insertParticipant())

        state.shouldFireStateChange { it.dartThrown(Dart(1, 1)) }
        state.shouldFireStateChange { it.resetRound() }
        state.shouldFireStateChange { it.commitRound() }
        state.shouldFireStateChange { it.addCompletedRound(listOf(Dart(1, 1))) }
        state.shouldFireStateChange { it.participantFinished(3, 10) }
        state.shouldFireStateChange { it.setParticipantFinishPosition(4) }
        state.shouldFireStateChange { it.updateActive(true) }
        state.shouldFireStateChange { it.updateActive(false) }
        state.shouldNotFireStateChange { it.updateActive(false) }
    }

    @Test
    fun `Should fire state changed at the point a listener is added`() {
        val state = TestPlayerState(insertParticipant())
        val listener = mockk<PlayerStateListener<TestPlayerState>>(relaxed = true)
        state.addListener(listener)

        verify { listener.stateChanged(state) }
    }

    @Test
    fun `Should identify human vs ai`() {
        val ai = insertPlayer(strategy = "foo")
        val human = insertPlayer(strategy = "")

        val aiPt = insertParticipant(playerId = ai.rowId)
        val humanPt = insertParticipant(playerId = human.rowId)

        TestPlayerState(aiPt).isHuman() shouldBe false
        TestPlayerState(humanPt).isHuman() shouldBe true
    }

    @Test
    fun `Should correctly identify the current individual within a team`() {
        val pt1 = insertParticipant()
        val pt2 = insertParticipant()

        val state = TestTeamState(listOf(pt1, pt2))
        state.currentIndividual() shouldBe pt1

        state.dartThrown(drtDoubleEight())
        state.commitRound()

        state.currentIndividual() shouldBe pt2
    }

    @Test
    fun `Should correctly identify whether or not there are multiple participants`() {
        TestPlayerState(insertParticipant()).hasMultiplePlayers() shouldBe false

        val pt1 = insertParticipant()
        val pt2 = insertParticipant()
        TestTeamState(listOf(pt1, pt2)).hasMultiplePlayers() shouldBe true
    }

    @Test
    fun `Should correctly extract the rounds for a player, including the current one`() {
        val pt1 = insertParticipant()
        val pt2 = insertParticipant()
        val state = TestTeamState(listOf(pt1, pt2))

        val roundOne = listOf(drtOuterTwenty(), drtOuterFive(), drtOuterOne())
        roundOne.forEach(state::dartThrown)
        state.commitRound()

        val roundTwo = listOf(drtOuterNineteen(), drtInnerSeven(), drtOuterNineteen())
        roundTwo.forEach(state::dartThrown)
        state.commitRound()

        val roundThree = listOf(drtTrebleOne())
        roundThree.forEach(state::dartThrown)

        state.getRoundsForIndividual(pt1).shouldContainExactly(roundOne, roundThree)
        state.getRoundsForIndividual(pt2).shouldContainExactly(listOf(roundTwo))
    }
}

data class TestPlayerState(
    val participant: ParticipantEntity,
    override val completedRounds: MutableList<List<Dart>> = mutableListOf(),
    override val currentRound: MutableList<Dart> = mutableListOf(),
    override var isActive: Boolean = false,
    private val scoreSoFar: Int = -1
) : AbstractPlayerState<TestPlayerState>() {
    override val wrappedParticipant = SingleParticipant(participant)

    override fun getScoreSoFar() = scoreSoFar
}

data class TestTeamState(
    val participants: List<ParticipantEntity>,
    override val completedRounds: MutableList<List<Dart>> = mutableListOf(),
    override val currentRound: MutableList<Dart> = mutableListOf(),
    override var isActive: Boolean = false,
    private val scoreSoFar: Int = -1
) : AbstractPlayerState<TestTeamState>() {
    override val wrappedParticipant = TeamParticipant(insertTeam(), participants)

    override fun getScoreSoFar() = scoreSoFar
}

fun <S : AbstractPlayerState<S>> S.shouldFireStateChange(fn: (state: S) -> Unit) {
    val listener = mockk<PlayerStateListener<S>>(relaxed = true)
    addListener(listener)
    clearMocks(listener)

    fn(this)

    val state = this
    verify { listener.stateChanged(state) }
}

fun <S : AbstractPlayerState<S>> S.shouldNotFireStateChange(fn: (state: S) -> Unit) {
    val listener = mockk<PlayerStateListener<S>>(relaxed = true)
    addListener(listener)
    clearMocks(listener)

    fn(this)

    val state = this
    verifyNotCalled { listener.stateChanged(state) }
}
