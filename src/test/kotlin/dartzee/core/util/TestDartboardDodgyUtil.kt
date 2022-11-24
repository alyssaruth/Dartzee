import com.github.alexburlton.swingtest.findChild
import com.github.alexburlton.swingtest.flushEdt
import com.github.alexburlton.swingtest.getChild
import com.github.alexburlton.swingtest.shouldBeVisible
import dartzee.core.bean.SwingLabel
import dartzee.core.util.doBadLuck
import dartzee.core.util.doChucklevision
import dartzee.helper.AbstractRegistryTest
import dartzee.makeTestDartboard
import dartzee.screen.Dartboard
import dartzee.utils.PREFERENCES_BOOLEAN_SHOW_ANIMATIONS
import dartzee.utils.PreferenceUtil
import dartzee.utils.ResourceCache
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.Control
import javax.sound.sampled.Line
import javax.sound.sampled.LineEvent
import javax.sound.sampled.LineListener

class TestDartboardDodgyUtil : AbstractRegistryTest()
{
    override fun getPreferencesAffected() = listOf(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS)

    @BeforeEach
    fun beforeEach()
    {
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS, true)
    }

    @Test
    fun `should show image until sound clip has finished playing`()
    {
        val dartboard = makeTestDartboard()
        val clip = captureClip()

        dartboard.doBadLuck()
        flushEdt()

        dartboard.dodgyLabelShouldExist()

        clip.simulateFinish()
        dartboard.dodgyLabelShouldNotExist()
    }

    @Test
    fun `should continue to show image until the final sound clip has finished playing`()
    {
        val dartboard = makeTestDartboard()
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

    private fun captureClip(): HackedClip {
        val clip = HackedClip()
        mockkStatic(AudioSystem::class)
        every { AudioSystem.getLine(any()) } returns clip
        return clip
    }

    private fun Dartboard.dodgyLabelShouldExist() {
        getChild<SwingLabel> { it.testId == "dodgyLabel" }.shouldBeVisible()
    }

    private fun Dartboard.dodgyLabelShouldNotExist() {
        findChild<SwingLabel> { it.testId == "dodgyLabel" }.shouldBeNull()
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll()
        {
            ResourceCache.initialiseResources()
        }
    }
}

class HackedClip : Clip {
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
    override fun open(p0: AudioInputStream?) {}
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