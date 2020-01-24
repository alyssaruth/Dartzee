package dartzee.screen

import dartzee.`object`.ColourWrapper
import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SEGMENT_TYPE_MISS
import dartzee.core.bean.getPointList
import dartzee.core.bean.paint
import dartzee.core.util.Debug
import dartzee.core.util.getParentWindow
import dartzee.core.util.runOnEventThread
import dartzee.listener.DartboardListener
import dartzee.screen.game.DartsGameScreen
import dartzee.screen.game.GamePanelX01
import dartzee.utils.*
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.image.BufferedImage
import java.net.URL
import java.util.*
import javax.sound.sampled.*
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JLayeredPane
import javax.swing.SwingConstants


private const val LAYER_NUMBERS = 1
private const val LAYER_DARTS = 2
private const val LAYER_DODGY = 3
private const val LAYER_SLIDER = 4

open class Dartboard : JLayeredPane, MouseListener, MouseMotionListener
{
    private var hmPointToSegment = mutableMapOf<Point, DartboardSegment>()
    protected var hmSegmentKeyToSegment = mutableMapOf<String, DartboardSegment>()
    val scoringPoints = mutableListOf<Point>()

    private val dartLabels = mutableListOf<JLabel>()

    private var listener: DartboardListener? = null
    var centerPoint = Point(200, 200)
        private set

    private var diameter = 360.0

    var scoreLabelColor: Color = Color.WHITE
    var renderScoreLabels = false

    private var dartCount = 0
    private var simulation = false

    //Cached things
    private var lastHoveredSegment: DartboardSegment? = null
    private var colourWrapper: ColourWrapper? = null
    private var latestClip: Clip? = null

    var dartboardImage: BufferedImage? = null
    val dartboardLabel = JLabel()
    private val dodgyLabel = JLabel() //You know what this is...

    constructor()
    {
        layout = null
        add(dartboardLabel, Integer.valueOf(-1))
    }

    constructor(width: Int, height: Int)
    {
        setSize(width, height)
        dartboardLabel.setSize(width, height)
        layout = null
        add(dartboardLabel, Integer.valueOf(-1))
    }

    fun addDartboardListener(listener: DartboardListener)
    {
        this.listener = listener
    }

    fun paintDartboardCached()
    {
        paintDartboard(cached = true)
    }

    open fun paintDartboard(colourWrapper: ColourWrapper? = null, listen: Boolean = true, cached: Boolean = false)
    {
        val width = width
        val height = height

        Debug.append("Painting darboard. Dim[$width,$height]")

        dartboardLabel.setSize(width, height)

        //Initialise/clear down variables
        this.colourWrapper = colourWrapper
        centerPoint = Point(width / 2, height / 2)
        diameter = 0.7 * width
        hmPointToSegment.clear()

        if (cached
          && dartboardTemplate != null)
        {
            initialiseFromTemplate()
        }
        else
        {
            dartboardImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

            //Construct the segments, populated with their points. Cache pt -> segment.
            getPointList(width, height).forEach { factoryAndCacheSegmentForPoint(it) }

            Debug.append("Cached all points/segments.")

            //Render the actual image
            renderDartboardImage()
        }

        dartboardLabel.icon = ImageIcon(dartboardImage!!)
        dartboardLabel.repaint()

        addScoreLabels()

        if (cached
          && dartboardTemplate == null)
        {
            dartboardTemplate = DartboardTemplate(this)
        }

        //Now the dartboard is painted, add the mouse listeners
        if (listen)
        {
            ensureListening()
        }
    }

    open fun initialiseFromTemplate()
    {
        hmPointToSegment = dartboardTemplate!!.getPointToSegmentMap()
        hmSegmentKeyToSegment = dartboardTemplate!!.getSegmentKeyToSegmentMap()
        dartboardImage = dartboardTemplate!!.getDartboardImg()
    }

    private fun renderDartboardImage()
    {
        dartboardImage?.paint { getColourForPointAndSegment(it, getSegmentForPoint(it), false, colourWrapper) }
        Debug.append("Created dartboardImage")
    }

    private fun addScoreLabels()
    {
        if (!renderScoreLabels)
        {
            Debug.append("Not adding scores.")
            return
        }

        //Get the height we want for our labels, which is half the thickness of the outer band
        val radius = diameter / 2
        val outerRadius = UPPER_BOUND_OUTSIDE_BOARD_RATIO * radius
        val lblHeight = Math.round((outerRadius - radius) / 2).toInt()

        val fontToUse = getFontForDartboardLabels(lblHeight)

        for (i in 1..20)
        {
            //Create a label with standard properties
            val lbl = JLabel("" + i)
            lbl.foreground = scoreLabelColor
            lbl.background = DartsColour.TRANSPARENT
            lbl.isOpaque = true
            lbl.horizontalAlignment = SwingConstants.CENTER
            lbl.font = fontToUse

            //Work out the width for this label, based on the text
            val metrics = factoryFontMetrics(fontToUse)
            val lblWidth = metrics.stringWidth("" + i) + 2
            lbl.setSize(lblWidth, lblHeight)

            //Work out where to place the label
            val points = getPointsForSegment(i, SEGMENT_TYPE_MISS)
            val avgPoint = getAverage(points)
            val lblX = avgPoint.getX().toInt() - lblWidth / 2
            val lblY = avgPoint.getY().toInt() - lblHeight / 2
            lbl.setLocation(lblX, lblY)

            //Add to the screen
            add(lbl)
            setLayer(lbl, LAYER_NUMBERS)
        }
    }

