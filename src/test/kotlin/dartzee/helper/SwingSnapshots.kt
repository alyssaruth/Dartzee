package dartzee.helper

import dartzee.bean.SIZE
import dartzee.core.bean.getPointList
import io.kotlintest.fail
import io.kotlintest.shouldBe
import org.junit.Assume
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JComponent

private val overwrite = System.getenv("updateSnapshots") == "true"
private val osForScreenshots = (System.getenv("screenshotOs") ?: "").toLowerCase(Locale.ENGLISH)

fun JComponent.shouldMatchImage(imageName: String)
{
    val os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH)
    if (osForScreenshots.isNotEmpty())
    {
        Assume.assumeTrue("Wrong OS for screenshot tests: $os", os.contains(osForScreenshots))
    }

    val img = BufferedImage(SIZE, SIZE, BufferedImage.TYPE_4BYTE_ABGR)
    val g2 = img.createGraphics()
    paint(g2)

    val callingSite = Throwable().stackTrace[1].className
    val imgPath = "src/test/resources/__snapshots__/$callingSite"

    val file = File("$imgPath/$imageName.png")
    if (!file.exists() && !overwrite)
    {
        fail("Snapshot image not found: ${file.path}. Run with env var updateSnapshots=true to write for the first time.")
    }

    file.mkdirs()

    if (overwrite)
    {
        ImageIO.write(img, "png", file)
    }
    else
    {
        val savedImg = ImageIO.read(file)
        val match = img.isEqual(savedImg)
        if (!match)
        {
            val failedFile = File("$imgPath/$imageName.failed.png")
            ImageIO.write(img, "png", failedFile)
            fail("Snapshot image did not match: ${file.path}. Run with env var updateSnapshots=true to overwrite.")
        }
    }
}

private fun BufferedImage.isEqual(other: BufferedImage): Boolean
{
    if (width != other.width || height != other.height) return false
    return getPointList(width, height).all { getRGB(it.x, it.y) == other.getRGB(it.x, it.y) }
}

fun ImageIcon.shouldMatch(other: ImageIcon)
{
    toBufferedImage().isEqual(other.toBufferedImage()) shouldBe true
}
private fun ImageIcon.toBufferedImage(): BufferedImage
{
    val bi = BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_RGB)
    val g = bi.createGraphics()
    paintIcon(null, g, 0, 0)
    g.dispose()
    return bi
}
