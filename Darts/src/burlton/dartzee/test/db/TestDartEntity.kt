package burlton.dartzee.test.db

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.SEGMENT_TYPE_TREBLE
import burlton.dartzee.code.db.DartEntity
import burlton.dartzee.test.helper.randomGuid
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import java.awt.Point

class TestDartEntity: AbstractEntityTest<DartEntity>()
{
    override fun factoryDao() = DartEntity()

    @Test
    fun `Should factory and save with the correct values`()
    {
        val dart = Dart(20, 3, Point(5, 5), SEGMENT_TYPE_TREBLE)
        val roundId = randomGuid()

        val de = DartEntity.factoryAndSave(dart, roundId, 1, 501)

        de.rowId shouldNotBe ""
        de.score shouldBe 20
        de.multiplier shouldBe 3
        de.posX shouldBe 5
        de.posY shouldBe 5
        de.roundId shouldBe roundId
        de.ordinal shouldBe 1
        de.startingScore shouldBe 501
        de.segmentType shouldBe SEGMENT_TYPE_TREBLE

        de.retrieveForId(de.rowId) shouldNotBe null
    }
}