    private fun getFontForDartboardLabels(lblHeight: Int): Font
    {
        //Start with a fontSize of 1
        var fontSize = 1
        var font = Font("Trebuchet MS", Font.PLAIN, fontSize)

        //We're going to increment our test font 1 at a time, and keep checking its height
        var testFont = font
        var metrics = factoryFontMetrics(testFont)
        var fontHeight = metrics.height

        while (fontHeight < lblHeight - 2)
        {
            //The last iteration succeeded, so set our return value to be the font we tested.
            font = testFont

            //Create a new testFont, with incremented font size
            fontSize++
            testFont = Font("Trebuchet MS", Font.PLAIN, fontSize)

            //Get the updated font height
            metrics = factoryFontMetrics(testFont)
            fontHeight = metrics.height
        }

        return font
    }

    private fun factoryFontMetrics(font: Font): FontMetrics
    {
        //Use a new Canvas rather than going via graphics, as then this will work headless (e.g. from tests)
        return Canvas().getFontMetrics(font)
    }

    fun highlightDartboard(hoveredPoint: Point)
    {
        val hoveredSegment = getSegmentForPoint(hoveredPoint)
        if (hoveredSegment == lastHoveredSegment)
        {
            //Nothing to do
            return
        }

        lastHoveredSegment?.let { colourSegment(it, false) }

        lastHoveredSegment = hoveredSegment
        colourSegment(hoveredSegment, true)
    }

    fun colourSegment(segment: DartboardSegment, highlight: Boolean)
    {
        val actuallyHighlight = highlight && !segment.isMiss() && shouldActuallyHighlight(segment)

        val hoveredColour = getColourForPointAndSegment(null, segment, actuallyHighlight, colourWrapper) ?: return
        colourSegment(segment, hoveredColour)
    }

    open fun shouldActuallyHighlight(segment: DartboardSegment) = true

    open fun colourSegment(segment: DartboardSegment, col: Color)
    {
        val pointsForCurrentSegment = segment.points
        for (i in pointsForCurrentSegment.indices)
        {
            val pt = pointsForCurrentSegment[i]
            if (colourWrapper?.edgeColour == null || !segment.isEdgePoint(pt))
            {
                colourPoint(pt, col)
            }
        }

        dartboardLabel.repaint()
    }

    private fun colourPoint(pt: Point, colour: Color)
    {
        val x = pt.getX().toInt()
        val y = pt.getY().toInt()

        val rgb = colour.rgb
        val currentRgb = dartboardImage!!.getRGB(x, y)

        if (rgb != currentRgb)
        {
            dartboardImage!!.setRGB(x, y, rgb)
        }
    }

    fun getAllSegments() = hmSegmentKeyToSegment.values.toList()

    protected fun getSegmentForPoint(pt: Point, stackTrace: Boolean = true): DartboardSegment
    {
        val segment = hmPointToSegment[pt]
        if (segment != null)
        {
            return segment
        }

        if (stackTrace)
        {
            Debug.stackTrace("Couldn't find segment for point (" + pt.getX() + ", " + pt.getY() + ")."
                    + "Width = " + width + ", Height = " + height)
        }

        return factoryAndCacheSegmentForPoint(pt)
    }

    private fun factoryAndCacheSegmentForPoint(pt: Point): DartboardSegment
    {
        val segmentKey = factorySegmentKeyForPoint(pt, centerPoint, diameter)
        var segment = hmSegmentKeyToSegment[segmentKey]
        if (segment == null)
        {
            segment = DartboardSegment(segmentKey)
            hmSegmentKeyToSegment[segmentKey] = segment
        }

        segment.addPoint(pt)
        hmPointToSegment[pt] = segment

        if (!segment.isMiss())
        {
            scoringPoints.add(pt)
        }

        return segment
    }

    /**
     * Public methods
     */
    fun getPointsForSegment(score: Int, type: Int): MutableList<Point>
    {
        val segmentKey = score.toString() + "_" + type
        val segment = hmSegmentKeyToSegment[segmentKey]
        return segment?.points ?: mutableListOf()
    }
    fun getSegment(score: Int, type: Int): DartboardSegment? = hmSegmentKeyToSegment["${score}_$type"]

