package dartzee.bean

import java.awt.Canvas
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.font.TextLayout
import javax.swing.JLabel
import javax.swing.SwingConstants

fun paintLabel(
    g: Graphics2D,
    center: Point,
    textHeight: Int,
    labelHeight: Int,
    baseFont: Font,
    color: Color,
    text: String,
    maxWidth: Int = Int.MAX_VALUE,
) {
    val font = getFontForHeight(text, baseFont, textHeight, g, maxWidth)

    val lbl = JLabel(text)
    lbl.foreground = color
    lbl.horizontalAlignment = SwingConstants.CENTER
    lbl.font = font

    // Work out the width for this label, based on the text
    val metrics = Canvas().getFontMetrics(font)
    val lblWidth = metrics.stringWidth(text) + 5
    lbl.setSize(lblWidth, labelHeight)

    val lblX = center.getX().toInt() - lblWidth / 2
    val lblY = center.getY().toInt() - labelHeight / 2

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.translate(lblX, lblY)
    lbl.paint(g)
    g.translate(-lblX, -lblY)
}

private fun getFontForHeight(
    text: String,
    baseFont: Font,
    height: Int,
    g: Graphics2D,
    maxWidth: Int = Int.MAX_VALUE,
): Font {
    val normalisedText = text.uppercase()

    // Start with a fontSize of 1
    var fontSize = 1f
    var font = baseFont.deriveFont(Font.PLAIN, fontSize)

    // We're going to increment our test font 1 at a time, and keep checking its height
    var testFont = font
    var bounds = TextLayout(normalisedText, font, g.fontRenderContext).bounds

    while (bounds.height < height * 0.75 && bounds.width < maxWidth) {
        // The last iteration succeeded, so set our return value to be the font we tested.
        font = testFont

        // Create a new testFont, with incremented font size
        fontSize++
        testFont = baseFont.deriveFont(Font.PLAIN, fontSize)

        // Get the updated font height
        bounds = TextLayout(normalisedText, font, g.fontRenderContext).bounds
    }

    return font
}
