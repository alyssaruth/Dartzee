package dartzee.e2e

import dartzee.achievements.AchievementType
import dartzee.confirmGameDeletion
import dartzee.db.AchievementEntity
import dartzee.db.EntityName
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameLaunchParams
import dartzee.game.GameLauncher
import dartzee.game.GameType
import dartzee.getQuestionDialog
import dartzee.helper.DEFAULT_X01_CONFIG
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
import io.github.alyssaruth.swingtest.clickChild
import io.github.alyssaruth.swingtest.clickYes
import io.github.alyssaruth.swingtest.expectQuestionDialog
import io.github.alyssaruth.swingtest.findChild
import io.github.alyssaruth.swingtest.selectOptionFromInputDialog
import io.github.alyssaruth.swingtest.typeIntoInputDialog
import io.github.alyssaruth.swingtest.waitForAssertion
import io.github.alyssaruth.swingtest.waitForInfoDialog
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.io.File
import java.util.UUID
import javax.swing.JButton
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SyncE2E : AbstractE2ETest() {
    @BeforeEach
    override fun beforeEach() {
        super.beforeEach()

        InjectedThings.databaseDirectory = TEST_DB_DIRECTORY
        InjectedThings.mainDatabase = Database(dbName = UUID.randomUUID().toString())
        DartsDatabaseUtil.initialiseDatabase(InjectedThings.mainDatabase)

        val store = AmazonS3RemoteDatabaseStore("dartzee-unit-test")
        InjectedThings.syncManager = SyncManager(store)
        InjectedThings.syncConfigurer = SyncConfigurer(store)

        File(TEST_DB_DIRECTORY).mkdirs()
    }

    @AfterEach
    fun after() {
        InjectedThings.mainDatabase = Database(inMemory = true)
        File(TEST_ROOT).deleteRecursively()
    }

    @Test
    fun `Syncing two games with same local ID`() {
        val (winner, loser) = createPlayers()

        val gameId = runGame(winner, loser)
        val losingPtScore = retrieveParticipant(gameId, loser.rowId).finalScore

        val mainScreen = ScreenCache.mainScreen
        ScreenCache.switch<SyncManagementScreen>()
        mainScreen.isVisible = true

        val remoteName = UUID.randomUUID().toString()
        performPush(mainScreen, remoteName)
        wipeGamesAndResetRemote(mainScreen)

        val secondGameId = runGame(winner, loser)

        performSync(mainScreen, remoteName)

        GameEntity().retrieveForId(gameId)!!.localId shouldBe 1
        GameEntity().retrieveForId(secondGameId)!!.localId shouldBe 2
        retrieveParticipant(gameId, loser.rowId).finalScore shouldBe losingPtScore

        val x01Wins =
            AchievementEntity()
                .retrieveEntities(
                    "PlayerId = '${winner.rowId}' AND AchievementType = '${AchievementType.X01_GAMES_WON}'"
                )
        x01Wins.size shouldBe 2
    }

    @Test
    fun `Syncing deleted data`() {
        val (winner, loser) = createPlayers()

        runGame(winner, loser)

        val mainScreen = ScreenCache.mainScreen
        ScreenCache.switch<SyncManagementScreen>()
        mainScreen.isVisible = true

        performPush(mainScreen, UUID.randomUUID().toString())
        deleteGame(mainScreen)

        ScreenCache.switch<SyncManagementScreen>()
        mainScreen.clickChild<JButton>(text = "Perform Sync")

        waitForAssertion { SyncProgressDialog.isVisible() shouldBe true }
        waitForAssertion { SyncProgressDialog.isVisible() shouldBe false }

        waitForInfoDialog("Sync completed successfully!\nGames pushed: 0\nGames pulled: 0")

        getCountFromTable(EntityName.Game) shouldBe 0
        getCountFromTable(EntityName.Dart) shouldBe 0
        getCountFromTable(EntityName.Participant) shouldBe 0
        getCountFromTable(EntityName.X01Finish) shouldBe 0
    }

    private fun deleteGame(mainScreen: DartsApp) {
        ScreenCache.switch<UtilitiesScreen>()
        mainScreen.clickChild<JButton>(text = "Delete Game")

        selectOptionFromInputDialog("Select Game ID", 1L, title = "Delete Game")
        confirmGameDeletion(1)
    }

    private fun runGame(winner: PlayerEntity, loser: PlayerEntity): String {
        val params =
            GameLaunchParams(
                listOf(winner, loser),
                GameType.X01,
                DEFAULT_X01_CONFIG.toJson(),
                false,
            )
        GameLauncher().launchNewGame(params)

        val gameId = retrieveGame().rowId
        waitForAssertion(15000) {
            retrieveParticipant(gameId, loser.rowId).isActive() shouldBe false
        }
        closeOpenGames()
        return gameId
    }

    private fun performPush(mainScreen: DartsApp, remoteName: String): String {
        mainScreen.clickChild<JButton>(text = "Get Started > ")

        typeIntoInputDialog(
            "Enter a unique name for the shared database (case-sensitive)",
            remoteName,
            title = "Sync Setup",
        )
        expectQuestionDialog(
            "No shared database found called '$remoteName'. Would you like to create it?",
            "Create '$remoteName'",
            title = "Database not found",
        )

        waitForAssertion { mainScreen.findChild<SyncManagementPanel>() shouldNotBe null }

        return remoteName
    }

    private fun performSync(mainScreen: DartsApp, remoteName: String) {
        mainScreen.clickChild<JButton>(text = "Get Started > ")

        typeIntoInputDialog(
            "Enter a unique name for the shared database (case-sensitive)",
            remoteName,
            title = "Sync Setup",
        )

        expectQuestionDialog(
            "Shared database '$remoteName' already exists. How would you like to proceed?",
            "Sync with local data",
            title = "Database found",
        )

        waitForInfoDialog("Sync completed successfully!\nGames pushed: 1\nGames pulled: 1")

        waitForAssertion { mainScreen.findChild<SyncManagementPanel>() shouldNotBe null }
    }

    private fun wipeGamesAndResetRemote(mainScreen: DartsApp) {
        purgeGameAndConfirm(1)
        wipeTable(EntityName.DeletionAudit)
        wipeTable(EntityName.Achievement)

        mainScreen.clickChild<JButton>(text = "Reset")

        val question = getQuestionDialog()
        question.clickYes()

        waitForAssertion { mainScreen.findChild<SyncSetupPanel>() shouldNotBe null }
    }
}
