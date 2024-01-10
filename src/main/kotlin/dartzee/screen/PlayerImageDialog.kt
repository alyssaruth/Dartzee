package dartzee.screen

import dartzee.bean.PlayerImageRadio
import dartzee.core.bean.FileUploader
import dartzee.core.bean.IFileUploadListener
import dartzee.core.bean.WrapLayout
import dartzee.core.bean.scrollToBottom
import dartzee.core.screen.SimpleDialog
import dartzee.core.util.DialogUtil
import dartzee.core.util.FileUtil
import dartzee.core.util.getAllChildComponentsForType
import dartzee.db.PlayerImageEntity
import dartzee.utils.InjectedThings
import dartzee.utils.PLAYER_IMAGE_HEIGHT
import dartzee.utils.PLAYER_IMAGE_WIDTH
import dartzee.utils.convertImageToAvatarDimensions
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import javax.swing.UIManager
import javax.swing.border.TitledBorder
import javax.swing.filechooser.FileNameExtensionFilter

class PlayerImageDialog(private val imageSelectedCallback: (String) -> Unit) :
    SimpleDialog(), IFileUploadListener {
    private val tabbedPane = JTabbedPane(SwingConstants.TOP)
    private val panelPreset = JPanel()
    private val panelUpload = JPanel()
    private val panelPresets = JPanel()
    private val panelPreviouslyUploaded = JPanel()
    private val filter = FileNameExtensionFilter("Image files", *ImageIO.getReaderFileSuffixes())
    private val fs = FileUploader(filter)
    private val bgUploaded = ButtonGroup()
    private val scrollPaneUploaded = JScrollPane()

    init {
        setSize(650, 400)
        setLocationRelativeTo(null)
        isModal = InjectedThings.allowModalDialogs
        title = "Select Avatar"

        contentPane.add(tabbedPane, BorderLayout.CENTER)
        tabbedPane.addTab("Presets", null, panelPreset, null)
        panelPreset.layout = BorderLayout(0, 0)
        val scrollPanePresets = JScrollPane()
        panelPreset.add(scrollPanePresets)
        panelPreset.name = "presetTab"
        scrollPanePresets.setViewportView(panelPresets)
        scrollPanePresets.verticalScrollBar.unitIncrement = 16
        panelPresets.layout = WrapLayout()
        tabbedPane.addTab("Upload", null, panelUpload, null)
        panelUpload.layout = BorderLayout(0, 0)
        panelUpload.name = "uploadTab"
        val panelUploadOptions = JPanel()
        panelUpload.add(panelUploadOptions, BorderLayout.NORTH)
        scrollPaneUploaded.verticalScrollBar.unitIncrement = 16
        scrollPaneUploaded.border =
            TitledBorder(
                UIManager.getBorder("TitledBorder.border"),
                "Previously Uploaded",
                TitledBorder.LEADING,
                TitledBorder.TOP,
                null,
                Color(0, 0, 0)
            )
        panelUpload.add(scrollPaneUploaded, BorderLayout.CENTER)
        scrollPaneUploaded.setViewportView(panelPreviouslyUploaded)
        panelPreviouslyUploaded.layout = WrapLayout()
        panelUploadOptions.layout = BorderLayout(0, 0)
        panelUploadOptions.add(fs)

        fs.addFileUploadListener(this)
        init()
    }

    private fun init() {
        val entities = PlayerImageEntity().retrieveEntities()
        populatePanel(panelPresets, entities.filter { it.preset }, ButtonGroup())
        populatePanel(panelPreviouslyUploaded, entities.filter { !it.preset }, bgUploaded)
    }

    private fun populatePanel(panel: JPanel, entities: List<PlayerImageEntity>, bg: ButtonGroup) {
        entities.forEach { img ->
            val radio = PlayerImageRadio(img)
            panel.add(radio)
            radio.addToButtonGroup(bg)
        }
    }

    private fun getPlayerImageIdFromSelection(): String? {
        val panel = tabbedPane.selectedComponent as JPanel

        val radios = panel.getAllChildComponentsForType<PlayerImageRadio>()
        return radios.find { it.isSelected() }?.playerImageId
    }

    override fun okPressed() {
        val playerImageId = getPlayerImageIdFromSelection()
        if (playerImageId == null) {
            DialogUtil.showErrorOLD("You must select an image.")
            return
        }

        imageSelectedCallback(playerImageId)
        dispose()
    }

    private fun validateFile(file: File): Boolean {
        val imageReaders = ImageIO.getImageReadersBySuffix(file.extension)
        if (!imageReaders.hasNext()) {
            DialogUtil.showErrorOLD("You must select a valid image file.")
            return false
        }

        val imgDim = FileUtil.getImageDim(file) ?: Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        if (imgDim.getWidth() < PLAYER_IMAGE_WIDTH || imgDim.getHeight() < PLAYER_IMAGE_HEIGHT) {
            DialogUtil.showErrorOLD(
                "The image is too small - it must be at least $PLAYER_IMAGE_WIDTH x $PLAYER_IMAGE_HEIGHT px."
            )
            return false
        }

        return true
    }

    override fun fileUploaded(file: File): Boolean {
        if (!validateFile(file)) {
            return false
        }

        val scaled = convertImageToAvatarDimensions(file.readBytes())

        val pi = PlayerImageEntity.factoryAndSave(file.absolutePath, scaled, false)
        val rdbtn = PlayerImageRadio(pi!!)

        panelPreviouslyUploaded.add(rdbtn)
        rdbtn.addToButtonGroup(bgUploaded)
        repaint()

        scrollPaneUploaded.scrollToBottom()
        return true
    }
}
