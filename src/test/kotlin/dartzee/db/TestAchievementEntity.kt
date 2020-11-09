package dartzee.db

import dartzee.achievements.*
import dartzee.achievements.x01.AchievementX01BestFinish
import dartzee.achievements.x01.AchievementX01BestGame
import dartzee.game.GameType
import dartzee.helper.*
import dartzee.screen.ScreenCache
import dartzee.screen.game.AbstractDartsGameScreen
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

        insertAchievement(playerId = playerId, type = AchievementType.GOLF_BEST_GAME)
        insertAchievement(playerId = randomGuid(), type = AchievementType.X01_BEST_GAME)

        AchievementEntity.retrieveAchievement(AchievementType.X01_BEST_GAME, playerId) shouldBe null
    }

    @Test
    fun `Should retrieve an achievement by playerId and ref`()
    {
        val playerId = randomGuid()
        val type = AchievementType.GOLF_BEST_GAME

        val a = insertAchievement(playerId = playerId, type = type)

        val a2 = AchievementEntity.retrieveAchievement(type, playerId)!!

        a2.rowId shouldBe a.rowId
        a2.achievementType shouldBe type
        a2.playerId shouldBe playerId
    }

    @Test
    fun `updateAchievement - should insert a fresh achievement row if none are present`()
    {
        getCountFromTable("Achievement") shouldBe 0

        val ref = AchievementType.X01_BEST_GAME
        val playerId = randomGuid()
        val gameId = randomGuid()

        val gameScreen = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(gameId, gameScreen)

        AchievementEntity.updateAchievement(ref, playerId, gameId, 54)

        val a = AchievementEntity.retrieveAchievement(ref, playerId)!!
        a.gameIdEarned shouldBe gameId
        a.achievementCounter shouldBe 54
        a.playerId shouldBe playerId
        a.achievementType shouldBe ref

        gameScreen.attainedValue shouldBe 54
    }

    @Test
    fun `updateAchievement - should preserve an increasing achievement for values that are not strictly greater`()
    {
        val ref = AchievementType.X01_BEST_FINISH
        val playerId = randomGuid()
        val oldGameId = randomGuid()
        val newGameId = randomGuid()

        val gameScreen = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(newGameId, gameScreen)

        insertAchievement(type = ref, playerId = playerId, gameIdEarned = oldGameId, achievementCounter = 100)

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
        val ref = AchievementType.X01_BEST_FINISH
        val playerId = randomGuid()
        val oldGameId = randomGuid()
        val newGameId = randomGuid()

        val oldValue = AchievementX01BestFinish().greenThreshold
        val newValue = AchievementX01BestFinish().blueThreshold

        val gameScreen = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(newGameId, gameScreen)

        insertAchievement(type = ref, playerId = playerId, gameIdEarned = oldGameId, achievementCounter = oldValue)

        AchievementEntity.updateAchievement(ref, playerId, newGameId, newValue)

        val a = AchievementEntity.retrieveAchievement(ref, playerId)!!
        a.gameIdEarned shouldBe newGameId
        a.achievementCounter shouldBe newValue
        gameScreen.attainedValue shouldBe newValue
    }


    @Test
    fun `updateAchievement - should preserve a decreasing achievement for values that are not strictly less`()
    {
        val ref = AchievementType.X01_BEST_GAME
        val playerId = randomGuid()
        val oldGameId = randomGuid()
        val newGameId = randomGuid()

        val gameScreen = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(newGameId, gameScreen)

        insertAchievement(type = ref, playerId = playerId, gameIdEarned = oldGameId, achievementCounter = 100)

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
        val ref = AchievementType.X01_BEST_GAME
        val playerId = randomGuid()
        val oldGameId = randomGuid()
        val newGameId = randomGuid()

        val oldValue = AchievementX01BestGame().greenThreshold
        val newValue = AchievementX01BestGame().blueThreshold

        val gameScreen = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(newGameId, gameScreen)

        insertAchievement(type = ref, playerId = playerId, gameIdEarned = oldGameId, achievementCounter = oldValue)

        AchievementEntity.updateAchievement(ref, playerId, newGameId, newValue)

        val a = AchievementEntity.retrieveAchievement(ref, playerId)!!
        a.gameIdEarned shouldBe newGameId
        a.achievementCounter shouldBe newValue
        gameScreen.attainedValue shouldBe newValue
    }

    @Test
    fun `insertAchievement - Should insert a row with the specified values`()
    {
        val ref = AchievementType.X01_HOTEL_INSPECTOR
        val playerId = randomGuid()
        val gameId = randomGuid()
        val detail = "20, 5, 1"

        AchievementEntity.insertAchievement(ref, playerId, gameId, detail)

        val a = retrieveAchievement()
        a.achievementCounter shouldBe -1
        a.achievementType shouldBe ref
        a.playerId shouldBe playerId
        a.gameIdEarned shouldBe gameId
        a.achievementDetail shouldBe detail
    }

    @Test
    fun `insertAchievement - Should insert empty achievement detail by default`()
    {
        val ref = AchievementType.X01_SHANGHAI
        val playerId = randomGuid()
        val gameId = randomGuid()

        AchievementEntity.insertAchievement(ref, playerId, gameId)

        val a = retrieveAchievement()
        a.achievementCounter shouldBe -1
        a.achievementType shouldBe ref
        a.playerId shouldBe playerId
        a.gameIdEarned shouldBe gameId
        a.achievementDetail shouldBe ""
    }

    @Test
    fun `insertAchievement - Should call into triggerAchievementUnlock`()
    {
        val ref = AchievementType.X01_SHANGHAI
        val playerId = randomGuid()
        val gameId = randomGuid()

        //Start with 1 row
        insertAchievement(type = ref, playerId = playerId)

        val scrn = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(gameId, scrn)

        AchievementEntity.insertAchievement(ref, playerId, gameId)

        scrn.playerId shouldBe playerId
        scrn.achievementType shouldBe ref
        scrn.attainedValue shouldBe 2
        scrn.gameId shouldBe gameId
    }

    @Test
    fun `insertAchievementWithCounter - Should insert a row with the specified values`()
    {
        val ref = AchievementType.GOLF_POINTS_RISKED
        val playerId = randomGuid()
        val gameId = randomGuid()

        AchievementEntity.insertAchievementWithCounter(ref, playerId, gameId, "10", 5)

        val a = retrieveAchievement()
        a.achievementCounter shouldBe 5
        a.achievementType shouldBe ref
        a.playerId shouldBe playerId
        a.gameIdEarned shouldBe gameId
        a.achievementDetail shouldBe "10"
    }

    @Test
    fun `insertAchievementWithCounter - Should call into triggerAchievementUnlock`()
    {
        val ref = AchievementType.GOLF_POINTS_RISKED
        val playerId = randomGuid()
        val gameId = randomGuid()

        //Start with 1 row for 6 points
        insertAchievement(type = ref, playerId = playerId, achievementCounter = 6, achievementDetail = "2")

        val scrn = FakeDartsScreen()
        ScreenCache.addDartsGameScreen(gameId, scrn)

        AchievementEntity.insertAchievementWithCounter(ref, playerId, gameId, "10", 5)

        scrn.playerId shouldBe playerId
        scrn.achievementType shouldBe ref
        scrn.attainedValue shouldBe 11
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

        scrn.achievementType shouldBe null
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

        scrn.achievementType shouldBe achievement.achievementType
        scrn.attainedValue shouldBe achievement.yellowThreshold
        scrn.gameId shouldBe gameId
    }

    class FakeDartsScreen: AbstractDartsGameScreen(2, GameType.X01)
    {
        override val windowName = "Fake"

        var gameId: String? = null
        var playerId: String? = null
        var achievementType: AchievementType? = null
        var attainedValue: Int? = null

        override fun achievementUnlocked(gameId: String, playerId: String, achievement: AbstractAchievement)
        {
            this.gameId = gameId
            this.playerId = playerId
            this.achievementType = achievement.achievementType
            this.attainedValue = achievement.attainedValue
        }

        override fun fireAppearancePreferencesChanged()
        {

        }
    }
}