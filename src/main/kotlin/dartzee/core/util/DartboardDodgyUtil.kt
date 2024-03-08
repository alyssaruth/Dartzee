package dartzee.core.util

import dartzee.logging.CODE_AUDIO_ERROR
import dartzee.logging.CODE_RESOURCE_CACHE_NOT_INITIALISED
import dartzee.preferences.Preferences
import dartzee.screen.GameplayDartboard
import dartzee.screen.LAYER_DODGY
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.preferenceService
import dartzee.utils.ResourceCache
import java.util.*
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.Line
import javax.sound.sampled.LineEvent
import javax.swing.ImageIcon
import javax.swing.JLabel

fun GameplayDartboard.doChucklevision() {
    val rand = Random()
    val chuckleSound = rand.nextInt(3) + 1

    doDodgy(ResourceCache.IMG_CHUCKLE, 266, 279, "chucklevision$chuckleSound")
}

fun GameplayDartboard.doFawlty() {
    val rand = Random()
    val brucey = rand.nextInt(4) + 1

    doDodgy(ResourceCache.IMG_BASIL, 576, 419, "basil$brucey")
}

fun GameplayDartboard.doForsyth() {
    val rand = Random()
    val brucey = rand.nextInt(4) + 1

    doDodgy(ResourceCache.IMG_BRUCE, 300, 478, "forsyth$brucey")
}

fun GameplayDartboard.doBadLuck() {
    val rand = Random()
    val ix = rand.nextInt(2) + 1

    doDodgy(ResourceCache.IMG_BRUCE, 300, 478, "badLuck$ix")
}

fun GameplayDartboard.doBull() {
    doDodgy(ResourceCache.IMG_DEV, 400, 476, "bull")
}

fun GameplayDartboard.doBadMiss() {
    val rand = Random()
    val miss = rand.nextInt(5) + 1

    // 4-1 ratio because mitchell > spencer!
    if (miss <= 4) {
        doDodgy(ResourceCache.IMG_MITCHELL, 300, 250, "badmiss$miss")
    } else {
        doDodgy(ResourceCache.IMG_SPENCER, 460, 490, "damage")
    }
}

fun GameplayDartboard.doGolfMiss() {
    doDodgy(ResourceCache.IMG_DEV, 400, 476, "fourTrimmed")
}

private fun GameplayDartboard.doDodgy(ii: ImageIcon, width: Int, height: Int, soundName: String) {
    if (!preferenceService.get(Preferences.showAnimations)) {
        return
    }

    runOnEventThread { doDodgyOnEdt(ii, width, height, soundName) }
}

private fun GameplayDartboard.doDodgyOnEdt(
    ii: ImageIcon,
    width: Int,
    height: Int,
    soundName: String
) {
    removeDodgyLabels()

    val dodgyLabel = JLabel("")
    dodgyLabel.name = "DodgyLabel"
    dodgyLabel.icon = ii
    dodgyLabel.setSize(width, height)

    val x = (getWidth() - width) / 2
    val y = getHeight() - height
    dodgyLabel.setLocation(x, y)
    add(dodgyLabel)

    setLayer(dodgyLabel, LAYER_DODGY)

    repaint()
    revalidate()

    playDodgySound(soundName)
}

fun GameplayDartboard.playDodgySound(soundName: String) {
    if (!preferenceService.get(Preferences.showAnimations)) {
        return
    }

    try {
        if (ResourceCache.isInitialised) {
            playDodgySoundCached(soundName)
        } else {
            logger.warn(
                CODE_RESOURCE_CACHE_NOT_INITIALISED,
                "Not playing [$soundName] - ResourceCache not initialised"
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
    soundName: String
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
