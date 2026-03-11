package dartzee.core.util

import dartzee.logging.CODE_AUDIO_ERROR
import dartzee.logging.CODE_RESOURCE_CACHE_NOT_INITIALISED
import dartzee.preferences.Preferences
import dartzee.screen.GameplayDartboard
import dartzee.screen.LAYER_DODGY
import dartzee.screen.animation.Animation
import dartzee.screen.animation.IAnimation
import dartzee.screen.animation.IAnimationTrigger
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.preferenceService
import dartzee.utils.ResourceCache
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.Line
import javax.sound.sampled.LineEvent
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
        val dodgyLabel = JLabel("")
        dodgyLabel.name = "DodgyLabel"
        dodgyLabel.icon = ii
        dodgyLabel.setSize(ii.iconWidth, ii.iconHeight)

        val x = (getWidth() - ii.iconWidth) / 2
        val y = getHeight() - ii.iconHeight
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
