package code.screen;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SwingConstants;

import code.listener.DartboardListener;
import code.object.ColourWrapper;
import code.object.Dart;
import code.object.DartboardSegment;
import code.screen.game.GamePanelRoundTheClock;
import code.screen.game.GamePanelX01;
import code.utils.DartboardUtil;
import code.utils.DartsColour;
import code.utils.DartsRegistry;
import code.utils.GeometryUtil;
import code.utils.PreferenceUtil;
import object.SuperHashMap;
import util.Debug;

public class Dartboard extends JLayeredPane
					   implements MouseListener,
					   MouseMotionListener
{
	private static final ImageIcon DARTIMG = new ImageIcon(GamePanelX01.class.getResource("/dartImage.png"));
	
	private static SuperHashMap<String, ImageIcon> hmResourceNameToImage = new SuperHashMap<>();
	private static SuperHashMap<String, URL> hmSoundNameToUrl = new SuperHashMap<>();
	
	private SuperHashMap<Point, DartboardSegment> hmPointToSegment = new SuperHashMap<>();
	private SuperHashMap<String, DartboardSegment> hmSegmentKeyToSegment = new SuperHashMap<>();
	
	private ArrayList<JLabel> dartLabels = new ArrayList<>();
	
	private DartboardListener listener = null;
	private Point centerPt = new Point(200, 200);
	private double diameter = 360;
	private boolean renderScoreLabels = false;
	
	private int dartCount = 0;
	
	private boolean simulation = false;
	
	private Clip clip = null;
	
	//Cached things
	private DartboardSegment lastHoveredSegment = null;
	private ColourWrapper colourWrapper = null;
	
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
	        	
	        	DartboardSegment segment = getSegmentForPoint(pt);
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
		DartboardSegment hoveredSegment = getSegmentForPoint(hoveredPoint);
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
	private void colourSegment(DartboardSegment segment, boolean highlight)
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
	
	private DartboardSegment getSegmentForPoint(Point pt)
	{
		DartboardSegment segment = hmPointToSegment.get(pt);
		if (segment != null)
		{
			return segment;
		}
		
		Debug.stackTrace("Couldn't find segment for point (" + pt.getX() + ", " + pt.getY() + ")."
					   + "Width = " + getWidth() + ", Height = " + getHeight());
		
		return factoryAndCacheSegmentForPoint(pt);
	}
	
	private DartboardSegment factoryAndCacheSegmentForPoint(Point pt)
	{
		String segmentKey = DartboardUtil.factorySegmentKeyForPoint(pt, centerPt, diameter);
		DartboardSegment segment = hmSegmentKeyToSegment.get(segmentKey);
		if (segment == null)
		{
			segment = new DartboardSegment(segmentKey);
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
		DartboardSegment segment = hmSegmentKeyToSegment.get(segmentKey);
		return segment.getPoints();
	}
	
	public boolean isDouble(Point pt)
	{
		DartboardSegment seg = getSegmentForPoint(pt);
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
	
	public Dart convertPointToDart(Point pt, boolean rationalise)
	{
		if (rationalise)
		{
			rationalisePoint(pt);
		}
		
		DartboardSegment segment = getSegmentForPoint(pt);
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
		
		doDodgy("forsyth1", 300, 478, "forsyth" + brucey);
	}
	
	public void doBull()
	{
		doDodgy("dev", 400, 476, "bull");
	}
	
	public void doBadMiss()
	{
		Random rand = new Random();
		int miss = rand.nextInt(5) + 1;
		
		//4-1 ratio because mitchell > spencer!
		if (miss <= 4)
		{
			doDodgy("mitchell", 300, 250, "badmiss" + miss);
		}
		else
		{
			doDodgy("spencer", 460, 490, "damage");
		}
	}
	
	public void doGolfMiss()
	{
		doDodgy("dev", 400, 476, "fourTrimmed");
	}
	
	private void doDodgy(String imageName, int width, int height, String soundName)
	{
		if (!PreferenceUtil.getBooleanValue(DartsRegistry.PREFERENCES_BOOLEAN_SHOW_ANIMATIONS)
		  || simulation)
		{
			return;
		}
		
		//On-the-fly caching for the ImageIcons, so we're not delving into the JAR all the time
		ImageIcon ii = hmResourceNameToImage.get(imageName);
		if (ii == null)
		{
			ii = new ImageIcon(GamePanelRoundTheClock.class.getResource("/horrific/" + imageName + ".png"));
			hmResourceNameToImage.put(imageName, ii);
		}
		
		dodgyLabel.setIcon(ii);
		dodgyLabel.setSize(width, height);
		
		int x = (getWidth() - width)/2;
		int y = (getHeight() - height);
		dodgyLabel.setLocation(x, y);
		
		remove(dodgyLabel);
		add(dodgyLabel, -1);
		moveToFront(dodgyLabel);
		
		repaint();
		revalidate();
		
		playDodgySound(soundName);
	}
	
	public void playDodgySound(String soundName)
	{
		try
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
			
			clip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));
	        clip.addLineListener(new LineListener()
	        {
	            @Override
	            public void update(LineEvent event)
	            {
	            	if (event.getType() == LineEvent.Type.STOP)
	                {
	            		//Always close our one
	            		try (Clip myClip = (Clip)event.getLine();)
	            		{
	            			myClip.stop();
	            			myClip.close();
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

	        //This is still slow. Can we cache a 'pool' of the InputStreams? Then rather than close them, call reset() and return them
	        //to the pool. Need more than one in case we try to play the same sound twice.
	        clip.open(AudioSystem.getAudioInputStream(url));
	        clip.start();
	    }
	    catch (Exception exc)
	    {
	        Debug.stackTrace(exc);
	    }
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
				add(lbl, i);
				
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
