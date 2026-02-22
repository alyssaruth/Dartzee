package dartzee.screen

import dartzee.achievements.convertEmptyAchievements
import dartzee.core.bean.AbstractDevScreen
import dartzee.core.bean.CheatBar
import dartzee.core.util.DialogUtil
import dartzee.db.GameEntity
import dartzee.logging.CODE_SCREEN_LOAD_ERROR
import dartzee.logging.CODE_SWITCHED_SCREEN
import dartzee.logging.KEY_CURRENT_SCREEN
import dartzee.logging.LoggingCode
import dartzee.main.exitApplication
import dartzee.`object`.DartsClient
import dartzee.theme.Themes
import dartzee.theme.applyCurrentTheme
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.DevUtilities
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.gameLauncher
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.ResourceCache
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Image
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.util.UUID
import javax.swing.AbstractAction
import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.KeyStroke

private const val CMD_PURGE_GAME = "purge "
private const val CMD_LOAD_GAME = "load "
private const val CMD_CLEAR_CONSOLE = "cls"
private const val CMD_EMPTY_SCREEN_CACHE = "emptyscr"
private const val CMD_GUID = "guid"
private const val CMD_TEST = "test"
private const val CMD_VANILLA = "vanilla"
private const val CMD_HALLOWEEN = "halloween"

val APP_SIZE = Dimension(1000, 700)

class DartsApp(commandBar: CheatBar) : AbstractDevScreen(commandBar), WindowListener {
    override val windowName = "Main Window"
    var currentScreen: EmbeddedScreen = ScreenCache.get<MenuScreen>()

    init {
        title = "Darts"
        size = APP_SIZE
        minimumSize = APP_SIZE
        setLocationRelativeTo(null)
        contentPane.layout = BorderLayout(0, 0)

        contentPane.add(commandBar, BorderLayout.SOUTH)
        defaultCloseOperation = DO_NOTHING_ON_CLOSE

        addWindowListener(this)
    }

    fun init() {
        setIcon()

        ResourceCache.initialiseResources()

        DartsDatabaseUtil.initialiseDatabase(mainDatabase)

        InjectedThings.esDestination.readOldLogs()

        addConsoleShortcut()
        switchScreen(ScreenCache.get<MenuScreen>())

        // Pop up the change log if we've just updated
        if (DartsClient.justUpdated) {
            convertEmptyAchievements()

            val dialog = ChangeLog()
            dialog.isVisible = true
        }
    }

    private fun setIcon() {
        val imageStr = "dartzee"

        // Load the four images corresponding to 16px, 32px, 64px and 128px
        val images = ArrayList<Image>()
        var i = 16
        while (i < 256) {
            val ico = ImageIcon(javaClass.getResource("/icons/$imageStr$i.png")).image
            images.add(ico)
            i *= 2
        }

        iconImages = images
    }

    private fun addConsoleShortcut() {
        val triggerStroke = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK)
        val content = contentPane as JPanel

        val inputMap = content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        inputMap.put(triggerStroke, "showConsole")

        val actionMap = content.actionMap
        actionMap.put(
            "showConsole",
            object : AbstractAction() {
                override fun actionPerformed(e: ActionEvent) {
                    val loggingDialog = InjectedThings.loggingConsole
                    loggingDialog.isVisible = true
                    loggingDialog.toFront()
                }
            },
        )
    }

    fun switchScreen(scrn: EmbeddedScreen, reInit: Boolean = true) {
        try {
            if (reInit) {
                scrn.initialise()
            }
        } catch (t: Throwable) {
            logger.error(CODE_SCREEN_LOAD_ERROR, "Failed to load screen ${scrn.getScreenName()}", t)
            DialogUtil.showError("Error loading screen - " + scrn.getScreenName())
            return
        }

        logger.info(CODE_SWITCHED_SCREEN, "Switched to screen ${scrn.getScreenName()}")

        contentPane.remove(this.currentScreen)

        this.currentScreen = scrn
        contentPane.add(scrn, BorderLayout.CENTER)

        val screenName = scrn.getScreenName()
        title = "Darts - $screenName"

        logger.addToContext(KEY_CURRENT_SCREEN, scrn.getScreenName())

        // Need repaint() in case we don't resize.
        pack()
        repaint()

        scrn.postInit()
    }

    /** CheatListener */
    override fun commandsEnabled() = DartsClient.devMode

    override fun processCommand(cmd: String): String {
        var textToShow = ""
        if (cmd.startsWith(CMD_PURGE_GAME)) {
            val gameIdentifier = cmd.substring(CMD_PURGE_GAME.length)
            val gameId = Integer.parseInt(gameIdentifier)
            DevUtilities.purgeGame(gameId.toLong())
        } else if (cmd.startsWith(CMD_LOAD_GAME)) {
            val gameIdentifier = cmd.substring(CMD_LOAD_GAME.length)
            val localId = gameIdentifier.toLong()
            val gameId = GameEntity.getGameId(localId)
            gameId?.let { gameLauncher.loadAndDisplayGame(gameId) }
        } else if (cmd == CMD_CLEAR_CONSOLE) {
            InjectedThings.loggingConsole.clear()
        } else if (cmd == "dim") {
            println("Current screen size: $size")
        } else if (cmd == CMD_EMPTY_SCREEN_CACHE) {
            ScreenCache.emptyCache()
        } else if (cmd == CMD_GUID) {
            textToShow = UUID.randomUUID().toString()
        } else if (cmd == "stacktrace") {
            logger.error(LoggingCode("test"), "Testing stack trace")
        } else if (cmd == CMD_TEST) {
            val window = TestWindow()
            window.isVisible = true
        } else if (cmd == CMD_HALLOWEEN) {
            InjectedThings.theme = Themes.HALLOWEEN
            applyCurrentTheme()
        } else if (cmd == CMD_VANILLA) {
            InjectedThings.theme = null
            applyCurrentTheme()
        }

        return textToShow
    }

    override fun windowActivated(arg0: WindowEvent) {}

    override fun windowClosed(arg0: WindowEvent) {}

    override fun windowDeactivated(arg0: WindowEvent) {}

    override fun windowDeiconified(arg0: WindowEvent) {}

    override fun windowIconified(arg0: WindowEvent) {}

    override fun windowOpened(arg0: WindowEvent) {}

    override fun windowClosing(arg0: WindowEvent) {
        exitApplication()
    }
}
