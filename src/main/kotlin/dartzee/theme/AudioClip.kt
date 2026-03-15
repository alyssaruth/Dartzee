package dartzee.theme

import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

data class AudioClip(private val stream: AudioInputStream) {
    init {
        stream.mark(Int.MAX_VALUE)
    }

    private var lastClip: Clip? = null

    fun playOnce() {
        stop()

        stream.reset()
        val clip = AudioSystem.getClip()
        clip.open(stream)
        clip.start()
        clip.addLineListener { event ->
            if (event.type === LineEvent.Type.STOP) {
                clip.stop()
                clip.close()
            }
        }

        lastClip = clip
    }

    fun loop() {
        if (lastClip == null) {
            val clip = AudioSystem.getClip()
            clip.open(stream)
            lastClip = clip
        }

        lastClip?.loop(Clip.LOOP_CONTINUOUSLY)
    }

    fun stop() {
        lastClip?.stop()
    }
}
