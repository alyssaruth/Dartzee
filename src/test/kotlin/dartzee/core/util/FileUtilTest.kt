package dartzee.core.util

import dartzee.core.util.FileUtil.renameToWithRetries
import dartzee.helper.AbstractTest
import dartzee.logging.CODE_FILE_ERROR
import dartzee.logging.Severity
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import java.awt.Dimension
import java.io.File
import java.nio.file.DirectoryNotEmptyException
import javax.swing.ImageIcon
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FileUtilTest : AbstractTest() {
    private val testDirectory = File("Test/")

    @BeforeEach
    fun beforeEach() {
        testDirectory.deleteRecursively()
        testDirectory.mkdirs()
    }

    @AfterEach
    fun afterEach() {
        testDirectory.deleteRecursively()
    }

    @Test
    fun `Should successfully delete a file`() {
        val f = File("Test/Test.txt")
        f.createNewFile()
        f.exists() shouldBe true

        val result = FileUtil.deleteFileIfExists("Test/Test.txt")
        result shouldBe true
        f.exists() shouldBe false
    }

    @Test
    fun `Should handle a non-existent file`() {
        val result = FileUtil.deleteFileIfExists("DoesNotExist.txt")
        result shouldBe false
    }

    @Test
    fun `Should stack trace and return false if the deletion fails`() {
        val f = File("Test/File.txt")
        f.createNewFile()

        FileUtil.deleteFileIfExists("Test") shouldBe false

        val log = verifyLog(CODE_FILE_ERROR, Severity.ERROR)
        log.message shouldBe "Failed to delete file Test"
        log.errorObject?.shouldBeInstanceOf<DirectoryNotEmptyException>()
    }

    @Test
    fun `Should swap in a file successfully`() {
        val current = File("Test/Current.txt")
        current.createNewFile()
        current.writeText("Current")

        val new = File("Test/New.txt")
        new.createNewFile()
        new.writeText("New")

        FileUtil.swapInFile(current.path, new.path) shouldBe null

        new.exists() shouldBe false
        current.exists() shouldBe true
        current.readLines().shouldContainExactly("New")
    }

    @Test
    fun `Should swap in a directory successfully`() {
        File("Test/Current").mkdir()
        File("Test/New").mkdir()

        val current = File("Test/Current/File.txt")
        current.createNewFile()
        current.writeText("Current")

        val new = File("Test/New/File.txt")
        new.createNewFile()
        new.writeText("New")

        FileUtil.swapInFile("Test/Current", "Test/New") shouldBe null

        new.exists() shouldBe false
        current.exists() shouldBe true
        current.readLines().shouldContainExactly("New")
    }

    @Test
    fun `Should pre-clean for when a move has previously failed`() {
        val zzDir = File("Test/zzCurrent")
        zzDir.mkdir()

        val inTheWay = File("Test/zzCurrent/Bad.txt")
        inTheWay.createNewFile()
        inTheWay.writeText("muahaha")

        val currentDir = File("Test/Current")
        currentDir.mkdir()
        val newDir = File("Test/New")
        newDir.mkdir()

        val current = File("Test/Current/File.txt")
        current.createNewFile()
        current.writeText("Current")

        val new = File("Test/New/File.txt")
        new.createNewFile()
        new.writeText("New")

        FileUtil.swapInFile("Test/Current", "Test/New") shouldBe null

        zzDir.shouldNotExist()
        inTheWay.shouldNotExist()
        newDir.shouldNotExist()
        new.shouldNotExist()

        currentDir.shouldExist()
        current.shouldExist()
        current.readLines().shouldContainExactly("New")
    }

    @Test
    fun `Rename should fail for non-existent file`() {
        val f = File("Test/Test.txt")

        f.renameToWithRetries(File("Test/New.txt")) shouldBe false

        val error = verifyLog(CODE_FILE_ERROR, Severity.ERROR)
        error.message shouldBe
            "Trying to rename [Test/Test.txt] to [Test/New.txt] but it does not exist"
    }

    @Test
    fun `Rename should fail if destination already exists`() {
        val f = File("Test/Test.txt")
        f.createNewFile()
        val new = File("Test/New.txt")
        new.createNewFile()

        f.renameToWithRetries(new) shouldBe false

        val error = verifyLog(CODE_FILE_ERROR, Severity.ERROR)
        error.message shouldBe
            "Trying to rename [Test/Test.txt] to [Test/New.txt] but [Test/New.txt] already exists"
    }

    @Test
    fun `Rename should succeed on retry`() {
        val f = mockk<File>()
        every { f.exists() } returns true

        var counter = 0
        every { f.renameTo(any()) } answers
            {
                if (counter < 2) {
                    counter++
                    false
                } else {
                    true
                }
            }

        val new = File("Test/New.txt")
        f.renameToWithRetries(new) shouldBe true

        verifyLog(CODE_FILE_ERROR, Severity.WARN)
    }

    @Test
    fun `Rename should give up on persistent failure`() {
        val f = mockk<File>()
        every { f.exists() } returns true
        every { f.renameTo(any()) } returns false

        val new = File("Test/New.txt")

        f.renameToWithRetries(new) shouldBe false
    }

    @Test
    fun `Should read image dimensions correctly`() {
        val bean = javaClass.getResource("/Bean.png")!!.toURI()
        val statsIcon = javaClass.getResource("/stats_large.png")!!.toURI()

        FileUtil.getImageDim(File(bean)) shouldBe Dimension(150, 150)
        FileUtil.getImageDim(File(statsIcon)) shouldBe Dimension(48, 48)
    }

    @Test
    fun `Should extract correct bytes`() {
        val bytes = FileUtil.getByteArrayForResource("/Bean.png")

        val ii = ImageIcon(bytes)
        ii.iconWidth shouldBe 150
        ii.iconHeight shouldBe 150
    }
}
