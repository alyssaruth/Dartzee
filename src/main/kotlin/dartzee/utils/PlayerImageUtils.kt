package dartzee.utils

import dartzee.core.bean.paint
import dartzee.core.bean.toBufferedImage
import dartzee.db.PlayerEntity
import dartzee.db.PlayerImageEntity
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import kotlin.math.abs
import kotlin.math.pow

const val PLAYER_IMAGE_WIDTH = 150
const val PLAYER_IMAGE_HEIGHT = 150

fun splitAvatar(
    playerOne: PlayerEntity,
    playerTwo: PlayerEntity,
    selectedPlayer: PlayerEntity?,
    gameFinished: Boolean
): ImageIcon {
    val firstImg = getAvatarImage(playerOne.playerImageId)
    val secondImg = getAvatarImage(playerTwo.playerImageId)

    val selectedImg =
        when (selectedPlayer) {
            playerOne -> firstImg
            playerTwo -> secondImg
            else -> null
        }

    return splitAvatar(firstImg, secondImg, selectedImg, gameFinished)
}

fun splitAvatar(
    firstImg: BufferedImage,
    secondImg: BufferedImage,
    selectedImg: BufferedImage?,
    gameFinished: Boolean
): ImageIcon {
    val diagonalOffset =
        if (gameFinished) 1.0
        else
            when (selectedImg) {
                firstImg -> 1.4
                secondImg -> 0.6
                else -> 1.0
            }

    val diagonalCenter = (firstImg.width * diagonalOffset).toInt()
    val diagonalThickness = if (selectedImg != null) 1 else 0

    val newImage = BufferedImage(firstImg.width, firstImg.height, BufferedImage.TYPE_INT_ARGB)
    newImage.paint { pt ->
        val manhattan = pt.x + pt.y
        if (abs(manhattan - diagonalCenter) <= diagonalThickness) {
            Color.BLACK
        } else {
            val imgForSection = if (manhattan < diagonalCenter) firstImg else secondImg
            val rgb = imgForSection.getRGB(pt.x, pt.y)
            val rgbToUse = if (selectedImg == imgForSection || gameFinished) rgb else greyscale(rgb)
            Color(rgbToUse)
        }
    }

    return ImageIcon(newImage)
}

fun getAvatarImage(imageId: String) =
    PlayerImageEntity.retrieveImageIconForId(imageId).toBufferedImage(BufferedImage.TYPE_INT_RGB)

fun getImageForTableRow(imageId: String) = ImageIcon(getRawImageForTableRow(imageId))

fun getRawImageForTableRow(imageId: String) = getAvatarImage(imageId).resize(50, 50)

fun combinePlayerFlags(flagOne: ImageIcon, flagTwo: ImageIcon): ImageIcon {
    val imageOne = flagOne.toBufferedImage(BufferedImage.TYPE_INT_ARGB)
    val imageTwo = flagTwo.toBufferedImage(BufferedImage.TYPE_INT_ARGB)
    val newImage =
        BufferedImage(
            flagOne.iconWidth + flagTwo.iconWidth,
            flagOne.iconHeight,
            BufferedImage.TYPE_INT_ARGB
        )

    newImage.paint { pt ->
        if (pt.x < flagOne.iconWidth) {
            Color(imageOne.getRGB(pt.x, pt.y), true)
        } else {
            Color(imageTwo.getRGB(pt.x - flagOne.iconWidth, pt.y), true)
        }
    }

    return ImageIcon(newImage)
}

fun ImageIcon.toBufferedImage(type: Int): BufferedImage =
    image.toBufferedImage(iconWidth, iconHeight, type)

fun ImageIcon.greyscale(): ImageIcon {
    val original = image.toBufferedImage(PLAYER_IMAGE_WIDTH, PLAYER_IMAGE_HEIGHT)

    val newImage =
        BufferedImage(PLAYER_IMAGE_WIDTH, PLAYER_IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB)
    newImage.paint { pt ->
        val rgb = original.getRGB(pt.x, pt.y)
        Color(greyscale(rgb))
    }

    return ImageIcon(newImage)
}

private fun greyscale(rgb: Int): Int {
    val r = rgb shr 16 and 0xFF
    val g = rgb shr 8 and 0xFF
    val b = rgb and 0xFF

    // Normalize and gamma correct:
    val rr = (r / 255.0).pow(2.2).toFloat()
    val gg = (g / 255.0).pow(2.2).toFloat()
    val bb = (b / 255.0).pow(2.2).toFloat()

    // Calculate luminance:
    val lum = (0.2126 * rr + 0.7152 * gg + 0.0722 * bb).toFloat()

    // Gamma compand and rescale to byte range:
    val grayLevel = (255.0 * lum.toDouble().pow(1.0 / 2.2)).toInt()
    return (grayLevel shl 16) + (grayLevel shl 8) + grayLevel
}

fun convertImageToAvatarDimensions(imageBytes: ByteArray): ByteArray {
    val icon = ImageIcon(imageBytes)
    val image = icon.image.toBufferedImage(icon.iconWidth, icon.iconHeight)
    val minDimension = minOf(icon.iconWidth, icon.iconHeight)

    val xc = (image.width - minDimension) / 2
    val yc = (image.height - minDimension) / 2

    val croppedImage = image.getSubimage(xc, yc, minDimension, minDimension)
    val resizedImage = croppedImage.resize(PLAYER_IMAGE_WIDTH, PLAYER_IMAGE_HEIGHT)

    val baos = ByteArrayOutputStream()
    ImageIO.write(resizedImage, "jpg", baos)
    return baos.toByteArray()
}

fun BufferedImage.resize(width: Int, height: Int = width) =
    getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH).toBufferedImage(width, height)