    fun isDouble(pt: Point): Boolean
    {
        val seg = getSegmentForPoint(pt)
        return seg.isDoubleExcludingBull()
    }

    open fun dartThrown(pt: Point)
    {
        val dart = convertPointToDart(pt, true)

        listener?.let {
            addDart(pt)
            it.dartThrown(dart)
        }
    }

    fun addOverlay(pt: Point, overlay: Component)
    {
        add(overlay)
        setLayer(overlay, LAYER_SLIDER)
        overlay.location = pt
    }

    fun convertPointToDart(pt: Point, rationalise: Boolean): Dart
    {
        if (rationalise)
        {
            rationalisePoint(pt)
        }

        val segment = getSegmentForPoint(pt)
        return getDartForSegment(pt, segment)
    }

    fun rationalisePoint(pt: Point)
    {
        val x = pt.x.coerceIn(0, width - 1)
        val y = pt.y.coerceIn(0, height - 1)

        pt.setLocation(x, y)
    }

    fun listen(listen: Boolean)
    {
        if (listen)
        {
            ensureListening()
        }
        else
        {
            stopListening()
        }
    }

    fun ensureListening()
    {
        if (dartboardLabel.mouseListeners.isEmpty())
        {
            dartboardLabel.addMouseListener(this)
            dartboardLabel.addMouseMotionListener(this)
        }
    }

    fun stopListening()
    {
        if (dartboardLabel.mouseListeners.isNotEmpty())
        {
            dartboardLabel.removeMouseListener(this)
            dartboardLabel.removeMouseMotionListener(this)
        }

        //Undo any colouring that there might have been
        lastHoveredSegment?.let { colourSegment(it, false) }
    }

    fun doFawlty()
    {
        val rand = Random()
        val brucey = rand.nextInt(4) + 1

        doDodgy(ResourceCache.IMG_BASIL, 576, 419, "basil$brucey")
    }

    fun doForsyth()
    {
        val rand = Random()
        val brucey = rand.nextInt(4) + 1

        doDodgy(ResourceCache.IMG_BRUCE, 300, 478, "forsyth$brucey")
    }

    fun doBadLuck()
    {
        val rand = Random()
        val ix = rand.nextInt(2) + 1

        doDodgy(ResourceCache.IMG_BRUCE, 300, 478, "badLuck$ix")
    }

    fun doBull()
    {
        doDodgy(ResourceCache.IMG_DEV, 400, 476, "bull")
    }

    fun doBadMiss()
    {
        val rand = Random()
        val miss = rand.nextInt(5) + 1

        //4-1 ratio because mitchell > spencer!
        if (miss <= 4)
        {
            doDodgy(ResourceCache.IMG_MITCHELL, 300, 250, "badmiss$miss")
        }
        else
        {
            doDodgy(ResourceCache.IMG_SPENCER, 460, 490, "damage")
        }
    }

    fun doGolfMiss()
    {
        doDodgy(ResourceCache.IMG_DEV, 400, 476, "fourTrimmed")
    }

    private fun doDodgy(ii: ImageIcon, width: Int, height: Int, soundName: String)
    {
        if (!PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS) || simulation)
        {
            return
        }

