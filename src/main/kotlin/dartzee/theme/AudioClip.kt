package dartzee.theme

import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.Line
import javax.sound.sampled.LineEvent

data class AudioClip(private val stream: AudioInputStream) {
    private var cachedClip: Clip

    init {
        stream.mark(Int.MAX_VALUE)
        cachedClip = prepareNewClip()
    }

    private fun prepareNewClip(): Clip {
        val clip = AudioSystem.getLine(Line.Info(Clip::class.java)) as Clip
        clip.open(stream)
        return clip
    }

    fun playOnce() {
        stream.reset()
        val clip = cachedClip
        clip.start()
        clip.addLineListener { event ->
            if (event.type === LineEvent.Type.STOP) {
                clip.stop()
                clip.close()
            }
        }

        cachedClip = prepareNewClip()
    }

    fun loop() {
        cachedClip.loop(Clip.LOOP_CONTINUOUSLY)
    }

    fun stop() {
        cachedClip.stop()
    }
}
