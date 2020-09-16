package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.db.DartEntity
import dartzee.db.ParticipantEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertParticipant
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Point

class TestAbstractPlayerState: AbstractTest()
{
    @Test
    fun `it should take a copy of the darts that are added`()
    {
        val state = DefaultPlayerState(insertParticipant())
        val darts = mutableListOf(Dart(20, 1))

        state.addDarts(darts)

        darts.clear()
        state.darts.first().shouldContainExactly(Dart(20, 1))
    }

    @Test
    fun `It should populate the darts with the ParticipantId`()
    {
        val pt = insertParticipant()
        val state = DefaultPlayerState(pt)

        val dart = Dart(20, 1)
        dart.participantId shouldBe ""

        state.addDarts(mutableListOf(dart))

        dart.participantId shouldBe pt.rowId
    }

    @Test
    fun `Should support resetting the currently thrown darts`()
    {
        val state = DefaultPlayerState(insertParticipant())

        state.dartThrown(Dart(20, 1))
        state.dartsThrown.shouldContainExactly(Dart(20, 1))

        state.resetRound()
        state.dartsThrown.shouldBeEmpty()
    }

    @Test
    fun `Should support committing a round of darts and saving them to the database`()
    {
        val pt = insertParticipant(insertPlayer = true)
        val dartOne = Dart(20, 1, Point(50, 50), SegmentType.OUTER_SINGLE)
        val dartTwo = Dart(5, 1, Point(40, 45), SegmentType.OUTER_SINGLE)
        val dartThree = Dart(1, 1, Point(60, 45), SegmentType.OUTER_SINGLE)

        val state = DefaultPlayerState(pt)
        state.dartThrown(dartOne)
        state.dartThrown(dartTwo)
        state.dartThrown(dartThree)

        state.commitRound()
        state.dartsThrown.shouldBeEmpty()
        state.darts.shouldContainExactly(listOf(listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1))))

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
    private fun validateDartEntity(dartEntity: DartEntity, originalDart: Dart)
    {
        dartEntity.multiplier shouldBe originalDart.multiplier
        dartEntity.score shouldBe originalDart.score
        dartEntity.posX shouldBe originalDart.getX()
        dartEntity.posY shouldBe originalDart.getY()
        dartEntity.segmentType shouldBe originalDart.segmentType
    }

    @Test
    fun `Should correctly compute the current round number`()
    {
        val state = DefaultPlayerState(insertParticipant())
        state.currentRoundNumber() shouldBe 1

        state.dartThrown(Dart(1, 1, Point(0, 0)))
        state.currentRoundNumber() shouldBe 1

        state.commitRound()
        state.currentRoundNumber() shouldBe 2

        state.dartThrown(Dart(20, 1, Point(0, 0)))
        state.currentRoundNumber() shouldBe 2

        state.resetRound()
        state.currentRoundNumber() shouldBe 2
    }

    data class DefaultPlayerState(override val pt: ParticipantEntity,
                                  override val darts: MutableList<List<Dart>> = mutableListOf(),
                                  override val dartsThrown: MutableList<Dart> = mutableListOf()): AbstractPlayerState()
    {
        override fun getScoreSoFar() = -1
    }
}
