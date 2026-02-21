package dartzee.core.bean

import java.io.File

interface IFileUploadListener {
    fun fileUploaded(file: File): Boolean
}
