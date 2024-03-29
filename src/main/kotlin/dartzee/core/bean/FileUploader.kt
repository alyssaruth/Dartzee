package dartzee.core.bean

import dartzee.core.util.DialogUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.border.EmptyBorder
import javax.swing.filechooser.FileFilter

class FileUploader(ff: FileFilter) : JPanel(), ActionListener {
    private var selectedFile: File? = null
    private val listeners = mutableListOf<IFileUploadListener>()

    private val textField = JTextField("")
    private val btnSelectFile = JButton("...")
    private val fc = JFileChooser()
    private val btnUpload = JButton("Upload")

    init {
        layout = BorderLayout(0, 0)

        val filters = fc.choosableFileFilters
        fc.removeChoosableFileFilter(filters[0])
        fc.addChoosableFileFilter(ff)
        val panel = JPanel()
        add(panel, BorderLayout.CENTER)
        panel.layout = BorderLayout(0, 0)
        panel.add(textField, BorderLayout.CENTER)
        textField.text = ""
        textField.isEditable = false
        panel.add(btnSelectFile, BorderLayout.EAST)
        btnSelectFile.preferredSize = Dimension(25, 20)
        val panelEast = JPanel()
        panelEast.border = EmptyBorder(0, 5, 0, 5)
        add(panelEast, BorderLayout.EAST)
        panelEast.layout = BorderLayout(0, 0)
        panelEast.add(btnUpload, BorderLayout.CENTER)

        btnSelectFile.addActionListener(this)
        btnUpload.addActionListener(this)
    }

    fun addFileUploadListener(listener: IFileUploadListener) {
        listeners.add(listener)
    }

    private fun selectFile() {
        val returnVal = fc.showOpenDialog(this)
        val selectedFile = fc.selectedFile
        if (returnVal == JFileChooser.APPROVE_OPTION && selectedFile != null) {
            this.selectedFile = fc.selectedFile
            textField.text = selectedFile.path
        }
    }

    private fun uploadPressed() {
        val file = selectedFile
        if (file == null) {
            DialogUtil.showErrorOLD("You must select a file to upload.")
            return
        }

        val success = listeners.all { it.fileUploaded(file) }
        if (success) {
            this.selectedFile = null
            textField.text = ""
        }
    }

    override fun actionPerformed(arg0: ActionEvent) {
        when (arg0.source) {
            btnSelectFile -> selectFile()
            btnUpload -> uploadPressed()
        }
    }
}
