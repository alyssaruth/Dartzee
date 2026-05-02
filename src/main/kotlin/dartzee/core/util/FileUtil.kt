package dartzee.core.util

import dartzee.logging.CODE_FILE_ERROR
import dartzee.logging.CODE_SWITCHING_FILES
import dartzee.utils.InjectedThings.logger
import java.awt.Dimension
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageInputStream

object FileUtil {
    fun deleteFileIfExists(filePath: String) =
        try {
            val path = Paths.get(filePath)
            Files.deleteIfExists(path)
        } catch (t: Throwable) {
            logger.error(CODE_FILE_ERROR, "Failed to delete file $filePath", t)
            false
        }

    fun swapInFile(oldFilePath: String, newFilePath: String): String? {
        val oldFile = File(oldFilePath)
        val oldFileName = oldFile.name
        val newFile = File(newFilePath)
        val zzOldFile = File(oldFile.parent, "zz$oldFileName")

        logger.info(CODE_SWITCHING_FILES, "Pre-delete [$zzOldFile] in case it exists already")
        if (!zzOldFile.deleteRecursively()) {
            return "Failed to tidy up before move."
        }

        logger.info(CODE_SWITCHING_FILES, "Rename current out of the way [$oldFile -> $zzOldFile]")
        if (oldFile.exists() && !oldFile.renameToWithRetries(zzOldFile)) {
            return "Failed to rename old out of the way."
        }

        logger.info(CODE_SWITCHING_FILES, "Rename new to current [$newFile -> $oldFile]")
        if (!newFile.renameToWithRetries(File(oldFile.parent, oldFileName))) {
            return "Failed to rename new file to $oldFileName"
        }

        logger.info(CODE_SWITCHING_FILES, "Delete zz'd file [$zzOldFile]")
        if (!zzOldFile.deleteRecursively()) {
            return "Failed to delete zz'd old file: ${zzOldFile.path}"
        }

        return null
    }

    tailrec fun File.renameToWithRetries(newFile: File, attempt: Int = 1): Boolean {
        if (!exists()) {
            logger.error(
                CODE_FILE_ERROR,
                "Trying to rename [$this] to [$newFile] but it does not exist",
            )
            return false
        }

        if (newFile.exists()) {
            logger.error(
                CODE_FILE_ERROR,
                "Trying to rename [$this] to [$newFile] but [$newFile] already exists",
            )
            return false
        }

        val success = renameTo(newFile)
        return if (success) {
            true
        } else if (attempt == 5) {
            false
        } else {
            logger.warn(
                CODE_FILE_ERROR,
                "Failed to rename [$this] to [$newFile] on attempt $attempt, will retry.",
            )
            Thread.sleep(500)
            renameToWithRetries(newFile, attempt + 1)
        }
    }

    fun getImageDim(file: File): Dimension? {
        val iter = ImageIO.getImageReadersBySuffix(file.extension)
        val reader = if (iter.hasNext()) iter.next() else null
        if (reader != null) {
            try {
                FileImageInputStream(file).use { stream ->
                    reader.input = stream
                    val width = reader.getWidth(reader.minIndex)
                    val height = reader.getHeight(reader.minIndex)
                    return Dimension(width, height)
                }
            } catch (e: IOException) {
                logger.error(CODE_FILE_ERROR, "Failed to get img dimensions for $file", e)
            } finally {
                reader.dispose()
            }
        } else {
            logger.error(
                CODE_FILE_ERROR,
                "No reader found for file extension: ${file.extension} (full path: ${file.absolutePath})",
            )
        }

        return null
    }

    fun getByteArrayForResource(resourcePath: String): ByteArray? =
        try {
            javaClass.getResourceAsStream(resourcePath).use { stream -> stream?.readBytes() }
        } catch (ioe: IOException) {
            logger.error(CODE_FILE_ERROR, "Failed to read classpath resource $resourcePath", ioe)
            null
        }
}
