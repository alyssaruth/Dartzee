package dartzee.achievements.dartzee

import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.achievements.AchievementType
import dartzee.dartzee.DartzeeRoundResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.retrieveAchievement
import dartzee.helper.testRules
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.insertDartzeeRules
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementDartzeeUnderPressure :
    AbstractMultiRowAchievementTest<AchievementDartzeeUnderPressure>() {
    override fun factoryAchievement() = AchievementDartzeeUnderPressure()

    override fun setUpAchievementRowForPlayerAndGame(
        p: PlayerEntity,
        g: GameEntity,
        database: Database
    ) {
        val pt =
            insertParticipant(
                gameId = g.rowId,
                playerId = p.rowId,
                finalScore = 275,
                database = database
            )
        insertValidRoundResult(pt, testRules, database)
    }

    @Test
    fun `Should include participants who were part of a team`() {
        val pt = insertRelevantParticipant(finalScore = 120, team = true)
        insertValidRoundResult(pt, testRules)

        runConversion()
        getAchievementCount() shouldBe 1
    }

    @Test
    fun `Should ignore games with fewer than 5 rounds`() {
        val pt = insertRelevantParticipant(finalScore = 120)
        val shortList = testRules.subList(0, DARTZEE_ACHIEVEMENT_MIN_ROUNDS - 2)
        insertValidRoundResult(pt, shortList)

        runConversion()
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore unfinished games`() {
        val pt = insertRelevantParticipant(finalScore = -1)
        insertValidRoundResult(pt, testRules)

        runConversion()
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore games where the hardest rule was passed early`() {
        val pt = insertRelevantParticipant(finalScore = 120)
        insertDartzeeRules(pt.gameId, testRules)

        val hardestRuleResult = getHardestRulePass(testRules)
        DartzeeRoundResultEntity.factoryAndSave(hardestRuleResult, pt, testRules.size)

        val lastResult = DartzeeRoundResult(1, true, 20)
        DartzeeRoundResultEntity.factoryAndSave(lastResult, pt, testRules.size + 1)

        runConversion()
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore games where the hardest rule was done last but failed`() {
        val pt = insertRelevantParticipant(finalScore = 120)
        insertDartzeeRules(pt.gameId, testRules)

        val hardestRuleResult = DartzeeRoundResult(testRules.size, false, 60)
        DartzeeRoundResultEntity.factoryAndSave(hardestRuleResult, pt, testRules.size + 1)

        runConversion()
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should include the round score and description of the rule`() {
        val pt = insertRelevantParticipant(finalScore = 275)
        val drr = insertValidRoundResult(pt, testRules)

        runConversion()

        val a = retrieveAchievement()
        a.achievementType shouldBe AchievementType.DARTZEE_UNDER_PRESSURE
        a.achievementCounter shouldBe 50
        a.achievementDetail shouldBe "Total = 50"
        a.gameIdEarned shouldBe pt.gameId
        a.dtAchieved shouldBe drr.dtCreation
    }

    @Test
    fun `Should insert a row per game`() {
        val player = insertPlayer()
        val pt = insertRelevantParticipant(player, 100)
        val pt2 = insertRelevantParticipant(player, 150)

        insertValidRoundResult(pt, testRules)
        insertValidRoundResult(pt2, testRules)

        runConversion()
        getAchievementCount() shouldBe 2
    }

    private fun insertValidRoundResult(
        participant: ParticipantEntity,
        rules: List<DartzeeRuleDto>,
        database: Database = mainDatabase
    ): DartzeeRoundResultEntity {
        insertDartzeeRules(participant.gameId, rules, database)
        val roundResult = getHardestRulePass(rules)
        return DartzeeRoundResultEntity.factoryAndSave(
            roundResult,
            participant,
            rules.size + 1,
            database
        )
    }

    private fun getHardestRulePass(rules: List<DartzeeRuleDto>) =
        DartzeeRoundResult(rules.size, true, 50)
}