        runOnEventThread { doDodgyOnEdt(ii, width, height, soundName) }
    }

    private fun doDodgyOnEdt(ii: ImageIcon, width: Int, height: Int, soundName: String)
    {
        dodgyLabel.icon = ii
        dodgyLabel.setSize(width, height)

        val x = (getWidth() - width) / 2
        val y = getHeight() - height
        dodgyLabel.setLocation(x, y)

        remove(dodgyLabel)
        add(dodgyLabel)

        setLayer(dodgyLabel, LAYER_DODGY)

        repaint()
        revalidate()

        playDodgySound(soundName)
    }

    fun playDodgySound(soundName: String)
    {
        if (!PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS) || simulation)
        {
            return
        }

        try
        {
            if (ResourceCache.isInitialised)
            {
                playDodgySoundCached(soundName)
            }
            else
            {
                playDodgySoundAdHoc(soundName)
            }
        }
        catch (e: Exception)
        {
            Debug.stackTrace(e, "Caught error playing sound [$soundName]")
        }

    }

    private fun playDodgySoundCached(soundName: String)
    {
        val stream = ResourceCache.borrowInputStream(soundName) ?: return

        val clip = initialiseAudioClip(stream, soundName)
        if (clip != null)
        {
            clip.open(stream)
            clip.start()
        }
    }

    /**
     * Old, ad-hoc version for playing sounds (was really slow on home PC).
     *
     * Caches the URL on-the-fly, but still initialises a fresh InputStream every time.
     */
    private fun playDodgySoundAdHoc(soundName: String)
    {
        Debug.append("Playing $soundName ad-hoc - this will be slow")

        var url: URL? = hmSoundNameToUrl[soundName]
        if (url == null)
        {
            url = javaClass.getResource("/wav/$soundName.wav")
            hmSoundNameToUrl[soundName] = url
        }

        //Resource may still be null if it genuinely doesn't exist. Just return.
        if (url == null)
        {
            return
        }

        val clip = initialiseAudioClip(null, soundName)
        if (clip != null)
        {
            clip.open(AudioSystem.getAudioInputStream(url))
            clip.start()
        }
    }

    private fun initialiseAudioClip(stream: AudioInputStream?, soundName: String): Clip?
    {
        val myClip = AudioSystem.getLine(Line.Info(Clip::class.java)) as Clip

        //Overwrite the 'latestClip' variable so this always stores the latest sound.
        //Allows us to not dismiss the label until the final sound has finished, in the case of overlapping sounds.
        latestClip = myClip

        myClip.addLineListener { event ->
            if (event.type === LineEvent.Type.STOP)
            {
                //Always close or return our one
                myClip.stop()
                myClip.close()

                if (ResourceCache.isInitialised && stream != null)
                {
                    ResourceCache.returnInputStream(soundName, stream)
                }

                //See whether there's currently any later clip still running. If there isn't, also dismiss our dodgyLabel
                val somethingRunning = latestClip?.isRunning ?: false
                if (!somethingRunning)
                {
                    remove(dodgyLabel)
                    repaint()
                    revalidate()
                }
            }
        }

        return myClip
    }

    private fun addDart(pt: Point)
    {
        if (dartLabels.isEmpty())
        {
            for (i in 0..4)
            {
                val lbl = JLabel(DARTIMG)
                lbl.setSize(76, 80)
                lbl.isVisible = false
                add(lbl)

                dartLabels.add(lbl)
            }
        }

        val lbl = dartLabels[dartCount]
        lbl.location = pt
        lbl.isVisible = true
        dartCount++

        setLayer(lbl, LAYER_DARTS, 5-dartCount)

        revalidate()
        repaint()
    }

    fun clearDarts()
    {
        //Always want to stop this at the same time
        //stopDodgy();

        for (i in dartLabels.indices)
        {
            val dartLabel = dartLabels[i]
            dartLabel.isVisible = false
        }

        dartCount = 0
        revalidate()
        repaint()

    }

    fun setSimulation(simulation: Boolean)
    {
        this.simulation = simulation
    }

    override fun mouseMoved(arg0: MouseEvent)
    {
        if (getParentWindow()?.isFocused == true)
        {
            highlightDartboard(arg0.point)
        }
    }

    override fun mouseReleased(arg0: MouseEvent)
    {
        if (!suppressClickForGameWindow())
        {
            val pt = arg0.point
            dartThrown(pt)
        }
    }
    private fun suppressClickForGameWindow(): Boolean
    {
        val scrn = getParentWindow() as? DartsGameScreen ?: return false
        if (scrn.haveLostFocus)
        {
            scrn.haveLostFocus = false
            return true
        }

        return false
    }

    override fun mouseClicked(arg0: MouseEvent) {}
    override fun mousePressed(arg0: MouseEvent) {}
    override fun mouseDragged(arg0: MouseEvent) {}
    override fun mouseEntered(arg0: MouseEvent) {}
    override fun mouseExited(arg0: MouseEvent) {}

    fun factoryOverlay() = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

    companion object
    {
        private val DARTIMG = ImageIcon(GamePanelX01::class.java.getResource("/dartImage.png"))

        private val hmSoundNameToUrl = mutableMapOf<String, URL>()
        var dartboardTemplate: DartboardTemplate? = null

        fun appearancePreferenceChanged()
        {
            dartboardTemplate = null
        }
    }

    inner class DartboardTemplate(dartboard: Dartboard)
    {
        private val dartboardImg = dartboard.dartboardImage!!
        private val hmPointToSegment = dartboard.hmPointToSegment
        private val hmSegmentKeyToSegment = dartboard.hmSegmentKeyToSegment

        fun getPointToSegmentMap(): MutableMap<Point, DartboardSegment>
        {
            return hmPointToSegment.toMutableMap()
        }

        fun getSegmentKeyToSegmentMap(): MutableMap<String, DartboardSegment>
        {
            return hmSegmentKeyToSegment.toMutableMap()
        }

        fun getDartboardImg(): BufferedImage
        {
            val cm = dartboardImg.colorModel
            val isAlphaPremultiplied = cm.isAlphaPremultiplied
            val writableRaster = dartboardImg.raster.createCompatibleWritableRaster()
            val raster = dartboardImg.copyData(writableRaster)
            return BufferedImage(cm, raster, isAlphaPremultiplied, null)
        }

    }

}
