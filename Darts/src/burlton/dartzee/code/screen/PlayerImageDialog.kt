package burlton.dartzee.code.screen

import burlton.core.code.util.FileUtil
import burlton.dartzee.code.bean.PlayerImageRadio
import burlton.dartzee.code.db.PlayerImageEntity
import burlton.desktopcore.code.bean.FileUploadListener
import burlton.desktopcore.code.bean.FileUploader
import burlton.desktopcore.code.bean.WrapLayout
import burlton.desktopcore.code.screen.SimpleDialog
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.getAllChildComponentsForType
import java.awt.BorderLayout
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.filechooser.FileNameExtensionFilter

class PlayerImageDialog : SimpleDialog(), FileUploadListener
{
    var playerImageIdSelected = ""

    private val tabbedPane = JTabbedPane(SwingConstants.TOP)
    private val panelPreset = JPanel()
    private val panelUpload = JPanel()
    private val panelPresets = JPanel()
    private val panelPreviouslyUploaded = JPanel()
    private val filter = FileNameExtensionFilter("Image files", *ImageIO.getReaderFileSuffixes())
    private val fs = FileUploader(filter)
    private val bgUploaded = ButtonGroup()

    init
    {
        setSize(650, 400)
        setLocationRelativeTo(null)
        isModal = true
        title = "Select Avatar"

        contentPane.add(tabbedPane, BorderLayout.CENTER)
        tabbedPane.addTab("Presets", null, panelPreset, null)
        panelPreset.layout = BorderLayout(0, 0)
        val scrollPanePresets = JScrollPane()
        panelPreset.add(scrollPanePresets)
        scrollPanePresets.setViewportView(panelPresets)
        scrollPanePresets.verticalScrollBar.unitIncrement = 16
        panelPresets.layout = WrapLayout()
        tabbedPane.addTab("Upload", null, panelUpload, null)
        panelUpload.layout = BorderLayout(0, 0)
        val panelUploadOptions = JPanel()
        panelUpload.add(panelUploadOptions, BorderLayout.NORTH)
        val scrollPaneUploaded = JScrollPane()
        scrollPaneUploaded.verticalScrollBar.unitIncrement = 16
        scrollPaneUploaded.border = TitledBorder(UIManager.getBorder("TitledBorder.border"), "Previously Uploaded", TitledBorder.LEADING, TitledBorder.TOP, null, Color(0, 0, 0))
        panelUpload.add(scrollPaneUploaded, BorderLayout.CENTER)
        scrollPaneUploaded.setViewportView(panelPreviouslyUploaded)
        panelPreviouslyUploaded.layout = WrapLayout()
        panelUploadOptions.layout = BorderLayout(0, 0)
        panelUploadOptions.add(fs)

        fs.addFileUploadListener(this)

        init()
    }

    private fun init()
    {
        val entities = PlayerImageEntity().retrieveEntities()
        populatePanel(panelPresets, entities.filter{ it.preset }, ButtonGroup())
        populatePanel(panelPreviouslyUploaded, entities.filter{ !it.preset }, bgUploaded)
    }

    private fun populatePanel(panel: JPanel, entities: List<PlayerImageEntity>, bg: ButtonGroup)
    {
        entities.forEach{
            val radio = PlayerImageRadio(it)
            panel.add(radio)
            radio.addToButtonGroup(bg)
        }
    }

    private fun validateAndUploadImage(imgFile: File)
    {
        val imgDim = FileUtil.getImageDim(imgFile.absolutePath)
        if (imgDim!!.getWidth() > 150 || imgDim.getHeight() > 150)
        {
            DialogUtil.showError("The image must be no larger than 150x150px.")
            return
        }

        val pi = PlayerImageEntity.factoryAndSave(imgFile, false)
        val rdbtn = PlayerImageRadio(pi!!)

        panelPreviouslyUploaded.add(rdbtn)
        rdbtn.addToButtonGroup(bgUploaded)

        repaint()
    }

    private fun getPlayerImageIdFromSelection(): String?
    {
        val panel = tabbedPane.selectedComponent as JPanel

        val radios = getAllChildComponentsForType(panel, PlayerImageRadio::class.java)
        return radios.find { it.isSelected() } ?.playerImageId
    }

    override fun okPressed()
    {
        val playerImageId = getPlayerImageIdFromSelection()
        if (playerImageId == null)
        {
            DialogUtil.showError("You must select an image.")
            return
        }

        playerImageIdSelected = playerImageId
        dispose()
    }

    override fun fileUploaded(file: File)
    {
        validateAndUploadImage(file)
    }
}
