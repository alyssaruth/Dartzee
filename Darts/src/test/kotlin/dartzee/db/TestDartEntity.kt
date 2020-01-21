package dartzee.test.db

import dartzee.`object`.Dart
import dartzee.`object`.SEGMENT_TYPE_TREBLE
import dartzee.db.DartEntity
import dartzee.test.helper.randomGuid
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import java.awt.Point

class TestDartEntity: AbstractEntityTest<DartEntity>()
{
    override fun factoryDao() = DartEntity()

    @Test
    fun `Should factory with the correct values`()
    {
        val dart = Dart(20, 3, Point(5, 5), SEGMENT_TYPE_TREBLE)
        val playerId = randomGuid()
        val participantId = randomGuid()

        val de = DartEntity.factory(dart, playerId, participantId, 5, 1, 501)

        de.rowId shouldNotBe ""
        de.playerId shouldBe playerId
        de.participantId shouldBe participantId
        de.roundNumber shouldBe 5
        de.ordinal shouldBe 1
        de.startingScore shouldBe 501
        de.score shouldBe 20
        de.multiplier shouldBe 3
        de.posX shouldBe 5
        de.posY shouldBe 5
        de.segmentType shouldBe SEGMENT_TYPE_TREBLE
    }
}