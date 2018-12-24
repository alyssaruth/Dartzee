package burlton.dartzee.code.screen;

import burlton.core.code.obj.SuperHashMap;
import burlton.core.code.util.Debug;
import burlton.dartzee.code.listener.DartboardListener;
import burlton.dartzee.code.object.ColourWrapper;
import burlton.dartzee.code.object.Dart;
import burlton.dartzee.code.object.DartboardSegment;
import burlton.dartzee.code.object.DartboardSegmentKt;
import burlton.dartzee.code.screen.game.GamePanelX01;
import burlton.dartzee.code.utils.*;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class Dartboard extends JLayeredPane
					   implements MouseListener,
					   MouseMotionListener
{
	private static final ImageIcon DARTIMG = new ImageIcon(GamePanelX01.class.getResource("/dartImage.png"));
	
	private static SuperHashMap<String, URL> hmSoundNameToUrl = new SuperHashMap<>();
	
	private SuperHashMap<Point, DartboardSegmentKt> hmPointToSegment = new SuperHashMap<>();
	private SuperHashMap<String, DartboardSegmentKt> hmSegmentKeyToSegment = new SuperHashMap<>();
	
	private ArrayList<JLabel> dartLabels = new ArrayList<>();
	
	private DartboardListener listener = null;
	private Point centerPt = new Point(200, 200);
	private double diameter = 360;
	private boolean renderScoreLabels = false;
	
	private int dartCount = 0;
	
	private boolean simulation = false;
	
	//Cached things
	private DartboardSegmentKt lastHoveredSegment = null;
	private ColourWrapper colourWrapper = null;
	private Clip clip = null;
	
	public Dartboard()
	{
		setLayout(null);
		add(dartboardLabel, Integer.valueOf(-1));
	}
	public Dartboard(int width, int height) 
	{
		setSize(width, height);
		dartboardLabel.setSize(width, height);
		setLayout(null);
		add(dartboardLabel, Integer.valueOf(-1));
	}

	private BufferedImage dartboardImage = null;
	private final JLabel dartboardLabel = new JLabel();
	private JLabel dodgyLabel = new JLabel(); //You know what this is...
	
	public void addDartboardListener(DartboardListener listener)
	{
		this.listener = listener;
	}
	
	public void paintDartboard()
	{
		paintDartboard(null, true);
	}
	public void paintDartboard(ColourWrapper colourWrapper, boolean listen)
	{	
		int width = getWidth();
		int height = getHeight();
		
		Debug.append("Painting darboard. Dim[" + width + "," + height + "]");
		
		dartboardLabel.setSize(width, height);
		
		//Initialise/clear down variables
		this.colourWrapper = colourWrapper;
		centerPt = new Point(width/2, height/2);
		diameter = 0.7 * width;
		hmPointToSegment.clear();
		dartboardImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		//Construct the segments, populated with their points. Cache pt -> segment.
		for (int x=0; x<width; x++) 
		{
		    for (int y=0; y<height; y++) 
		    {
		    	Point pt = new Point(x, y);
		    	factoryAndCacheSegmentForPoint(pt);
		    }
		}
		
		Debug.append("Cached all points/segments.");
		
		//Render the actual image
		renderDartboardImage(width, height);
		
		addScoreLabels();
		
		//Now the dartboard is painted, add the mouse listeners
		if (listen)
		{
			ensureListening();
		}
	}
	
	private void renderDartboardImage(int width, int height)
	{
		int[] pixels = new int[width * height];
		int pixelIx = 0;
		Point pt = new Point();
		for (int y=0; y<height; y++)
	    {
	        for (int x=0; x<width; x++)
	        {
	        	pt.setLocation(x, y);

				DartboardSegmentKt segment = getSegmentForPoint(pt);
				Color colour = DartboardUtil.getColourForPointAndSegment(pt, segment, false, colourWrapper);
		    	
		    	if (colour != null)
		    	{
		    		pixels[pixelIx] = colour.getRGB();
		    	}
		    	
		    	pixelIx++;
	        }
	    }
		
		dartboardImage.setRGB(0, 0, width, height, pixels, 0, width);
		
		Debug.append("Created dartboardImage");
		
		dartboardLabel.setIcon(new ImageIcon(dartboardImage));
		dartboardLabel.repaint();
	}
	
	private void addScoreLabels()
	{
		if (!renderScoreLabels)
		{
			Debug.append("Not adding scores.");
			return;
		}
		
		//Get the height we want for our labels, which is half the thickness of the outer band
		double radius = diameter / 2;
		double outerRadius = DartboardUtil.UPPER_BOUND_OUTSIDE_BOARD_RATIO * radius;
		int lblHeight = (int)Math.round((outerRadius - radius)/2);
		
		Font fontToUse = getFontForDartboardLabels(lblHeight);
		
		for (int i=1; i<=20; i++)
		{
			//Create a label with standard properties
			JLabel lbl = new JLabel("" + i);
			lbl.setForeground(Color.WHITE);
			lbl.setBackground(DartsColour.TRANSPARENT);
			lbl.setOpaque(true);
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			lbl.setFont(fontToUse);
			
			//Work out the width for this label, based on the text
			FontMetrics metrics = factoryFontMetrics(fontToUse);
			int lblWidth = metrics.stringWidth("" + i) + 2;
			lbl.setSize(lblWidth, lblHeight);
			
			//Work out where to place the label
			ArrayList<Point> points = getPointsForSegment(i, DartboardSegment.TYPE_MISS);
			Point avgPoint = GeometryUtil.getAverage(points);
			int lblX = (int)avgPoint.getX() - (lblWidth / 2);
			int lblY = (int)avgPoint.getY() - (lblHeight / 2);
			lbl.setLocation(lblX, lblY);
			
			//Add to the screen
			add(lbl);
		}
	}
	private Font getFontForDartboardLabels(int lblHeight)
	{
		//Start with a fontSize of 1
		int fontSize = 1;
		Font font = new Font("Trebuchet MS", Font.PLAIN, fontSize);
		
		//We're going to increment our test font 1 at a time, and keep checking its height
		Font testFont = font;
		FontMetrics metrics = factoryFontMetrics(testFont);
		int fontHeight = metrics.getHeight();
		
		while (fontHeight < lblHeight - 2)
		{
			//The last iteration succeeded, so set our return value to be the font we tested.
			font = testFont;
			
			//Create a new testFont, with incremented font size
			fontSize++;
			testFont = new Font("Trebuchet MS", Font.PLAIN, fontSize);
			
			//Get the updated font height
			metrics = factoryFontMetrics(testFont);
			fontHeight = metrics.getHeight();
		}
		
		return font;
	}
	private FontMetrics factoryFontMetrics(Font font)
	{
		//Get the graphics off the main window, because that will always be visible
		Graphics gfx = ScreenCache.getMainScreen().getGraphics();
		return gfx.getFontMetrics(font);
	}

	private void highlightDartboard(Point hoveredPoint)
	{
		DartboardSegmentKt hoveredSegment = getSegmentForPoint(hoveredPoint);
		if (hoveredSegment.equals(lastHoveredSegment))
		{
			//Nothing to do
			return;
		}
		
		if (lastHoveredSegment != null)
		{
			colourSegment(lastHoveredSegment, false);
		}
		
		lastHoveredSegment = hoveredSegment;
		colourSegment(lastHoveredSegment, true);
	}
	private void colourSegment(DartboardSegmentKt segment, boolean highlight)
	{
		if (segment.isMiss())
		{
			//Don't do any highlighting for missing the board
			return;
		}
		
		Color hoveredColour = DartboardUtil.getColourForPointAndSegment(null, segment, highlight, colourWrapper);
		ArrayList<Point> pointsForCurrentSegment = segment.getPoints();
		for (int i=0; i<pointsForCurrentSegment.size(); i++)
		{
			Point pt = pointsForCurrentSegment.get(i);
			colourPoint(pt, hoveredColour);
		}
		
		dartboardLabel.repaint();
	}
	
	private void colourPoint(Point pt, Color colour)
	{
		int x = (int)pt.getX();
		int y = (int)pt.getY();
		
		int rgb = colour.getRGB();
    	int currentRgb = dartboardImage.getRGB(x, y);
    	
    	if (rgb != currentRgb)
    	{
    		dartboardImage.setRGB(x, y, rgb);
    	}
	}
	
	private DartboardSegmentKt getSegmentForPoint(Point pt)
	{
		DartboardSegmentKt segment = hmPointToSegment.get(pt);
		if (segment != null)
		{
			return segment;
		}
		
		Debug.stackTrace("Couldn't find segment for point (" + pt.getX() + ", " + pt.getY() + ")."
					   + "Width = " + getWidth() + ", Height = " + getHeight());
		
		return factoryAndCacheSegmentForPoint(pt);
	}
	
	private DartboardSegmentKt factoryAndCacheSegmentForPoint(Point pt)
	{
		String segmentKey = DartboardUtil.factorySegmentKeyForPoint(pt, centerPt, diameter);
		DartboardSegmentKt segment = hmSegmentKeyToSegment.get(segmentKey);
		if (segment == null)
		{
			segment = new DartboardSegmentKt(segmentKey);
			hmSegmentKeyToSegment.put(segmentKey, segment);
		}
		
		segment.addPoint(pt);
		hmPointToSegment.put(pt, segment);
		
		return segment;
	}
	
	/**
	 * Public methods
	 */
	public ArrayList<Point> getPointsForSegment(int score, int type)
	{
		String segmentKey = score + "_" + type;
		DartboardSegmentKt segment = hmSegmentKeyToSegment.get(segmentKey);
		return segment.getPoints();
	}
	
	public boolean isDouble(Point pt)
	{
		DartboardSegmentKt seg = getSegmentForPoint(pt);
		return seg.isDoubleExcludingBull();
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) 
	{
		Point pt = arg0.getPoint();
		dartThrown(pt);
	}
	
	public void dartThrown(Point pt)
	{
		Dart dart = convertPointToDart(pt, true);
		if (listener != null)
		{
			addDart(pt);
			
			listener.dartThrown(dart);
		}
	}
	
	public void addOverlay(Point pt, Component overlay)
	{
		add(overlay, 10);
		overlay.setLocation(pt);
	}
	
	public Dart convertPointToDart(Point pt, boolean rationalise)
	{
		if (rationalise)
		{
			rationalisePoint(pt);
		}

		DartboardSegmentKt segment = getSegmentForPoint(pt);
		return DartboardUtil.getDartForSegment(pt, segment);
	}
	
	public void rationalisePoint(Point pt)
	{
		double x = pt.getX();
		double y = pt.getY();
		
		if (x < 0)
		{
			x = 0;
		}
		
		if (y < 0)
		{
			y = 0;
		}
		
		if (x >= getWidth())
		{
			x = getWidth() - 1;
		}
		
		if (y >= getHeight())
		{
			y = getHeight() - 1;
		}
		
		pt.setLocation(x, y);
	}
	
	public double getDiameter()
	{
		return diameter;
	}
	public boolean getRenderScoreLabels()
	{
		return renderScoreLabels;
	}
	public void setRenderScoreLabels(boolean renderScoreLabels)
	{
		this.renderScoreLabels = renderScoreLabels;
	}
	
	public double getArea()
	{
		double radius = diameter / 2;
		return (radius * radius * Math.PI);
	}
	
	public void listen(boolean listen)
	{
		if (listen)
		{
			ensureListening();
		}
		else
		{
			stopListening();
		}
	}
	
	public void ensureListening()
	{
		if (dartboardLabel.getMouseListeners().length == 0)
		{
			dartboardLabel.addMouseListener(this);
			dartboardLabel.addMouseMotionListener(this);
		}
	}
	
	public void stopListening()
	{
		if (dartboardLabel.getMouseListeners().length > 0)
		{
			dartboardLabel.removeMouseListener(this);
			dartboardLabel.removeMouseMotionListener(this);
		}
		
		//Undo any colouring that there might have been
		if (lastHoveredSegment != null)
		{
			colourSegment(lastHoveredSegment, false);
		}
	}
	
	public void doForsyth()
	{
		Random rand = new Random();
		int brucey = rand.nextInt(4) + 1;
		
		doDodgy(ResourceCache.IMG_BRUCE, 300, 478, "forsyth" + brucey);
	}
	
	public void doBull()
	{
		doDodgy(ResourceCache.IMG_DEV, 400, 476, "bull");
	}
	
	public void doBadMiss()
	{
		Random rand = new Random();
		int miss = rand.nextInt(5) + 1;
		
		//4-1 ratio because mitchell > spencer!
		if (miss <= 4)
		{
			doDodgy(ResourceCache.IMG_MITCHELL, 300, 250, "badmiss" + miss);
		}
		else
		{
			doDodgy(ResourceCache.IMG_SPENCER, 460, 490, "damage");
		}
	}
	
	public void doGolfMiss()
	{
		doDodgy(ResourceCache.IMG_DEV, 400, 476, "fourTrimmed");
	}
	
	private void doDodgy(ImageIcon ii, int width, int height, String soundName)
	{
		if (!PreferenceUtil.getBooleanValue(DartsRegistry.PREFERENCES_BOOLEAN_SHOW_ANIMATIONS)
		  || simulation)
		{
			return;
		}
		
		dodgyLabel.setIcon(ii);
		dodgyLabel.setSize(width, height);
		
		int x = (getWidth() - width)/2;
		int y = (getHeight() - height);
		dodgyLabel.setLocation(x, y);
		
		remove(dodgyLabel);
		add(dodgyLabel, 0);
		
		repaint();
		revalidate();
		
		playDodgySound(soundName);
	}
	
	public void playDodgySound(String soundName)
	{
		try
		{
			if (ResourceCache.isInitialised())
			{
				playDodgySoundCached(soundName);
			}
			else
			{
				playDodgySoundAdHoc(soundName);
			}
		}
		catch (Exception e)
		{
			Debug.stackTrace(e, "Caught error playing sound [" + soundName + "]");
		}
	}
	
	@SuppressWarnings("resource")
	private void playDodgySoundCached(String soundName) throws Exception
	{
		AudioInputStream stream = ResourceCache.borrowInputStream(soundName);
		if (stream == null)
		{
			return;
		}
		
		Clip clip = initialiseAudioClip(stream, soundName);
		
		clip.open(stream);
		clip.start();
	}
	
	/**
	 * Old, ad-hoc version for playing sounds (was really slow on home PC).
	 * 
	 * Caches the URL on-the-fly, but still initialises a fresh InputStream every time.
	 */
	@SuppressWarnings("resource")
	private void playDodgySoundAdHoc(String soundName) throws Exception
	{
		URL url = hmSoundNameToUrl.get(soundName);
		if (url == null)
		{
			url = getClass().getResource("/wav/" + soundName + ".wav");
			hmSoundNameToUrl.put(soundName, url);
		}
		
		//Resource may still be null if it genuinely doesn't exist. Just return.
		if (url == null)
		{
			return;
		}
		
		Clip clip = initialiseAudioClip(null, null);

        //This is still slow. Can we cache a 'pool' of the InputStreams? Then rather than close them, call reset() and return them
        //to the pool. Need more than one in case we try to play the same sound twice.
        clip.open(AudioSystem.getAudioInputStream(url));
        clip.start();
	}
	
	private Clip initialiseAudioClip(AudioInputStream stream, String soundName) throws Exception
	{
		//Overwrite the 'clip' variable so this always stores the latest sound. 
		//Allows us to not dismiss the label until the final sound has finished, in the case of overlapping sounds.
		clip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));
        clip.addLineListener(new LineListener()
        {
            @Override
            public void update(LineEvent event)
            {
            	if (event.getType() == LineEvent.Type.STOP)
                {
            		//Always close or return our one
            		try (Clip myClip = (Clip)event.getLine();)
            		{
            			myClip.stop();
            			
            			if (ResourceCache.isInitialised())
            			{
            				ResourceCache.returnInputStream(soundName, stream);
            			}
            			else
            			{
            				//Close the ad-hoc stream
            				myClip.close();
            			}
            		}
            		
            		//See whether there's currently any clip running. If there isn't, also dismiss our dodgyLabel
            		if (!clip.isRunning())
            		{
            			remove(dodgyLabel);
            	        repaint();
            			revalidate();
            		}
                }
            }
        });
        
        return clip;
	}
	
	private void addDart(Point pt)
	{
		if (dartLabels.isEmpty())
		{
			for (int i=0; i<5; i++)
			{
				JLabel lbl = new JLabel(DARTIMG);
				lbl.setSize(76, 80);
				lbl.setVisible(false);
				add(lbl, 5-i);
				
				dartLabels.add(lbl);
			}
		}
		
		JLabel lbl = dartLabels.get(dartCount);
		lbl.setLocation(pt);
		lbl.setVisible(true);
		dartCount++;
		
		revalidate();
		repaint();
	}
	
	public void clearDarts()
	{
		//Always want to stop this at the same time
		//stopDodgy();
		
		for (int i=0; i<dartLabels.size(); i++)
		{
			JLabel dartLabel = dartLabels.get(i);
			dartLabel.setVisible(false);
		}
		
		dartCount = 0;
		revalidate();
		repaint();
		
	}
	
	public Point getCenterPoint()
	{
		return centerPt;
	}
	
	public void setSimulation(boolean simulation)
	{
		this.simulation = simulation;
	}
	
	@Override
	public void mouseMoved(MouseEvent arg0) 
	{
		highlightDartboard(arg0.getPoint());
	}
	
	@Override
	public void mousePressed(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent arg0) {}
	@Override
	public void mouseDragged(MouseEvent arg0) {}
	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}
	
	public BufferedImage factoryOverlay()
	{
		int width = getWidth();
		int height = getHeight();
		Debug.append("Overlay Dimensions: [" + width + ", " + height + "]");
		return new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
	}
}
