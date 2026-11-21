package dartzee.theme

import dartzee.core.util.runOnEventThreadBlocking
import dartzee.screen.DartsApp
import dartzee.screen.MenuScreen
import dartzee.utils.rotateImageByDegrees
import java.awt.Dimension
import java.awt.Point
import javax.swing.ImageIcon
import javax.swing.JLabel
import kotlin.random.Random

enum class SlideMode {
    BOTTOM,
    TOP,
    LEFT,
    RIGHT,
}

class PopupSlider(
    private val app: DartsApp,
    imagePath: String,
    private val listener: PopupListener? = null,
    private val phasePause: Long = 1500,
) {
    private val img = ImageIcon(javaClass.getResource(imagePath))
    private val lbl = JLabel(img)
    private val imgMap =
        mapOf(
            SlideMode.BOTTOM to img,
            SlideMode.TOP to rotateImageByDegrees(img, 180.0),
            SlideMode.LEFT to rotateImageByDegrees(img, 90.0),
            SlideMode.RIGHT to rotateImageByDegrees(img, 270.0),
        )

    fun start() {
        val t = Thread {
            while (true) {
                Thread.sleep(1000)
                val result = Random.nextInt(100)
                if (result >= 90 && app.currentScreen !is MenuScreen) {
                    animate(SlideMode.entries.random())
                }
            }
        }

        t.name = "PopupSlider"
        t.start()
    }

    fun animate(mode: SlideMode) {
        val icon = imgMap.getValue(mode)
        val lblSizes = Dimension(icon.iconWidth, icon.iconHeight)
        runOnEventThreadBlocking {
            app.remove(lbl)
            lbl.icon = icon
            lbl.size = lblSizes
        }

        val centerX = (app.width / 2) - lblSizes.width / 2
        val centerY = (app.height / 2) - lblSizes.height / 2
        when (mode) {
            SlideMode.BOTTOM ->
                animate(Point(centerX, app.height), Point(centerX, app.height - lblSizes.height))
            SlideMode.TOP -> animate(Point(centerX, 0 - lblSizes.height), Point(centerX, 0))
            SlideMode.LEFT -> animate(Point(0 - lblSizes.width, centerY), Point(0, centerY))
            SlideMode.RIGHT ->
                animate(Point(app.width, centerY), Point(app.width - lblSizes.width, centerY))
        }
    }

    private fun animate(start: Point, end: Point) {
        runOnEventThreadBlocking { app.add(lbl, 0) }

        animatePhase(start, end, "A")

        Thread.sleep(phasePause)

        animatePhase(end, start, "B")

        runOnEventThreadBlocking {
            app.remove(lbl)
            app.repaint()
        }
    }

    private fun animatePhase(start: Point, end: Point, phaseName: String) {
        val xDiff = (end.x - start.x) / 20
        val yDiff = (end.y - start.y) / 20

        (0..20).forEach { step ->
            runOnEventThreadBlocking {
                lbl.setLocation(start.x + (step * xDiff), start.y + (step * yDiff))
                listener?.popupMoved(phaseName, step)
            }

            Thread.sleep(20)
        }
    }
}

interface PopupListener {
    fun popupMoved(phase: String, step: Int)
}
