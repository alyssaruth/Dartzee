package dartzee.e2e

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.waitForAssertion
import dartzee.achievements.AchievementType
import dartzee.confirmGameDeletion
import dartzee.db.AchievementEntity
import dartzee.db.EntityName
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameLaunchParams
import dartzee.game.GameLauncher
import dartzee.game.GameType
import dartzee.helper.AbstractRegistryTest
import dartzee.helper.TEST_DB_DIRECTORY
import dartzee.helper.TEST_ROOT
import dartzee.helper.getCountFromTable
import dartzee.helper.retrieveGame
import dartzee.helper.retrieveParticipant
import dartzee.helper.wipeTable
import dartzee.purgeGameAndConfirm
import dartzee.screen.DartsApp
import dartzee.screen.ScreenCache
import dartzee.screen.UtilitiesScreen
import dartzee.screen.sync.SyncManagementPanel
import dartzee.screen.sync.SyncManagementScreen
import dartzee.screen.sync.SyncProgressDialog
import dartzee.screen.sync.SyncSetupPanel
import dartzee.sync.AmazonS3RemoteDatabaseStore
import dartzee.sync.SyncConfigurer
import dartzee.sync.SyncManager
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings
import dartzee.utils.PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import javax.swing.JButton
import javax.swing.JOptionPane

class SyncE2E: AbstractRegistryTest()
{
    override fun getPreferencesAffected() = listOf(PREFERENCES_INT_AI_SPEED, PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE)

    @BeforeEach
    fun before()
    {
        InjectedThings.databaseDirectory = TEST_DB_DIRECTORY
        InjectedThings.mainDatabase = Database(dbName = UUID.randomUUID().toString())
        DartsDatabaseUtil.initialiseDatabase(InjectedThings.mainDatabase)

        val store = AmazonS3RemoteDatabaseStore("dartzee-unit-test")
        InjectedThings.syncManager = SyncManager(store)
        InjectedThings.syncConfigurer = SyncConfigurer(store)

        File(TEST_DB_DIRECTORY).mkdirs()

        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, 50)
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE, true)
    }

    @AfterEach
    fun after()
    {
        InjectedThings.mainDatabase = Database(inMemory = true)
        File(TEST_ROOT).deleteRecursively()
    }

    @Tag("e2e")
    @Test
    fun `Syncing two games with same local ID`()
    {
        val (winner, loser) = createPlayers()

        val gameId = runGame(winner, loser)
        val losingPtScore = retrieveParticipant(gameId, loser.rowId).finalScore

        val mainScreen = ScreenCache.mainScreen
        ScreenCache.switch<SyncManagementScreen>()
        mainScreen.isVisible = true

        performPush(mainScreen)
        wipeGamesAndResetRemote(mainScreen)

        val secondGameId = runGame(winner, loser)

        performSync(mainScreen)

        GameEntity().retrieveForId(gameId)!!.localId shouldBe 1
        GameEntity().retrieveForId(secondGameId)!!.localId shouldBe 2
        retrieveParticipant(gameId, loser.rowId).finalScore shouldBe losingPtScore

        dialogFactory.infosShown.last() shouldBe "Sync completed successfully!\n\nGames pushed: 1\nGames pulled: 1"

        val x01Wins = AchievementEntity().retrieveEntities("PlayerId = '${winner.rowId}' AND AchievementType = '${AchievementType.X01_GAMES_WON}'")
        x01Wins.size shouldBe 2
    }

    @Tag("e2e")
    @Test
    fun `Syncing deleted data`()
    {
        val (winner, loser) = createPlayers()

        runGame(winner, loser)

        val mainScreen = ScreenCache.mainScreen
        ScreenCache.switch<SyncManagementScreen>()
        mainScreen.isVisible = true

        performPush(mainScreen)
        deleteGame(mainScreen)

        ScreenCache.switch<SyncManagementScreen>()
        mainScreen.clickChild<JButton>(text = "Perform Sync")

        waitForAssertion { SyncProgressDialog.isVisible() shouldBe true }
        waitForAssertion { SyncProgressDialog.isVisible() shouldBe false }

        waitForAssertion { dialogFactory.infosShown.lastOrNull() shouldBe "Sync completed successfully!\n\nGames pushed: 0\nGames pulled: 0" }
        getCountFromTable(EntityName.Game) shouldBe 0
        getCountFromTable(EntityName.Dart) shouldBe 0
        getCountFromTable(EntityName.Participant) shouldBe 0
        getCountFromTable(EntityName.X01Finish) shouldBe 0
    }

    private fun deleteGame(mainScreen: DartsApp)
    {
        ScreenCache.switch<UtilitiesScreen>()
        dialogFactory.inputSelection = 1L
        dialogFactory.questionOption = JOptionPane.YES_OPTION
        mainScreen.clickChild<JButton>(text = "Delete Game", async = true)
        confirmGameDeletion(1)
    }

    private fun runGame(winner: PlayerEntity, loser: PlayerEntity): String
    {
        val params = GameLaunchParams(listOf(winner, loser), GameType.X01, "501", false)
        GameLauncher().launchNewGame(params)

        val gameId = retrieveGame().rowId
        waitForAssertion(15000) { retrieveParticipant(gameId, loser.rowId).isActive() shouldBe false }
        closeOpenGames()
        return gameId
    }

    private fun performPush(mainScreen: DartsApp): String
    {
        val remoteName = UUID.randomUUID().toString()
        dialogFactory.inputSelection = remoteName
        dialogFactory.optionSequence.add("Create '$remoteName'")
        mainScreen.clickChild<JButton>(text = "Get Started > ")

        waitForAssertion { mainScreen.findChild<SyncManagementPanel>() shouldNotBe null }

        return remoteName
    }

    private fun performSync(mainScreen: DartsApp)
    {
        dialogFactory.optionSequence.add("Sync with local data")
        mainScreen.clickChild<JButton>(text = "Get Started > ")
        waitForAssertion { mainScreen.findChild<SyncManagementPanel>() shouldNotBe null }
    }

    private fun wipeGamesAndResetRemote(mainScreen: DartsApp)
    {
        purgeGameAndConfirm(1)
        wipeTable(EntityName.DeletionAudit)
        wipeTable(EntityName.Achievement)

        dialogFactory.questionOption = JOptionPane.YES_OPTION
        mainScreen.clickChild<JButton>(text = "Reset")
        waitForAssertion { mainScreen.findChild<SyncSetupPanel>() shouldNotBe null }
    }
}