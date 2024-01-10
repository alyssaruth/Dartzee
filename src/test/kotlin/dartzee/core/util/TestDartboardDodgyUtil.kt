package dartzee.core.util

import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import dartzee.core.helper.verifyNotCalled
import dartzee.helper.AbstractRegistryTest
import dartzee.logging.CODE_AUDIO_ERROR
import dartzee.logging.Severity
import dartzee.screen.GameplayDartboard
import dartzee.utils.PREFERENCES_BOOLEAN_SHOW_ANIMATIONS
import dartzee.utils.PreferenceUtil
import dartzee.utils.ResourceCache
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.Control
import javax.sound.sampled.Line
import javax.sound.sampled.LineEvent
import javax.sound.sampled.LineListener
import javax.swing.JLabel
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestDartboardDodgyUtil : AbstractRegistryTest() {
    override fun getPreferencesAffected() = listOf(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS)

    @BeforeEach
    fun beforeEach() {
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS, true)
    }

    @Test
    fun `should not play a sound if preference is disabled`() {
        mockkStatic(AudioSystem::class)

        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS, false)
        val dartboard = GameplayDartboard()
        dartboard.playDodgySound("60")

        verifyNotCalled { AudioSystem.getLine(any()) }
    }

    @Test
    fun `should do nothing if invalid sound is requested`() {
        mockkStatic(AudioSystem::class)

        val dartboard = GameplayDartboard()
        dartboard.playDodgySound("invalid")

        verifyNotCalled { AudioSystem.getLine(any()) }
    }

    @Test
    fun `should log an error and hide the image if there is an error playing the audio`() {
        val dartboard = GameplayDartboard()
        captureClip(true)

        dartboard.doBadLuck()
        flushEdt()

        dartboard.dodgyLabelShouldNotExist()
        verifyLog(CODE_AUDIO_ERROR, Severity.ERROR)
    }

    @Test
    fun `should show image until sound clip has finished playing`() {
        val dartboard = GameplayDartboard()
        val clip = captureClip()

        dartboard.doBadLuck()
        flushEdt()

        dartboard.dodgyLabelShouldExist()

        clip.simulateFinish()
        dartboard.dodgyLabelShouldNotExist()
    }

    @Test
    fun `should continue to show image until the final sound clip has finished playing`() {
        val dartboard = GameplayDartboard()
        val clip1 = captureClip()

        dartboard.doBadLuck()
        flushEdt()

        val clip2 = captureClip()
        dartboard.doChucklevision()
        flushEdt()

        dartboard.dodgyLabelShouldExist()

        clip1.simulateFinish()
        dartboard.dodgyLabelShouldExist()

        clip2.simulateFinish()
        dartboard.dodgyLabelShouldNotExist()
    }

    private fun captureClip(throwError: Boolean = false): HackedClip {
        val clip = HackedClip(throwError)
        mockkStatic(AudioSystem::class)
        every { AudioSystem.getLine(any()) } returns clip
        return clip
    }

    private fun GameplayDartboard.dodgyLabelShouldExist() {
        getChild<JLabel>("DodgyLabel").shouldBeVisible()
    }

    private fun GameplayDartboard.dodgyLabelShouldNotExist() {
        findChild<JLabel>("DodgyLabel").shouldBeNull()
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            ResourceCache.initialiseResources()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            ResourceCache.resetCache()
        }
    }
}

class HackedClip(private val throwError: Boolean) : Clip {
    private var lineListener: LineListener? = null
    private var running: Boolean = true

    override fun addLineListener(p0: LineListener?) {
        lineListener = p0
    }

    override fun isRunning() = running

    fun simulateFinish() {
        running = false
        lineListener.shouldNotBeNull()
        lineListener?.update(LineEvent(this, LineEvent.Type.STOP, 50L))
    }

    override fun close() {}

    override fun getLineInfo() = mockk<Line.Info>()

    override fun open(p0: AudioFormat?, p1: ByteArray?, p2: Int, p3: Int) {}

    override fun open(p0: AudioInputStream?) {
        if (throwError) {
            throw Exception("Oh dear oh dear")
        }
    }

    override fun open() {}

    override fun isOpen() = false

    override fun getControls(): Array<Control> = arrayOf()

    override fun isControlSupported(p0: Control.Type?) = false

    override fun getControl(p0: Control.Type?) = mockk<Control>()

    override fun removeLineListener(p0: LineListener?) {}

    override fun drain() {}

    override fun flush() {}

    override fun start() {}

    override fun stop() {}

    override fun isActive() = false

    override fun getFormat() = mockk<AudioFormat>()

    override fun getBufferSize() = 0

    override fun available() = 0

    override fun getFramePosition() = 0

    override fun getLongFramePosition() = 0L

    override fun getMicrosecondPosition() = 0L

    override fun getLevel() = 0f

    override fun getFrameLength() = 0

    override fun getMicrosecondLength() = 0L

    override fun setFramePosition(p0: Int) {}

    override fun setMicrosecondPosition(p0: Long) {}

    override fun setLoopPoints(p0: Int, p1: Int) {}

    override fun loop(p0: Int) {}
}
