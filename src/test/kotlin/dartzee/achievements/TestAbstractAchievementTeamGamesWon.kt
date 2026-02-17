package dartzee.achievements

import dartzee.core.util.getSqlDateNow
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.db.TeamEntity
import dartzee.helper.getCountFromTable
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.insertTeam
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.sql.Timestamp
import org.junit.jupiter.api.Test

abstract class TestAbstractAchievementTeamGamesWon<E : AbstractAchievementTeamGamesWon> :
    AbstractMultiRowAchievementTest<E>() {
    override fun setUpAchievementRowForPlayerAndGame(
        p: PlayerEntity,
        g: GameEntity,
        database: Database,
    ) {
        insertWinningTeamAndParticipant(p, g, database = database)
    }

    private fun insertWinningTeamAndParticipant(
        p: PlayerEntity,
        g: GameEntity,
        finalScore: Int = 30,
        dtFinished: Timestamp = getSqlDateNow(),
        database: Database = mainDatabase,
    ): TeamEntity {
        val team =
            insertTeam(
                gameId = g.rowId,
                finishingPosition = 1,
                finalScore = finalScore,
                database = database,
                dtFinished = dtFinished,
            )
        insertParticipant(
            gameId = g.rowId,
            playerId = p.rowId,
            teamId = team.rowId,
            database = database,
        )
        return team
    }

    @Test
    fun `Should ignore participants who did not come 1st`() {
        val alice = insertPlayer(name = "Alice")
        val game = insertRelevantGame()
        val team = insertTeam(gameId = game.rowId, finishingPosition = 2)
        insertParticipant(gameId = game.rowId, playerId = alice.rowId, teamId = team.rowId)

        runConversion()

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore participants who were not part of a team`() {
        val alice = insertPlayer(name = "Alice")
        val game = insertRelevantGame()
        insertParticipant(
            gameId = game.rowId,
            playerId = alice.rowId,
            finishingPosition = 1,
            teamId = "",
        )

        runConversion()

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should insert a row per player and game, and take their latest finish date as DtLastUpdate`() {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        val t1 =
            insertWinningTeamAndParticipant(
                alice,
                insertRelevantGame(),
                dtFinished = Timestamp(500),
                finalScore = 20,
            )
        val t2 =
            insertWinningTeamAndParticipant(
                alice,
                insertRelevantGame(),
                dtFinished = Timestamp(1500),
                finalScore = 45,
            )
        val t3 =
            insertWinningTeamAndParticipant(
                alice,
                insertRelevantGame(),
                dtFinished = Timestamp(1000),
                finalScore = 26,
            )

        insertWinningTeamAndParticipant(bob, insertRelevantGame(), dtFinished = Timestamp(2000))
        insertWinningTeamAndParticipant(bob, insertRelevantGame(), dtFinished = Timestamp(1000))

        runConversion()

        getCountFromTable("Achievement") shouldBe 5
        val achievementRows = AchievementEntity().retrieveEntities("")
        val aliceRows = achievementRows.filter { it.playerId == alice.rowId }
        aliceRows.size shouldBe 3
        val gameIdAndScore = aliceRows.map { Pair(it.gameIdEarned, it.achievementDetail) }
        gameIdAndScore.shouldContainExactlyInAnyOrder(
            Pair(t1.gameId, "20"),
            Pair(t2.gameId, "45"),
            Pair(t3.gameId, "26"),
        )

        val bobRow = achievementRows.filter { it.playerId == bob.rowId }
        bobRow.size shouldBe 2
    }
}
