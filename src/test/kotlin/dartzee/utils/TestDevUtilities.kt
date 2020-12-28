package dartzee.utils

import dartzee.db.TestAchievementEntity
import dartzee.helper.*
import dartzee.screen.ScreenCache
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JOptionPane

class TestDevUtilities: AbstractTest()
{
    @Test
    fun `Should show an error and return out if there are no games in the DB`()
    {
        DevUtilities.purgeGame()

        dialogFactory.errorsShown.shouldContainExactly("No games to delete.")
        dialogFactory.inputsShown.shouldBeEmpty()
    }

    @Test
    fun `Should not delete any games if info dialog is cancelled`()
    {
        dialogFactory.inputSelection = null
        insertGame(localId = 1)

        DevUtilities.purgeGame()

        dialogFactory.inputsShown.shouldContainExactly("Delete Game")
        dialogFactory.inputOptionsPresented?.size shouldBe 1

        getCountFromTable("Game") shouldBe 1
    }

    @Test
    fun `Should purge the game that was selected on the input dialog`()
    {
        insertGame(localId = 1)
        insertGame(localId = 2)

        dialogFactory.inputSelection = 2L
        dialogFactory.questionOption = JOptionPane.YES_OPTION

        DevUtilities.purgeGame()

        dialogFactory.inputsShown.shouldContainExactly("Delete Game")
        dialogFactory.inputOptionsPresented?.size shouldBe 2

        getCountFromTable("Game") shouldBe 1
        retrieveGame().localId shouldBe 1
    }

    @Test
    fun `Should show an error for trying to delete a game that doesnt exist`()
    {
        insertGame(localId = 5)

        DevUtilities.purgeGame(10)

        dialogFactory.errorsShown.shouldContainExactly("No game exists for ID 10")
        dialogFactory.questionsShown.shouldBeEmpty()
        getCountFromTable("Game") shouldBe 1
    }

    @Test
    fun `Should not delete a game which is open`()
    {
        val game = insertGame(localId = 5)

        ScreenCache.addDartsGameScreen(game.rowId, TestAchievementEntity.FakeDartsScreen())

        DevUtilities.purgeGame(5)

        dialogFactory.errorsShown.shouldContainExactly("Cannot delete a game that's open.")
        dialogFactory.questionsShown.shouldBeEmpty()
        getCountFromTable("Game") shouldBe 1
    }

    @Test
    fun `Should not delete a game if cancelled`()
    {
        insertGame(localId = 5)
        dialogFactory.questionOption = JOptionPane.NO_OPTION

        DevUtilities.purgeGame(5)

        dialogFactory.questionsShown.shouldHaveSize(1)
        getCountFromTable("Game") shouldBe 1
    }

    @Test
    fun `Should delete the specified game, along with associated Participants and Darts`()
    {
        val g1 = insertGame(localId = 1)
        val g2 = insertGame(localId = 2)


        val p = insertPlayer()
        val pt1 = insertParticipant(playerId = p.rowId, gameId = g1.rowId)
        val pt2 = insertParticipant(playerId = p.rowId, gameId = g2.rowId)

        val d1 = insertDart(pt1, ordinal = 1, score = 20, multiplier = 1, startingScore = 501)

        insertDart(pt2, ordinal = 1, score = 20, multiplier = 2, startingScore = 501)
        insertDart(pt2, ordinal = 2, score = 20, multiplier = 3, startingScore = 461)

        dialogFactory.questionOption = JOptionPane.YES_OPTION

        DevUtilities.purgeGame(2)

        dialogFactory.questionsShown.shouldHaveSize(1)
        val q = dialogFactory.questionsShown.first()

        q.shouldContain("Purge all data for Game #2?")
        q.shouldContain("Participant: 1 rows")
        q.shouldContain("Dart: 2 rows")

        getCountFromTable("Game") shouldBe 1
        getCountFromTable("Participant") shouldBe 1
        getCountFromTable("Dart") shouldBe 1

        retrieveGame().rowId shouldBe g1.rowId
        retrieveParticipant().rowId shouldBe pt1.rowId
        retrieveDart().rowId shouldBe d1.rowId
    }
}