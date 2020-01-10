package burlton.core.code.util

import java.awt.Component
import java.awt.Dimension
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream
import javax.swing.JFileChooser

object FileUtil
{
    fun deleteFileIfExists(filePath: String) =
        try {
            val path = Paths.get(filePath)
            Files.deleteIfExists(path)
        } catch (t: Throwable) {
            Debug.stackTrace(t, "Failed to delete file")
            false
        }

    fun swapInFile(oldFilePath: String, newFilePath: String): String?
    {
        val oldFile = File(oldFilePath)
        val oldFileName = oldFile.name
        val newFile = File(newFilePath)
        val zzOldFile = File(oldFile.parent, "zz$oldFileName")
        if (oldFile.exists()
            && !oldFile.renameTo(zzOldFile)
        )
        {
            return "Failed to rename old out of the way."
        }

        if (!newFile.renameTo(File(oldFile.parent, oldFileName)))
        {
            return "Failed to rename new file to $oldFileName"
        }

        if (zzOldFile.isFile)
        {
            if (!deleteFileIfExists(zzOldFile.path)) return "Failed to delete zz'd old file: ${zzOldFile.path}"
        }
        else
        {
            if (!deleteDirectoryIfExists(zzOldFile)) return "Failed to delete zz'd old directory: ${zzOldFile.path}"
        }

        return null
    }

    fun getImageDim(path: String): Dimension?
    {
        val suffix = getFileSuffix(path)
        val iter = ImageIO.getImageReadersBySuffix(suffix)
        val reader = if (iter.hasNext()) iter.next() else null
        if (reader != null)
        {
            try
            {
                FileImageInputStream(File(path)).use { stream ->
                    reader.input = stream
                    val width = reader.getWidth(reader.minIndex)
                    val height = reader.getHeight(reader.minIndex)
                    return Dimension(width, height)
                }
            }
            catch (e: IOException) { Debug.stackTrace(e) }
            finally { reader.dispose() }
        }
        else
        {
            Debug.stackTrace("No reader found for file extension: $suffix (full path: $path)")
        }

        return null
    }

    private fun getFileSuffix(path: String?): String
    {
        if (path == null
            || path.lastIndexOf('.') == -1
        ) {
            return ""
        }

        val dotIndex = path.lastIndexOf('.')
        return path.substring(dotIndex + 1)
    }

    fun getByteArrayForResource(resourcePath: String): ByteArray? =
        try {
            javaClass.getResourceAsStream(resourcePath).use { `is` ->
                ByteArrayOutputStream().use { baos ->
                    val b = ByteArray(4096)
                    var n: Int
                    while (`is`.read(b).also { n = it } != -1) {
                        baos.write(b, 0, n)
                    }
                    baos.toByteArray()
                }
            }
        } catch (ioe: IOException) {
            Debug.stackTrace(ioe, "Failed to read classpath resource: $resourcePath")
            null
        }

    /**
     * Delete a whole directory, recursively clearing out the files/subfolders too.
     */
    fun deleteDirectoryIfExists(dir: File): Boolean
    {
        if (!dir.exists() || !dir.isDirectory) return true

        val files = dir.listFiles()
        files?.forEach {
            val success = if (it.isDirectory) deleteDirectoryIfExists(it) else it.delete()
            if (!success) {
                return false
            }
        }

        return dir.delete()
    }

    /**
     * Copy directory A to a new directory, B. Copies all subfolders and files.
     */
    fun copyDirectoryRecursively(dirFrom: File, dirToCreate: String): Boolean
    {
        if (!copyFile(dirFrom, dirToCreate)) return false

        val files = dirFrom.listFiles()
        files?.forEach {
            val dirTo = "dirToCreate\\${it.name}"
            val success = if (it.isDirectory) copyDirectoryRecursively(it, dirTo) else copyFile(it, dirTo)
            if (!success) return false
        }

        return true
    }

    private fun copyFile(fileFrom: File, destinationFile: String): Boolean {
        try {
            Files.copy(fileFrom.toPath(), Paths.get(destinationFile))
        } catch (ioe: IOException) {
            Debug.append("Caught $ioe copying $fileFrom to $destinationFile")
            Debug.stackTraceSilently(ioe)
            return false
        }

        return true
    }

    /**
     * FileChooser
     */
    fun chooseDirectory(comp: Component?): File?
    {
        val fc = JFileChooser()
        fc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        val option = fc.showDialog(comp, "Select")
        if (option != JFileChooser.APPROVE_OPTION)
        {
            Debug.append("Cancelled directory selection")
            return null
        }

        return fc.selectedFile
    }
}