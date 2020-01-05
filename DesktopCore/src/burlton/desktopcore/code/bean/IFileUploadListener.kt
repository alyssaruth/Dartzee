package burlton.desktopcore.code.bean

import java.io.File

interface IFileUploadListener
{
    fun fileUploaded(file: File)
}
