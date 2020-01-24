package dartzee.utils

import dartzee.achievements.ACHIEVEMENT_REF_GOLF_GAMES_WON
import dartzee.bean.GameParamFilterPanelDartzee
import dartzee.bean.GameParamFilterPanelGolf
import dartzee.bean.GameParamFilterPanelRoundTheClock
import dartzee.bean.GameParamFilterPanelX01
import dartzee.db.*
import dartzee.helper.AbstractTest
import dartzee.helper.insertAchievement
import dartzee.helper.insertGame
import dartzee.helper.insertParticipant
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.Test
import kotlin.test.assertNotNull

class TestGameUtil: AbstractTest()
{
    @Test
    fun `Sensible descriptions when no params`()
    {
        getTypeDesc(GAME_TYPE_X01) shouldBe "X01"
        getTypeDesc(GAME_TYPE_GOLF) shouldBe "Golf"
        getTypeDesc(GAME_TYPE_ROUND_THE_CLOCK) shouldBe "Round the Clock"
        getTypeDesc(GAME_TYPE_DARTZEE) shouldBe "Dartzee"
        getTypeDesc(-1) shouldBe "<Game Type>"
    }

    @Test
    fun `Filter panel mappings`()
    {
        getFilterPanel(GAME_TYPE_X01).shouldBeInstanceOf<GameParamFilterPanelX01>()
        getFilterPanel(GAME_TYPE_GOLF).shouldBeInstanceOf<GameParamFilterPanelGolf>()
        getFilterPanel(GAME_TYPE_ROUND_THE_CLOCK).shouldBeInstanceOf<GameParamFilterPanelRoundTheClock>()
        getFilterPanel(GAME_TYPE_DARTZEE).shouldBeInstanceOf<GameParamFilterPanelDartzee>()
    }

    @Test
    fun `Does highest win`()
    {
        doesHighestWin(GAME_TYPE_X01) shouldBe false
        doesHighestWin(GAME_TYPE_GOLF) shouldBe false
        doesHighestWin(GAME_TYPE_ROUND_THE_CLOCK) shouldBe false
        doesHighestWin(GAME_TYPE_DARTZEE) shouldBe true
    }

    @Test
    fun `Should save a finishing position of -1 if there is only one participant`()
    {
        val pt = insertParticipant()

        setFinishingPositions(listOf(pt), insertGame())

        pt.finishingPosition shouldBe -1
    }

    @Test
    fun `Should correctly assign finishing positions when lowest wins`()
    {
        val pt = insertParticipant(finalScore = 25)
        val pt2 = insertParticipant(finalScore = 40)
        val pt3 = insertParticipant(finalScore = 55)

        setFinishingPositions(listOf(pt, pt3, pt2), insertGame(gameType = GAME_TYPE_GOLF))

        pt.finishingPosition shouldBe 1
        pt2.finishingPosition shouldBe 2
        pt3.finishingPosition shouldBe 3
    }

    @Test
    fun `Should correctly handle ties when lowest wins`()
    {
        val pt = insertParticipant(finalScore = 25)
        val pt2 = insertParticipant(finalScore = 40)
        val pt3 = insertParticipant(finalScore = 55)
        val pt4 = insertParticipant(finalScore = 40)

        setFinishingPositions(listOf(pt, pt3, pt2, pt4), insertGame(gameType = GAME_TYPE_GOLF))

        pt.finishingPosition shouldBe 1
        pt2.finishingPosition shouldBe 2
        pt4.finishingPosition shouldBe 2
        pt3.finishingPosition shouldBe 4
    }

    @Test
    fun `Should correctly assign finishing positions when highest wins`()
    {
        val pt = insertParticipant(finalScore = 25)
        val pt2 = insertParticipant(finalScore = 40)
        val pt3 = insertParticipant(finalScore = 55)

        setFinishingPositions(listOf(pt, pt3, pt2), insertGame(gameType = GAME_TYPE_DARTZEE))

        pt.finishingPosition shouldBe 3
        pt2.finishingPosition shouldBe 2
        pt3.finishingPosition shouldBe 1
    }

    @Test
    fun `Should correctly handle ties when highest wins`()
    {
        val pt = insertParticipant(finalScore = 25)
        val pt2 = insertParticipant(finalScore = 40)
        val pt3 = insertParticipant(finalScore = 55)
        val pt4 = insertParticipant(finalScore = 40)

        setFinishingPositions(listOf(pt, pt3, pt2, pt4), insertGame(gameType = GAME_TYPE_DARTZEE))

        pt.finishingPosition shouldBe 4
        pt2.finishingPosition shouldBe 2
        pt4.finishingPosition shouldBe 2
        pt3.finishingPosition shouldBe 1
    }

    @Test
    fun `Should save the finish positions to the database`()
    {
        val pt = insertParticipant(finalScore = 25)
        val pt2 = insertParticipant(finalScore = 40)

        setFinishingPositions(listOf(pt, pt2), insertGame(gameType = GAME_TYPE_GOLF))

        val retrievedPt = ParticipantEntity().retrieveForId(pt.rowId)
        retrievedPt!!.finishingPosition shouldBe 1

        val retrievedPt2 = ParticipantEntity().retrieveForId(pt2.rowId)
        retrievedPt2!!.finishingPosition shouldBe 2
    }

    @Test
    fun `Should update the relevant total wins achievement for all winners`()
    {
        val pt = insertParticipant(finalScore = 29)
        val pt2 = insertParticipant(finalScore = 29)
        val pt3 = insertParticipant(finalScore = 50)

        insertAchievement(achievementRef = ACHIEVEMENT_REF_GOLF_GAMES_WON, playerId = pt.playerId, achievementCounter = 1)

        setFinishingPositions(listOf(pt, pt2, pt3), insertGame(gameType = GAME_TYPE_GOLF))

        val a1 = AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_GOLF_GAMES_WON, pt.playerId)
        assertNotNull(a1)
        a1.achievementCounter shouldBe 2

        val a2 = AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_GOLF_GAMES_WON, pt2.playerId)
        assertNotNull(a2)
        a2.achievementCounter shouldBe 1

        AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_GOLF_GAMES_WON, pt3.playerId) shouldBe null
    }
}