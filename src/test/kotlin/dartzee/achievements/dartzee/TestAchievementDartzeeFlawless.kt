package dartzee.achievements.dartzee

import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.achievements.AchievementType
import dartzee.dartzee.DartzeeRoundResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.helper.*
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.insertDartzeeRules
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementDartzeeFlawless: AbstractMultiRowAchievementTest<AchievementDartzeeFlawless>()
{
    private val testRules = listOf(twoBlackOneWhite, scoreEighteens, innerOuterInner, totalIsFifty, allTwenties)

    override fun factoryAchievement() = AchievementDartzeeFlawless()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId, finalScore = 275, database = database)

        insertSuccessRoundResults(pt, testRules, database)
    }

    @Test
    fun `Should ignore games with fewer than 5 rules`()
    {
        val pt = insertRelevantParticipant(finalScore = 120)
        val shortList = testRules.subList(0, DARTZEE_ACHIEVEMENT_MIN_RULES - 1)
        insertSuccessRoundResults(pt, shortList)

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore games where a rule was failed`()
    {
        val pt = insertRelevantParticipant(finalScore = 120)
        insertDartzeeRules(pt.gameId, testRules)

        val roundResults = listOf(DartzeeRoundResult(1, true, 100), DartzeeRoundResult(2, false, -50))
        roundResults.forEach { DartzeeRoundResultEntity.factoryAndSave(it, pt, it.ruleNumber + 1) }

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore unfinished games`()
    {
        val pt = insertRelevantParticipant(finalScore = -1)
        insertSuccessRoundResults(pt, testRules)

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should include template name and final score`()
    {
        val p = insertPlayer()
        val t = insertDartzeeTemplate(name = "Goomba")
        val g = insertGame(gameType = GameType.DARTZEE, gameParams = t.rowId)
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = 250)
        insertSuccessRoundResults(pt, testRules)

        factoryAchievement().populateForConversion(emptyList())
        val achievement = retrieveAchievement()
        achievement.playerId shouldBe p.rowId
        achievement.achievementCounter shouldBe 250
        achievement.achievementDetail shouldBe "Goomba"
        achievement.gameIdEarned shouldBe g.rowId
        achievement.achievementType shouldBe AchievementType.DARTZEE_FLAWLESS
    }

    @Test
    fun `Should insert a row per game`()
    {
        val player = insertPlayer()
        val pt = insertRelevantParticipant(player, 100)
        val pt2 = insertRelevantParticipant(player, 150)

        insertSuccessRoundResults(pt, testRules)
        insertSuccessRoundResults(pt2, testRules)

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 2
    }

    private fun insertSuccessRoundResults(participant: ParticipantEntity, rules: List<DartzeeRuleDto>, database: Database = mainDatabase)
    {
        insertDartzeeRules(participant.gameId, rules, database)

        val roundResults = List(rules.size) { ix -> DartzeeRoundResult(ix, true, 50) }
        roundResults.forEach { DartzeeRoundResultEntity.factoryAndSave(it, participant, it.ruleNumber + 1, database) }
    }
}