package burlton.dartzee.code.screen

import burlton.core.code.util.Debug
import burlton.dartzee.code.`object`.DartsClient
import burlton.dartzee.code.achievements.convertEmptyAchievements
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.sanity.DatabaseSanityCheck
import burlton.dartzee.code.screen.game.DartsGameScreen
import burlton.dartzee.code.utils.DartsDatabaseUtil
import burlton.dartzee.code.utils.DevUtilities
import burlton.dartzee.code.utils.ResourceCache
import burlton.dartzee.test.helper.randomGuid
import burlton.desktopcore.code.bean.AbstractDevScreen
import burlton.desktopcore.code.bean.CheatBar
import burlton.desktopcore.code.util.DialogUtil
import com.mashape.unirest.http.Unirest
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Image
import java.awt.event.*
import java.util.*
import javax.swing.*

private const val CMD_PURGE_GAME = "purge "
private const val CMD_LOAD_GAME = "load "
private const val CMD_CLEAR_CONSOLE = "cls"
private const val CMD_EMPTY_SCREEN_CACHE = "emptyscr"
private const val CMD_SANITY = "sanity"
private const val CMD_GUID = "guid"

class DartsApp(commandBar: CheatBar) : AbstractDevScreen(commandBar), WindowListener
{
    var currentScreen: EmbeddedScreen? = null
        private set

    init
    {
        title = "Darts"
        setSize(800, 600)
        setLocationRelativeTo(null)
        contentPane.layout = BorderLayout(0, 0)

        contentPane.add(commandBar, BorderLayout.SOUTH)
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE

        addWindowListener(this)
    }

    fun init()
    {
        setIcon()

        ResourceCache.initialiseResources()

        DartsDatabaseUtil.initialiseDatabase()

        addConsoleShortcut()
        switchScreen(ScreenCache.getScreen(MenuScreen::class.java))

        //Pop up the change log if we've just updated
        if (DartsClient.justUpdated)
        {
            convertEmptyAchievements()

            val dialog = ChangeLog()
            dialog.isVisible = true
        }
    }

    private fun setIcon()
    {
        val imageStr = "dartzee"

        //Load the four images corresponding to 16px, 32px, 64px and 128px
        val images = ArrayList<Image>()
        var i = 16
        while (i < 256)
        {
            val ico = ImageIcon(javaClass.getResource("/icons/$imageStr$i.png")).image
            images.add(ico)
            i *= 2
        }

        iconImages = images
    }

    private fun addConsoleShortcut()
    {
        val triggerStroke = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK)
        val content = contentPane as JPanel

        val inputMap = content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        inputMap.put(triggerStroke, "showConsole")

        val actionMap = content.actionMap
        actionMap.put("showConsole", object : AbstractAction()
        {
            override fun actionPerformed(e: ActionEvent)
            {
                val loggingDialog = ScreenCache.getDebugConsole()
                loggingDialog.isVisible = true
                loggingDialog.toFront()
            }
        })
    }

    @JvmOverloads
    fun switchScreen(scrn: EmbeddedScreen?, reInit: Boolean = true)
    {
        try
        {
            if (reInit)
            {
                scrn!!.initialise()
            }
        }
        catch (t: Throwable)
        {
            Debug.stackTraceNoError(t, "Failed to load screen " + scrn!!.getScreenName())
            DialogUtil.showError("Error loading screen - " + scrn.getScreenName())
            return
        }

        if (this.currentScreen != null)
        {
            contentPane.remove(this.currentScreen!!)
        }

        this.currentScreen = scrn
        contentPane.add(scrn!!, BorderLayout.CENTER)

        val screenName = scrn.getScreenName()
        title = "Darts - $screenName"

        val desiredSize = scrn.getDesiredSize()
        if (desiredSize != null)
        {
            size = desiredSize
            minimumSize = desiredSize
        }
        else
        {
            minimumSize = Dimension(800, 600)
            setSize(800, 600) //Revert to default
        }

        //Need pack() to ensure the dialog resizes correctly.
        //Need repaint() in case we don't resize.
        pack()
        repaint()

        scrn.postInit()
    }

    /**
     * CheatListener
     */
    override fun commandsEnabled() = DartsClient.devMode

    override fun processCommand(cmd: String): String
    {
        var textToShow = ""
        if (cmd.startsWith(CMD_PURGE_GAME))
        {
            val gameIdentifier = cmd.substring(CMD_PURGE_GAME.length)
            val gameId = Integer.parseInt(gameIdentifier)
            DevUtilities.purgeGame(gameId.toLong())
        }
        else if (cmd.startsWith(CMD_LOAD_GAME))
        {
            val gameIdentifier = cmd.substring(CMD_LOAD_GAME.length)
            val localId = gameIdentifier.toLong()
            val gameId = GameEntity.getGameId(localId)
            gameId?.let { DartsGameScreen.loadAndDisplayGame(gameId) }
        }
        else if (cmd == CMD_CLEAR_CONSOLE)
        {
            Debug.clearLogs()
        }
        else if (cmd == "dim")
        {
            Debug.append("Current screen size: $size")
        }
        else if (cmd == CMD_EMPTY_SCREEN_CACHE)
        {
            ScreenCache.emptyCache()
            Debug.append("Emptied screen cache.")
        }
        else if (cmd == CMD_SANITY)
        {
            DatabaseSanityCheck.runSanityCheck()
        }
        else if (cmd == "dartzee")
        {
            val dlg = DartzeeRuleCreationDialog()
            dlg.isVisible = true
        }
        else if (cmd == CMD_GUID)
        {
            textToShow = randomGuid()
        }
        else if (cmd == "git")
        {
            val response = Unirest.get("https://api.github.com/repos/alexburlton/DartzeeRelease/releases/latest").asJson()

            Debug.append("Response tag: " + response.body.`object`.get("tag_name"))
        }
        else if (cmd == "load")
        {
            DialogUtil.showLoadingDialog("Testing")
        }
        else if (cmd == "stacktrace")
        {
            Debug.setSendingEmails(true)
            Debug.stackTraceNoError("Testing")
            Debug.setSendingEmails(false)
        }

        return textToShow
    }

    override fun windowActivated(arg0: WindowEvent) {}
    override fun windowClosed(arg0: WindowEvent) {}
    override fun windowDeactivated(arg0: WindowEvent) {}
    override fun windowDeiconified(arg0: WindowEvent) {}
    override fun windowIconified(arg0: WindowEvent) {}
    override fun windowOpened(arg0: WindowEvent) {}

    override fun windowClosing(arg0: WindowEvent)
    {
        ScreenCache.exitApplication()
    }
}
