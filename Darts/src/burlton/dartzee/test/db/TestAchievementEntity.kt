package burlton.dartzee.test.db

import burlton.dartzee.code.achievements.*
import burlton.dartzee.code.achievements.golf.AchievementGolfPointsRisked
import burlton.dartzee.code.achievements.x01.AchievementX01BestFinish
import burlton.dartzee.code.achievements.x01.AchievementX01BestGame
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.screen.game.DartsGameScreen
import burlton.dartzee.test.helper.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementEntity: AbstractEntityTest<AchievementEntity>()
{
    override fun factoryDao() = AchievementEntity()

    @Test
    fun `Should retrieve achievements for which no gameIdEarned is set`()
    {
        val p = insertPlayer()
        val a = insertAchievement(playerId = p.rowId, gameIdEarned = "")

        val achievements = AchievementEntity.retrieveAchievements(p.rowId)
        achievements.size shouldBe 1

        val achievement = achievements.first()
        achievement.rowId shouldBe a.rowId
        achievement.playerId shouldBe a.playerId
        achievement.retrievedFromDb shouldBe true
        achievement.localGameIdEarned shouldBe -1
    }

    @Test
    fun `Should only retrieve achievements for the specified player`()
    {
        val p1 = insertPlayer()
        val p2 = insertPlayer()

        val a1 = insertAchievement(playerId = p1.rowId)
        insertAchievement(playerId = p2.rowId)

        val achievements = AchievementEntity.retrieveAchievements(p1.rowId)

        achievements.size shouldBe 1
        achievements.first().playerId shouldBe p1.rowId
        achievements.first().rowId shouldBe a1.rowId

    }

    @Test
    fun `Should populate LocalGameIdEarned correctly when a linked game exists`()
    {
        val p = insertPlayer()
        val g = insertGame(localId = 72)

        insertAchievement(playerId = p.rowId, gameIdEarned = g.rowId)

        val achievements = AchievementEntity.retrieveAchievements(p.rowId)

        val achievement = achievements.first()
        achievement.localGameIdEarned shouldBe 72
    }

    @Test
    fun `Should return an empty list if no achievements are found`()
    {
        insertAchievement(playerId = randomGuid())

        val achievements = AchievementEntity.retrieveAchievements(randomGuid())
        achievements.shouldBeEmpty()
    }

    @Test
    fun `Should return null if no achievement exists`()
    {
        val playerId = randomGuid()

        insertAchievement(playerId = playerId, achievementRef = ACHIEVEMENT_REF_GOLF_BEST_GAME)
        insertAchievement(playerId = randomGuid(), achievementRef = ACHIEVEMENT_REF_X01_BEST_GAME)

        AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_X01_BEST_GAME, playerId) shouldBe null
    }

    @Test
    fun `Should retrieve an achievement by playerId and ref`()
    {
        val playerId = randomGuid()
        val achievementRef = ACHIEVEMENT_REF_GOLF_BEST_GAME

        val a = insertAchievement(playerId = playerId, achievementRef = achievementRef)

        val a2 = AchievementEntity.retrieveAchievement(achievementRef, playerId)!!

        a2.rowId shouldBe a.rowId
        a2.achievementRef shouldBe achievementRef
        a2.playerId shouldBe playerId
    }

    @Test
    fun `updateAchievement - should insert a fresh achievement row if none are present`()
    {
        getCountFromTable("Achievement") shouldBe 0

        val ref = ACHIEVEMENT_REF_X01_BEST_GAME
        val playerId = randomGuid()
        val gameId = randomGuid()

        val gameScreen = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(gameId, gameScreen)

        AchievementEntity.updateAchievement(ref, playerId, gameId, 54)

        val a = AchievementEntity.retrieveAchievement(ref, playerId)!!
        a.gameIdEarned shouldBe gameId
        a.achievementCounter shouldBe 54
        a.playerId shouldBe playerId
        a.achievementRef shouldBe ref

        gameScreen.attainedValue shouldBe 54
    }

    @Test
    fun `updateAchievement - should preserve an increasing achievement for values that are not strictly greater`()
    {
        val ref = ACHIEVEMENT_REF_X01_BEST_FINISH
        val playerId = randomGuid()
        val oldGameId = randomGuid()
        val newGameId = randomGuid()

        val gameScreen = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(newGameId, gameScreen)

        insertAchievement(achievementRef = ref, playerId = playerId, gameIdEarned = oldGameId, achievementCounter = 100)

        AchievementEntity.updateAchievement(ref, playerId, newGameId, 99)
        AchievementEntity.updateAchievement(ref, playerId, newGameId, 100)

        val a = AchievementEntity.retrieveAchievement(ref, playerId)!!
        a.gameIdEarned shouldBe oldGameId
        a.achievementCounter shouldBe 100
        gameScreen.playerId shouldBe null
    }

    @Test
    fun `updateAchievement - should update an increasing achievement for a value that is strictly greater`()
    {
        val ref = ACHIEVEMENT_REF_X01_BEST_FINISH
        val playerId = randomGuid()
        val oldGameId = randomGuid()
        val newGameId = randomGuid()

        val oldValue = AchievementX01BestFinish().greenThreshold
        val newValue = AchievementX01BestFinish().blueThreshold

        val gameScreen = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(newGameId, gameScreen)

        insertAchievement(achievementRef = ref, playerId = playerId, gameIdEarned = oldGameId, achievementCounter = oldValue)

        AchievementEntity.updateAchievement(ref, playerId, newGameId, newValue)

        val a = AchievementEntity.retrieveAchievement(ref, playerId)!!
        a.gameIdEarned shouldBe newGameId
        a.achievementCounter shouldBe newValue
        gameScreen.attainedValue shouldBe newValue
    }


    @Test
    fun `updateAchievement - should preserve a decreasing achievement for values that are not strictly less`()
    {
        val ref = ACHIEVEMENT_REF_X01_BEST_GAME
        val playerId = randomGuid()
        val oldGameId = randomGuid()
        val newGameId = randomGuid()

        val gameScreen = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(newGameId, gameScreen)

        insertAchievement(achievementRef = ref, playerId = playerId, gameIdEarned = oldGameId, achievementCounter = 100)

        AchievementEntity.updateAchievement(ref, playerId, newGameId, 101)
        AchievementEntity.updateAchievement(ref, playerId, newGameId, 100)

        val a = AchievementEntity.retrieveAchievement(ref, playerId)!!
        a.gameIdEarned shouldBe oldGameId
        a.achievementCounter shouldBe 100
        gameScreen.playerId shouldBe null
    }

    @Test
    fun `updateAchievement - should update a decreasing achievement for a value that is strictly less`()
    {
        val ref = ACHIEVEMENT_REF_X01_BEST_GAME
        val playerId = randomGuid()
        val oldGameId = randomGuid()
        val newGameId = randomGuid()

        val oldValue = AchievementX01BestGame().greenThreshold
        val newValue = AchievementX01BestGame().blueThreshold

        val gameScreen = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(newGameId, gameScreen)

        insertAchievement(achievementRef = ref, playerId = playerId, gameIdEarned = oldGameId, achievementCounter = oldValue)

        AchievementEntity.updateAchievement(ref, playerId, newGameId, newValue)

        val a = AchievementEntity.retrieveAchievement(ref, playerId)!!
        a.gameIdEarned shouldBe newGameId
        a.achievementCounter shouldBe newValue
        gameScreen.attainedValue shouldBe newValue
    }

    @Test
    fun `incrementAchievement - Should insert a new row if none present`()
    {
        val ref = ACHIEVEMENT_REF_X01_BEST_GAME
        val playerId = randomGuid()
        val gameId = randomGuid()

        val gameScreen = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(gameId, gameScreen)

        AchievementEntity.incrementAchievement(ref, playerId, gameId)

        val a = AchievementEntity.retrieveAchievement(ref, playerId)!!
        a.achievementCounter shouldBe 1
        a.playerId shouldBe playerId
        a.gameIdEarned shouldBe ""
        a.achievementRef shouldBe ref

        gameScreen.playerId shouldBe playerId
    }

    @Test
    fun `incrementAchievement - should increment by 1 by default`()
    {
        val ref = ACHIEVEMENT_REF_X01_BEST_GAME
        val playerId = randomGuid()

        insertAchievement(playerId = playerId, achievementRef = ref, achievementCounter = 5)

        AchievementEntity.incrementAchievement(ref, playerId, randomGuid())

        val a = AchievementEntity.retrieveAchievement(ref, playerId)!!
        a.achievementCounter shouldBe 6
    }

    @Test
    fun `incrementAchievement - should increment by the specified amount`()
    {
        val ref = ACHIEVEMENT_REF_X01_BEST_GAME
        val playerId = randomGuid()

        insertAchievement(playerId = playerId, achievementRef = ref, achievementCounter = 5)

        AchievementEntity.incrementAchievement(ref, playerId, randomGuid(), 10)

        val a = AchievementEntity.retrieveAchievement(ref, playerId)!!
        a.achievementCounter shouldBe 15
    }

    @Test
    fun `incrementAchievement - should call into triggerAchievementUnlock`()
    {
        val ref = ACHIEVEMENT_REF_GOLF_POINTS_RISKED
        val playerId = randomGuid()

        val oldAmount = AchievementGolfPointsRisked().redThreshold
        val increment = AchievementGolfPointsRisked().yellowThreshold - oldAmount

        insertAchievement(playerId = playerId, achievementRef = ref, achievementCounter = oldAmount)

        val gameId = randomGuid()
        val screen = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(gameId, screen)

        AchievementEntity.incrementAchievement(ref, playerId, gameId, increment)


        screen.playerId shouldBe playerId
        screen.achievementRef shouldBe ref
        screen.attainedValue shouldBe AchievementGolfPointsRisked().yellowThreshold
    }

    @Test
    fun `insertAchievement - Should insert a row with the specified values`()
    {
        val ref = ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR
        val playerId = randomGuid()
        val gameId = randomGuid()
        val detail = "20, 5, 1"

        AchievementEntity.insertAchievement(ref, playerId, gameId, detail)

        val a = retrieveAchievement()
        a.achievementCounter shouldBe -1
        a.achievementRef shouldBe ref
        a.playerId shouldBe playerId
        a.gameIdEarned shouldBe gameId
        a.achievementDetail shouldBe detail
    }

    @Test
    fun `insertAchievement - Should insert empty achievement detail by default`()
    {
        val ref = ACHIEVEMENT_REF_X01_SHANGHAI
        val playerId = randomGuid()
        val gameId = randomGuid()

        AchievementEntity.insertAchievement(ref, playerId, gameId)

        val a = retrieveAchievement()
        a.achievementCounter shouldBe -1
        a.achievementRef shouldBe ref
        a.playerId shouldBe playerId
        a.gameIdEarned shouldBe gameId
        a.achievementDetail shouldBe ""
    }

    @Test
    fun `insertAchievement - Should call into triggerAchievementUnlock`()
    {
        val ref = ACHIEVEMENT_REF_X01_SHANGHAI
        val playerId = randomGuid()
        val gameId = randomGuid()

        //Start with 1 row
        insertAchievement(achievementRef = ref, playerId = playerId)

        val scrn = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(gameId, scrn)

        AchievementEntity.insertAchievement(ref, playerId, gameId)

        scrn.playerId shouldBe playerId
        scrn.achievementRef shouldBe ref
        scrn.attainedValue shouldBe 2
        scrn.gameId shouldBe gameId
    }

    @Test
    fun `triggerAchievementUnlock - should do nothing if achievement has not moved over a grade boundary`()
    {
        val achievement = AchievementX01BestFinish()

        val oldValue = achievement.orangeThreshold
        val newValue = achievement.yellowThreshold - 1

        val gameId = randomGuid()
        val scrn = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(gameId, scrn)

        AchievementEntity.triggerAchievementUnlock(oldValue, newValue, achievement, randomGuid(), gameId)

        scrn.achievementRef shouldBe null
    }

    @Test
    fun `triggerAchievementUnlock - should trigger if achievement has moved over a grade boundary`()
    {
        val achievement = AchievementX01BestFinish()

        val oldValue = achievement.orangeThreshold
        val newValue = achievement.yellowThreshold

        val gameId = randomGuid()
        val scrn = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(gameId, scrn)

        AchievementEntity.triggerAchievementUnlock(oldValue, newValue, achievement, randomGuid(), gameId)

        scrn.achievementRef shouldBe achievement.achievementRef
        scrn.attainedValue shouldBe achievement.yellowThreshold
        scrn.gameId shouldBe gameId
    }

    class FakeDartsScreen: DartsGameScreen()
    {
        var gameId: String? = null
        var playerId: String? = null
        var achievementRef: Int? = null
        var attainedValue: Int? = null

        override fun achievementUnlocked(gameId: String, playerId: String, achievement: AbstractAchievement)
        {
            this.gameId = gameId
            this.playerId = playerId
            this.achievementRef = achievement.achievementRef
            this.attainedValue = achievement.attainedValue
        }
    }
}