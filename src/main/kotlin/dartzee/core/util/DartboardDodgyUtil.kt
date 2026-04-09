package dartzee.core.util

import dartzee.bean.paintLabel
import dartzee.logging.CODE_AUDIO_ERROR
import dartzee.logging.CODE_RESOURCE_CACHE_NOT_INITIALISED
import dartzee.preferences.Preferences
import dartzee.screen.GameplayDartboard
import dartzee.screen.LAYER_DODGY
import dartzee.screen.animation.Animation
import dartzee.screen.animation.IAnimation
import dartzee.screen.animation.IAnimationTrigger
import dartzee.theme.getBaseFont
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.preferenceService
import dartzee.utils.ResourceCache
import dartzee.utils.toBufferedImage
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import java.awt.image.BufferedImage
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.Line
import javax.sound.sampled.LineEvent
import javax.swing.ImageIcon
import javax.swing.JLabel

fun GameplayDartboard.doDodgy(trigger: IAnimationTrigger) {
    InjectedThings.animations[trigger]?.let(::doDodgy)
}

fun GameplayDartboard.doDodgy(animation: IAnimation) {
    if (!preferenceService.get(Preferences.showAnimations)) {
        return
    }

    runOnEventThread { doDodgyOnEdt(animation.getAnimation()) }
}

private fun GameplayDartboard.doDodgyOnEdt(animation: Animation) {
    removeDodgyLabels()

    animation.img?.let { ii ->
        val img = animation.text?.let { text ->
            val bi = ii.toBufferedImage(BufferedImage.TYPE_INT_ARGB)
            val graphics = bi.graphics as Graphics2D

            val center = Point(bi.width / 2, bi.height / 2)
            paintLabel(graphics, center, bi.height / 3, bi.height, getBaseFont(), Color.white, text)

            ImageIcon(bi)
        } ?: ii

        val dodgyLabel = JLabel("")
        dodgyLabel.name = "DodgyLabel"
        dodgyLabel.icon = img
        dodgyLabel.setSize(img.iconWidth, img.iconHeight)

        val x = (getWidth() - img.iconWidth) / 2
        val y = getHeight() - img.iconHeight
        dodgyLabel.setLocation(x, y)
        add(dodgyLabel)

        setLayer(dodgyLabel, LAYER_DODGY)

        repaint()
        revalidate()
    }

    playDodgySound(animation.wavResource)
}

private fun GameplayDartboard.playDodgySound(soundName: String) {
    if (!preferenceService.get(Preferences.showAnimations)) {
        return
    }

    try {
        if (ResourceCache.isInitialised) {
            playDodgySoundCached(soundName)
        } else {
            logger.warn(
                CODE_RESOURCE_CACHE_NOT_INITIALISED,
                "Not playing [$soundName] - ResourceCache not initialised",
            )
        }
    } catch (e: Throwable) {
        logger.error(CODE_AUDIO_ERROR, "Caught error playing sound [$soundName]", e)
        resetDodgy()
    }
}

private fun GameplayDartboard.playDodgySoundCached(soundName: String) {
    val stream = ResourceCache.borrowInputStream(soundName) ?: return

    val clip = initialiseAudioClip(stream, soundName)
    clip.open(stream)
    clip.start()
}

private fun GameplayDartboard.initialiseAudioClip(
    stream: AudioInputStream,
    soundName: String,
): Clip {
    val myClip = AudioSystem.getLine(Line.Info(Clip::class.java)) as Clip

    // Overwrite the 'latestClip' variable so this always stores the latest sound.
    // Allows us to not dismiss the label until the final sound has finished, in the case of
    // overlapping sounds.
    latestClip = myClip

    myClip.addLineListener { event ->
        if (event.type === LineEvent.Type.STOP) {
            // Always close or return our one
            myClip.stop()
            myClip.close()

            ResourceCache.returnInputStream(soundName, stream)

            val somethingRunning = latestClip?.isRunning ?: false
            if (!somethingRunning) {
                resetDodgy()
            }
        }
    }

    return myClip
}

private fun GameplayDartboard.resetDodgy() {
    removeDodgyLabels()
    repaint()
    revalidate()
}

private fun GameplayDartboard.removeDodgyLabels() {
    getAllChildComponentsForType<JLabel>().filter { it.name == "DodgyLabel" }.forEach(::remove)
}
