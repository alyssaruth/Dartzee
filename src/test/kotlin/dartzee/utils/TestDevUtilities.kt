package dartzee.utils

import com.github.alyssaburlton.swingtest.clickNo
import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.clickYes
import com.github.alyssaburlton.swingtest.flushEdt
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.DartzeeRuleEntity
import dartzee.db.EntityName
import dartzee.db.ParticipantEntity
import dartzee.db.TeamEntity
import dartzee.game.loadParticipants
import dartzee.game.prepareParticipants
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.getQuestionDialog
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.helper.insertDart
import dartzee.helper.insertDartzeeRoundResult
import dartzee.helper.insertFinishForPlayer
import dartzee.helper.insertGame
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.preparePlayers
import dartzee.helper.retrieveDart
import dartzee.helper.retrieveGame
import dartzee.helper.retrieveParticipant
import dartzee.helper.retrieveX01Finish
import dartzee.helper.testRules
import dartzee.only
import dartzee.purgeGameAndConfirm
import dartzee.runAsync
import dartzee.screen.ScreenCache
import dartzee.screen.game.FakeDartsScreen
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

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

        getCountFromTable(EntityName.Game) shouldBe 1
    }

    @Test
    fun `Should purge the game that was selected on the input dialog`()
    {
        insertGame(localId = 1)
        insertGame(localId = 2)

        dialogFactory.inputSelection = 2L

        runAsync { DevUtilities.purgeGame() }

        dialogFactory.inputsShown.shouldContainExactly("Delete Game")
        dialogFactory.inputOptionsPresented?.size shouldBe 2

        getQuestionDialog().clickYes()
        flushEdt()

        getCountFromTable(EntityName.Game) shouldBe 1
        retrieveGame().localId shouldBe 1
    }

    @Test
    fun `Should show an error for trying to delete a game that doesnt exist`()
    {
        insertGame(localId = 5)

        runAsync { DevUtilities.purgeGame(10) }

        val dlg = getErrorDialog()
        dlg.getDialogMessage() shouldBe "No game exists for ID 10"
        dlg.clickOk()
        getCountFromTable(EntityName.Game) shouldBe 1
    }

    @Test
    fun `Should not delete a game which is open`()
    {
        val game = insertGame(localId = 5)

        ScreenCache.addDartsGameScreen(game.rowId, FakeDartsScreen())

        runAsync { DevUtilities.purgeGame(5) }

        val dlg = getErrorDialog()
        dlg.getDialogMessage() shouldBe "Cannot delete a game that's open."
        dlg.clickOk()
        getCountFromTable(EntityName.Game) shouldBe 1
    }

    @Test
    fun `Should not delete a game if cancelled`()
    {
        insertGame(localId = 5)

        runAsync { DevUtilities.purgeGame(5) }

        val dlg = getQuestionDialog()
        dlg.clickNo()
        flushEdt()

        getCountFromTable(EntityName.Game) shouldBe 1
    }

    @Test
    fun `Should delete from X01Finish`()
    {
        val player = insertPlayer()
        val gameOne = insertFinishForPlayer(player, 25)
        val gameTwo = insertFinishForPlayer(player, 80)

        purgeGameAndConfirm(gameOne.localId)

        getCountFromTable(EntityName.X01Finish) shouldBe 1
        retrieveX01Finish().gameId shouldBe gameTwo.rowId
        retrieveX01Finish().finish shouldBe 80
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

        val q = purgeGameAndConfirm(2)

        q.shouldContain("Purge all data for Game #2?")
        q.shouldContain("Participant: 1 rows")
        q.shouldContain("Dart: 2 rows")

        getCountFromTable(EntityName.Game) shouldBe 1
        getCountFromTable(EntityName.Participant) shouldBe 1
        getCountFromTable(EntityName.Dart) shouldBe 1

        retrieveGame().rowId shouldBe g1.rowId
        retrieveParticipant().rowId shouldBe pt1.rowId
        retrieveDart().rowId shouldBe d1.rowId
    }

    @Test
    fun `Should delete teams associated with a game`()
    {
        val players = preparePlayers(5)

        val gameA = insertGame()
        val gameB = insertGame()

        prepareParticipants(gameA.rowId, players, pairMode = true)
        prepareParticipants(gameB.rowId, players, pairMode = true)

        getCountFromTable(EntityName.Team) shouldBe 4

        purgeGameAndConfirm(gameA.localId)

        loadParticipants(gameA.rowId).shouldBeEmpty()
        loadParticipants(gameB.rowId).size shouldBe 3

        ParticipantEntity().countWhere("GameId = '${gameA.rowId}'") shouldBe 0
        TeamEntity().countWhere("GameId = '${gameA.rowId}'") shouldBe 0
    }

    @Test
    fun `Should purge Dartzee gubbins`()
    {
        val player = insertPlayer()
        val g1 = insertGame()
        val g2 = insertGame()

        val pt1 = insertParticipant(playerId = player.rowId, gameId = g1.rowId)
        val pt2 = insertParticipant(playerId = player.rowId, gameId = g2.rowId)

        insertDartzeeRules(g1.rowId, testRules)
        insertDartzeeRules(g2.rowId, testRules)

        insertDartzeeRoundResult(pt1)
        insertDartzeeRoundResult(pt2)

        getCountFromTable(EntityName.DartzeeRule) shouldBe testRules.size * 2
        getCountFromTable(EntityName.DartzeeRoundResult) shouldBe 2

        purgeGameAndConfirm(g1.localId)

        getCountFromTable(EntityName.DartzeeRule) shouldBe testRules.size

        val dartzeeRules = DartzeeRuleEntity().retrieveEntities()
        dartzeeRules.size shouldBe testRules.size
        dartzeeRules.forAll { it.entityId shouldBe g2.rowId }

        val remainingResult = DartzeeRoundResultEntity().retrieveEntities().only()
        remainingResult.participantId shouldBe pt2.rowId
    }
}