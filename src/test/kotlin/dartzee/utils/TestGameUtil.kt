package dartzee.utils

import dartzee.bean.GameParamFilterPanelDartzee
import dartzee.bean.GameParamFilterPanelGolf
import dartzee.bean.GameParamFilterPanelRoundTheClock
import dartzee.bean.GameParamFilterPanelX01
import dartzee.db.ParticipantEntity
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.insertParticipant
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class TestGameUtil : AbstractTest() {
    @Test
    fun `Filter panel mappings`() {
        getFilterPanel(GameType.X01).shouldBeInstanceOf<GameParamFilterPanelX01>()
        getFilterPanel(GameType.GOLF).shouldBeInstanceOf<GameParamFilterPanelGolf>()
        getFilterPanel(GameType.ROUND_THE_CLOCK)
            .shouldBeInstanceOf<GameParamFilterPanelRoundTheClock>()
        getFilterPanel(GameType.DARTZEE).shouldBeInstanceOf<GameParamFilterPanelDartzee>()
    }

    @Test
    fun `Does highest win`() {
        doesHighestWin(GameType.X01) shouldBe false
        doesHighestWin(GameType.GOLF) shouldBe false
        doesHighestWin(GameType.ROUND_THE_CLOCK) shouldBe false
        doesHighestWin(GameType.DARTZEE) shouldBe true
    }

    @Test
    fun `Should save a finishing position of -1 if there is only one participant`() {
        val pt = insertParticipant()

        setFinishingPositions(listOf(pt), insertGame())

        pt.finishingPosition shouldBe -1
    }

    @Test
    fun `Should correctly assign finishing positions when lowest wins`() {
        val pt = insertParticipant(finalScore = 25)
        val pt2 = insertParticipant(finalScore = 40)
        val pt3 = insertParticipant(finalScore = 55)

        setFinishingPositions(listOf(pt, pt3, pt2), insertGame(gameType = GameType.GOLF))

        pt.finishingPosition shouldBe 1
        pt2.finishingPosition shouldBe 2
        pt3.finishingPosition shouldBe 3
    }

    @Test
    fun `Should ignore players who resigned when assigning finishing positions`() {
        val pt = insertParticipant(finalScore = -1, resigned = true, finishingPosition = 4)
        val pt2 = insertParticipant(finalScore = 40)
        val pt3 = insertParticipant(finalScore = 55)
        val pt4 = insertParticipant(finalScore = -1, resigned = true, finishingPosition = 3)

        setFinishingPositions(listOf(pt, pt3, pt2, pt4), insertGame(gameType = GameType.GOLF))

        pt.finishingPosition shouldBe 4
        pt2.finishingPosition shouldBe 1
        pt3.finishingPosition shouldBe 2
        pt4.finishingPosition shouldBe 3
    }

    @Test
    fun `Should correctly handle ties when lowest wins`() {
        val pt = insertParticipant(finalScore = 25)
        val pt2 = insertParticipant(finalScore = 40)
        val pt3 = insertParticipant(finalScore = 55)
        val pt4 = insertParticipant(finalScore = 40)

        setFinishingPositions(listOf(pt, pt3, pt2, pt4), insertGame(gameType = GameType.GOLF))

        pt.finishingPosition shouldBe 1
        pt2.finishingPosition shouldBe 2
        pt4.finishingPosition shouldBe 2
        pt3.finishingPosition shouldBe 4
    }

    @Test
    fun `Should correctly assign finishing positions when highest wins`() {
        val pt = insertParticipant(finalScore = 25)
        val pt2 = insertParticipant(finalScore = 40)
        val pt3 = insertParticipant(finalScore = 55)

        setFinishingPositions(listOf(pt, pt3, pt2), insertGame(gameType = GameType.DARTZEE))

        pt.finishingPosition shouldBe 3
        pt2.finishingPosition shouldBe 2
        pt3.finishingPosition shouldBe 1
    }

    @Test
    fun `Should correctly handle ties when highest wins`() {
        val pt = insertParticipant(finalScore = 25)
        val pt2 = insertParticipant(finalScore = 40)
        val pt3 = insertParticipant(finalScore = 55)
        val pt4 = insertParticipant(finalScore = 40)

        setFinishingPositions(listOf(pt, pt3, pt2, pt4), insertGame(gameType = GameType.DARTZEE))

        pt.finishingPosition shouldBe 4
        pt2.finishingPosition shouldBe 2
        pt4.finishingPosition shouldBe 2
        pt3.finishingPosition shouldBe 1
    }

    @Test
    fun `Should save the finish positions to the database`() {
        val pt = insertParticipant(finalScore = 25)
        val pt2 = insertParticipant(finalScore = 40)

        setFinishingPositions(listOf(pt, pt2), insertGame(gameType = GameType.GOLF))

        val retrievedPt = ParticipantEntity().retrieveForId(pt.rowId)
        retrievedPt!!.finishingPosition shouldBe 1

        val retrievedPt2 = ParticipantEntity().retrieveForId(pt2.rowId)
        retrievedPt2!!.finishingPosition shouldBe 2
    }
}
