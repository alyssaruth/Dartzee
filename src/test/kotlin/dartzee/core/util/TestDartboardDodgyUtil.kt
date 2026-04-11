package dartzee.core.util

import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldMatch
import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.core.helper.verifyNotCalled
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.logging.CODE_AUDIO_ERROR
import dartzee.logging.Severity
import dartzee.preferences.Preferences
import dartzee.screen.GameplayDartboard
import dartzee.screen.animation.Animation
import dartzee.screen.animation.BRUCEY_BAD_LUCK
import dartzee.screen.animation.BadLuckTrigger
import dartzee.screen.animation.CHUCKLEVISION
import dartzee.screen.animation.DEFAULT_ANIMATIONS
import dartzee.screen.animation.TotalScoreTrigger
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.preferenceService
import dartzee.utils.ResourceCache
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.Control
import javax.sound.sampled.Line
import javax.sound.sampled.LineEvent
import javax.sound.sampled.LineListener
import javax.swing.JLabel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestDartboardDodgyUtil : AbstractTest() {
    @BeforeEach
    fun before() {
        InjectedThings.animations = DEFAULT_ANIMATIONS
        ResourceCache.initialiseResources()

        mockkStatic(AudioSystem::class)
    }

    @AfterEach
    fun after() {
        unmockkStatic(AudioSystem::class)
    }

    @Test
    fun `should not play a sound if preference is disabled`() {
        preferenceService.save(Preferences.showAnimations, false)
        val dartboard = GameplayDartboard()
        dartboard.doDodgy(Animation("60", null))

        verifyNotCalled { AudioSystem.getLine(any()) }
    }

    @Test
    fun `should do nothing if invalid sound is requested`() {
        val dartboard = GameplayDartboard()
        dartboard.doDodgy(Animation("invalid", null))
        flushEdt()

        verifyNotCalled { AudioSystem.getLine(any()) }
    }

    @Test
    fun `should log an error and hide the image if there is an error playing the audio`() {
        val dartboard = GameplayDartboard()
        captureClip(true)

        dartboard.doDodgy(BRUCEY_BAD_LUCK)
        flushEdt()

        dartboard.dodgyLabelShouldNotExist()
        verifyLog(CODE_AUDIO_ERROR, Severity.ERROR)
    }

    @Test
    fun `should show image until sound clip has finished playing`() {
        val dartboard = GameplayDartboard()
        val clip = captureClip()

        dartboard.doDodgy(BRUCEY_BAD_LUCK)
        flushEdt()

        dartboard.dodgyLabelShouldExist()

        clip.simulateFinish()
        dartboard.dodgyLabelShouldNotExist()
    }

    @Test
    fun `should do nothing if trigger is not associated with an animation`() {
        val dartboard = GameplayDartboard()
        dartboard.doDodgy(TotalScoreTrigger(GameType.X01, 75))

        verifyNotCalled { AudioSystem.getLine(any()) }
    }

    @Test
    fun `should launch the appropriate animation based on the trigger`() {
        InjectedThings.animations = mapOf(BadLuckTrigger to BRUCEY_BAD_LUCK)
        val dartboard = GameplayDartboard()
        captureClip()
        dartboard.doDodgy(BadLuckTrigger)
        flushEdt()

        dartboard.dodgyLabel()!!.icon.shouldMatch(BRUCEY_BAD_LUCK.getAnimation().img!!)
    }

    @Test
    @Tag("screenshot")
    fun `should render text over image if specified`() {
        InjectedThings.animations =
            mapOf(
                BadLuckTrigger to
                    Animation("party-popper", "/theme/birthday/horrific/party-popper-1.png", "33")
            )

        val dartboard = GameplayDartboard()
        captureClip()
        dartboard.doDodgy(BadLuckTrigger)
        flushEdt()

        dartboard.dodgyLabel()!!.shouldMatchImage("animation-with-text")
    }

    @Test
    fun `should continue to show image until the final sound clip has finished playing`() {
        val dartboard = GameplayDartboard()
        val clip1 = captureClip()

        dartboard.doDodgy(BRUCEY_BAD_LUCK)
        flushEdt()

        val clip2 = captureClip()
        dartboard.doDodgy(CHUCKLEVISION)
        flushEdt()

        dartboard.dodgyLabelShouldExist()

        clip1.simulateFinish()
        dartboard.dodgyLabelShouldExist()

        clip2.simulateFinish()
        dartboard.dodgyLabelShouldNotExist()
    }

    private fun captureClip(throwError: Boolean = false): HackedClip {
        val clip = HackedClip(throwError)
        every { AudioSystem.getLine(any()) } returns clip
        return clip
    }

    private fun GameplayDartboard.dodgyLabel() = findChild<JLabel>("DodgyLabel")

    private fun GameplayDartboard.dodgyLabelShouldExist() {
        dodgyLabel()!!.shouldBeVisible()
    }

    private fun GameplayDartboard.dodgyLabelShouldNotExist() {
        dodgyLabel().shouldBeNull()
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
