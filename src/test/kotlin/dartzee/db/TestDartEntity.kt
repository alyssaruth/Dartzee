package dartzee.db

import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.helper.randomGuid
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.awt.Point

class TestDartEntity: AbstractEntityTest<DartEntity>()
{
    override fun factoryDao() = DartEntity()

    @Test
    fun `Should factory with the correct values`()
    {
        val dart = Dart(20, 3, Point(5, 5), SegmentType.TREBLE)
        dart.startingScore = 301
        val playerId = randomGuid()
        val participantId = randomGuid()

        val de = DartEntity.factory(dart, playerId, participantId, 5, 1)

        de.rowId shouldNotBe ""
        de.playerId shouldBe playerId
        de.participantId shouldBe participantId
        de.roundNumber shouldBe 5
        de.ordinal shouldBe 1
        de.startingScore shouldBe 301
        de.score shouldBe 20
        de.multiplier shouldBe 3
        de.posX shouldBe 5
        de.posY shouldBe 5
        de.segmentType shouldBe SegmentType.TREBLE
    }
}