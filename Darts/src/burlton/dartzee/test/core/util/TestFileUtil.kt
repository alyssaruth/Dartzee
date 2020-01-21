package burlton.dartzee.test.core.util

import burlton.dartzee.code.core.util.FileUtil
import burlton.dartzee.test.core.helper.AbstractTest
import burlton.dartzee.test.core.helper.exceptionLogged
import burlton.dartzee.test.core.helper.getLogs
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Dimension
import java.io.File
import javax.swing.ImageIcon

class TestFileUtil: AbstractTest()
{
    @Test
    fun `Should successfully delete a file`()
    {
        val f = File("Test.txt")
        f.createNewFile()
        f.exists() shouldBe true

        val result = FileUtil.deleteFileIfExists("Test.txt")
        result shouldBe true
        f.exists() shouldBe false
    }

    @Test
    fun `Should handle a non-existent file`()
    {
        val result = FileUtil.deleteFileIfExists("DoesNotExist.txt")
        result shouldBe false
    }

    @Test
    fun `Should stack trace and return false if the deletion fails`()
    {
        val f = File("Test.txt")
        f.createNewFile()
        f.setReadOnly()

        FileUtil.deleteFileIfExists("Test.txt") shouldBe false
        exceptionLogged() shouldBe true
        getLogs().shouldContain("Failed to delete file")
        getLogs().shouldContain("AccessDeniedException")

        //Tidy up
        f.setWritable(true)
        f.delete()
    }

    @Test
    fun `Should swap in a file successfully`()
    {
        val current = File("Current.txt")
        current.createNewFile()
        current.writeText("Current")

        val new = File("New.txt")
        new.createNewFile()
        new.writeText("New")

        FileUtil.swapInFile(current.path, new.path)

        new.exists() shouldBe false
        current.exists() shouldBe true
        current.readLines().shouldContainExactly("New")

        //Tidy up
        current.delete()
    }

    @Test
    fun `Should swap in a directory successfully`()
    {
        File("Current").mkdir()
        File("New").mkdir()

        val current = File("Current/File.txt")
        current.createNewFile()
        current.writeText("Current")

        val new = File("New/File.txt")
        new.createNewFile()
        new.writeText("New")

        FileUtil.swapInFile("Current", "New")

        new.exists() shouldBe false
        current.exists() shouldBe true
        current.readLines().shouldContainExactly("New")

        //Tidy up
        File("Current").deleteRecursively()
    }

    @Test
    fun `Should read image dimensions correctly`()
    {
        val bean = javaClass.getResource("/Bean.png")
        val statsIcon = javaClass.getResource("/stats_large.png")

        FileUtil.getImageDim(bean.path) shouldBe Dimension(150, 150)
        FileUtil.getImageDim(statsIcon.path) shouldBe Dimension(48, 48)
    }

    @Test
    fun `Should extract correct bytes`()
    {
        val bytes = FileUtil.getByteArrayForResource("/Bean.png")

        val ii = ImageIcon(bytes)
        ii.iconWidth shouldBe 150
        ii.iconHeight shouldBe 150
    }
}