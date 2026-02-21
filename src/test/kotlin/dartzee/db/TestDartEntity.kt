package dartzee.db

import dartzee.helper.randomGuid
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class TestDartEntity : AbstractEntityTest<DartEntity>() {
    override fun factoryDao() = DartEntity()

    @Test
    fun `Should factory with the correct values`() {
        val dart = Dart(20, 3, SegmentType.TREBLE)
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
        de.segmentType shouldBe SegmentType.TREBLE
    }
}
