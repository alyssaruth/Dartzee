package burlton.dartzee.code.ai;

import burlton.core.code.obj.SuperHashMap;
import burlton.core.code.util.Debug;
import burlton.core.code.util.XmlUtil;
import burlton.dartzee.code.db.GameEntityKt;
import burlton.dartzee.code.object.Dart;
import burlton.dartzee.code.object.DartKt;
import burlton.dartzee.code.object.DartboardSegment;
import burlton.dartzee.code.screen.Dartboard;
import burlton.dartzee.code.utils.GeometryUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.util.List;
import java.util.*;

public abstract class AbstractDartsModel 
{
	//public static final int TYPE_HEURISTIC = 1;
	public static final int TYPE_NORMAL_DISTRIBUTION = 2;
	
	public static final String DARTS_MODEL_NORMAL_DISTRIBUTION = "Simple Gaussian";
	
	//Log levels
	protected static final boolean LOGGING = false;
	private static final boolean LIGHT_LOGGING = false;
	
	private static final String TAG_SETUP_DART = "SetupDart";
	private static final String TAG_GOLF_AIM = "GolfAim";
	private static final String TAG_GOLF_STOP = "GolfStop";
	private static final String ATTRIBUTE_SCORING_DART = "ScoringDart";
	private static final String ATTRIBUTE_MERCY_RULE = "MercyRule";
	//private static final String ATTRIBUTE_HORIZONTAL_BIAS = "HorizontalBias";
	//private static final String ATTRIBUTE_VERTICAL_BIAS = "VerticalBias";
	private static final String ATTRIBUTE_SCORE = "Score";
	private static final String ATTRIBUTE_DART_VALUE = "DartValue";
	private static final String ATTRIBUTE_DART_MULTIPLIER = "DartMultiplier";
	private static final String ATTRIBUTE_DART_NUMBER = "DartNumber";
	private static final String ATTRIBUTE_SEGMENT_TYPE = "SegmentType";
	private static final String ATTRIBUTE_STOP_THRESHOLD = "StopThreshold";
	
	private static final int SCORING_DARTS_TO_THROW = 20000;
	private static final int DOUBLE_DARTS_TO_THROW = 20000;
	
	private int scoringDart = 20;
	
	//X01
	private SuperHashMap<Integer, Dart> hmScoreToDart = new SuperHashMap<>();
	private int mercyThreshold = -1;
	
	//Golf
	private SuperHashMap<Integer, Integer> hmDartNoToSegmentType = new SuperHashMap<>();
	private SuperHashMap<Integer, Integer> hmDartNoToStopThreshold = new SuperHashMap<>();
	
	private boolean logging = LIGHT_LOGGING;

	/**
	 * Static methods
	 */
	public static AbstractDartsModel factoryForType(int type)
	{
		if (type == TYPE_NORMAL_DISTRIBUTION)
		{
			return new DartsModelNormalDistribution();
		}
		else
		{
			Debug.stackTrace("Unexpected type: " + type);
			return null;
		}
	}
	
	public static Vector<String> getModelDescriptions()
	{
		Vector<String> models = new Vector<>();
		
		models.add(DARTS_MODEL_NORMAL_DISTRIBUTION);
		
		return models;
	}
	
	public static String getStrategyDesc(int type)
	{
		if (type == TYPE_NORMAL_DISTRIBUTION)
		{
			return DARTS_MODEL_NORMAL_DISTRIBUTION;
		}
		else
		{
			Debug.stackTrace("Unexpected type: " + type);
			return null;
		}
	}
	
	/**
	 * Abstract methods
	 */
	public abstract String getModelName();
	public abstract void writeXmlSpecific(Element rootElement);
	public abstract void readXmlSpecific(Element root);
	public abstract Point throwDartAtPoint(Point pt, Dartboard dartboard);
	public abstract int getType();
	public abstract double getProbabilityWithinRadius(double radius);
	
	/**
	 * Non-abstract stuff
	 */
	public void readXml(String xmlStr)
	{
		Document xmlDoc = XmlUtil.getDocumentFromXmlString(xmlStr);
		Element rootElement = xmlDoc.getDocumentElement();
		
		int scoringSingle = XmlUtil.getAttributeInt(rootElement, ATTRIBUTE_SCORING_DART);
		if (scoringSingle > 0)
		{
			this.scoringDart = scoringSingle;
		}
		
		//horizontalBias = XmlUtil.getAttributeDouble(rootElement, ATTRIBUTE_HORIZONTAL_BIAS);
		//verticalBias = XmlUtil.getAttributeDouble(rootElement, ATTRIBUTE_VERTICAL_BIAS);
		
		//X01
		mercyThreshold = XmlUtil.getAttributeInt(rootElement, ATTRIBUTE_MERCY_RULE, -1);
		
		hmScoreToDart = new SuperHashMap<>();
		NodeList setupDarts = rootElement.getElementsByTagName(TAG_SETUP_DART);
		int size = setupDarts.getLength();
		for (int i=0; i<size; i++)
		{
			Element setupDart = (Element)setupDarts.item(i);
			int score = XmlUtil.getAttributeInt(setupDart, ATTRIBUTE_SCORE);
			int value = XmlUtil.getAttributeInt(setupDart, ATTRIBUTE_DART_VALUE);
			int multiplier = XmlUtil.getAttributeInt(setupDart, ATTRIBUTE_DART_MULTIPLIER);
			
			hmScoreToDart.put(score, new Dart(value, multiplier));
		}
		
		//Golf
		hmDartNoToSegmentType = XmlUtil.readIntegerHashMap(rootElement, TAG_GOLF_AIM, ATTRIBUTE_DART_NUMBER, ATTRIBUTE_SEGMENT_TYPE);
		hmDartNoToStopThreshold = XmlUtil.readIntegerHashMap(rootElement, TAG_GOLF_STOP, ATTRIBUTE_DART_NUMBER, ATTRIBUTE_STOP_THRESHOLD);
		
		readXmlSpecific(rootElement);
	}
	
	public String writeXml()
	{
		Document xmlDoc = XmlUtil.factoryNewDocument();
		Element rootElement = xmlDoc.createElement(getModelName());
		
		if (scoringDart != 20)
		{
			rootElement.setAttribute(ATTRIBUTE_SCORING_DART, "" + scoringDart);
		}
		
		Iterator<Integer> it = hmScoreToDart.keySet().iterator();
		for (; it.hasNext(); )
		{
			int score = it.next();
			Dart drt = hmScoreToDart.get(score);
			
			Element child = xmlDoc.createElement(TAG_SETUP_DART);
			child.setAttribute(ATTRIBUTE_SCORE, "" + score);
			child.setAttribute(ATTRIBUTE_DART_VALUE, "" + drt.getScore());
			child.setAttribute(ATTRIBUTE_DART_MULTIPLIER, "" + drt.getMultiplier());
			rootElement.appendChild(child);
		}
		
		XmlUtil.writeHashMap(hmDartNoToSegmentType, xmlDoc, rootElement, TAG_GOLF_AIM, ATTRIBUTE_DART_NUMBER, ATTRIBUTE_SEGMENT_TYPE);
		XmlUtil.writeHashMap(hmDartNoToStopThreshold, xmlDoc, rootElement, TAG_GOLF_STOP, ATTRIBUTE_DART_NUMBER, ATTRIBUTE_STOP_THRESHOLD);
		
		if (mercyThreshold > -1)
		{
			rootElement.setAttribute(ATTRIBUTE_MERCY_RULE, "" + mercyThreshold);
		}
		
		writeXmlSpecific(rootElement);
		xmlDoc.appendChild(rootElement);
		
		return XmlUtil.getStringFromDocument(xmlDoc);
	}
	
	/**
	 * X01
	 */
	public void throwX01Dart(int score, Dartboard dartboard)
	{
		Point pt = getX01Dart(score, dartboard);
		dartboard.dartThrown(pt);
	}
	private Point getX01Dart(int score, Dartboard dartboard)
	{
		Debug.append("Score = " + score, logging);
		
		//Check for a specific dart to aim for. It's possible to override any value for a specific AI strategy.
		Dart drtToAimAt = getOveriddenDartToAimAt(score);
		if (drtToAimAt != null)
		{
			Point ptToAimAt = getPointForScore(drtToAimAt, dartboard);
			return throwDartAtPoint(ptToAimAt, dartboard);
		}
		
		//No overridden strategy, do the default thing
		if (score > 60)
		{
			return throwScoringDart(dartboard);
		}
		else
		{
			Dart defaultDrt = getDefaultDartToAimAt(score);
			Point ptToAimAt = getPointForScore(defaultDrt, dartboard);
			return throwDartAtPoint(ptToAimAt, dartboard);
		}
	}
	private Point throwScoringDart(Dartboard dartboard)
	{
		Point ptToAimAt = getScoringPoint(dartboard);
		return throwDartAtPoint(ptToAimAt, dartboard);
	}
	public Point getScoringPoint(Dartboard dartboard)
	{
		if (scoringDart == 25)
		{
			return getPointForScoreAdjustedForBias(scoringDart, dartboard, DartboardSegment.SEGMENT_TYPE_DOUBLE);
		}
		else
		{
			return getPointForScoreAdjustedForBias(scoringDart, dartboard, DartboardSegment.SEGMENT_TYPE_TREBLE);
		}
	}
	private Dart getOveriddenDartToAimAt(int score)
	{
		Dart customDrt = hmScoreToDart.get(score);
		if (customDrt != null)
		{
			return customDrt;
		}
		
		return null;
	}
	
	/**
	 * Golf
	 */
	public void throwGolfDart(int targetHole, int dartNo, Dartboard dartboard)
	{
		int segmentTypeToAimAt = getSegmentTypeForDartNo(dartNo);
		Point ptToAimAt = getPointForScoreAdjustedForBias(targetHole, dartboard, segmentTypeToAimAt);
		Point pt = throwDartAtPoint(ptToAimAt, dartboard);
		dartboard.dartThrown(pt);
	}
	private int getDefaultSegmentType(int dartNo)
	{
		if (dartNo == 1)
		{
			return DartboardSegment.SEGMENT_TYPE_DOUBLE;
		}
		
		return DartboardSegment.SEGMENT_TYPE_TREBLE;
	}
	private int getDefaultStopThreshold(int dartNo)
	{
		if (dartNo == 2)
		{
			//3 or better with second dart
			return 3;
		}
		
		//2 or better with the first dart
		return 2;
	}
	
	/**
	 * Clock
	 */
	public void throwClockDart(int clockTarget, String clockType, Dartboard dartboard)
	{
		int segmentType = getSegmentTypeForClockType(clockType);
	
		Point ptToAimAt = getPointForScoreAdjustedForBias(clockTarget, dartboard, segmentType);
		Point pt = throwDartAtPoint(ptToAimAt, dartboard);
		dartboard.dartThrown(pt);
	}
	private int getSegmentTypeForClockType(String clockType)
	{
		if (clockType.equals(GameEntityKt.CLOCK_TYPE_STANDARD))
		{
			return DartboardSegment.SEGMENT_TYPE_OUTER_SINGLE;
		}
		else if (clockType.equals(GameEntityKt.CLOCK_TYPE_DOUBLES))
		{
			return DartboardSegment.SEGMENT_TYPE_DOUBLE;
		}
		else
		{
			return DartboardSegment.SEGMENT_TYPE_TREBLE;
		}
	}
	
	
	/**
	 * Get the application-wide default thing to aim for, which applies to any score of 60 or less
	 */
	public static Dart getDefaultDartToAimAt(int score)
	{
		if (score > 60)
		{
			Debug.stackTrace("Trying to get strategy-invariant default for score over 60. This will not work.");
			return DartKt.factoryTreble(20);
		}
		
		//Aim for the single that puts you on double top
		if (score > 40)
		{
			int single = score - 40;
			return DartKt.factorySingle(single);
		}
		
		//Aim for the double
		if ((score % 2) == 0)
		{
			return DartKt.factoryDouble(score/2);
		}
		
		//On an odd number, less than 40. Aim to put ourselves on the highest possible power of 2.
		int scoreToLeaveRemaining = getHighestPowerOfTwoLessThan(score);
		int singleToAimFor = score - scoreToLeaveRemaining;
		return DartKt.factorySingle(singleToAimFor);
	}
	private static int getHighestPowerOfTwoLessThan(int score)
	{
		int ret = -1;
		for (int i=2; i<score; i=i*2)
		{
			ret = i;
		}
		
		return ret;
	}
	
	/**
	 * Given the single/double/treble required, calculate the physical coordinates of the optimal place to aim
	 */
	private Point getPointForScore(Dart drt, Dartboard dartboard)
	{
		int score = drt.getScore();
		int segmentType = drt.getSegmentTypeToAimAt();
		return getPointForScoreAdjustedForBias(score, dartboard, segmentType);
	}
	private Point getPointForScoreAdjustedForBias(int score, Dartboard dartboard, int type)
	{
		int multiplier = DartboardSegment.getMultiplier(type);
		Debug.append("Aiming for " + new Dart(score, multiplier), logging);
		
		List<Point> points = dartboard.getPointsForSegment(score, type);
		
		//Don't get the average point, pick a random one
		Point avgPoint = GeometryUtil.getAverage(points);
		//int size = points.size();
		//Random rand = new Random();
		//int ix = rand.nextInt(size);
		//Point avgPoint = points.get(ix);
		
		//double xAdjusted = avgPoint.getX() + horizontalBias;
		//double yAdjusted = avgPoint.getY() - verticalBias;
		
		//avgPoint.setLocation(xAdjusted, yAdjusted);
		
		//Need to rationalise here as we may have adjusted outside of the bounds
		//Shouldn't need this anymore, but can't hurt to leave it here anyway!
		dartboard.rationalisePoint(avgPoint);
		
		return avgPoint;
	}
	
	public SimulationWrapper runSimulation(Dartboard dartboard)
	{
		//Turn this off before we start
		logging = false;
		
		Debug.append("Simulating scoring throws for " + SCORING_DARTS_TO_THROW + " darts");
		
		HashMap<Point, Integer> hmPointToCount = new HashMap<>();
		
		double avgScore = 0;
		double missPercent = 0;
		double treblePercent = 0;
		
		//Get the scoring point once, not 20,000 times
		//Point ptToAimAt = getScoringPoint(dartboard);
		for (int i=0; i<SCORING_DARTS_TO_THROW; i++)
		{
			Point ptToAimAt = getScoringPoint(dartboard);
			
			Point pt = throwDartAtPoint(ptToAimAt, dartboard);
			dartboard.rationalisePoint(pt);
			
			Integer currentCount = hmPointToCount.get(pt);
			if (currentCount == null)
			{
				hmPointToCount.put(pt, Integer.valueOf(1));
			}
			else
			{
				int newCount = currentCount.intValue() + 1;
				hmPointToCount.put(pt, Integer.valueOf(newCount));
			}
			
			Dart dart = dartboard.convertPointToDart(pt, false);
			avgScore += dart.getTotal();
			
			if (dart.getTotal() == 0)
			{
				missPercent++;
			}
			
			if (dart.getMultiplier() == 3
			  && dart.getScore() == (scoringDart))
			{
				treblePercent++;
			}
		}
		
		avgScore = avgScore/SCORING_DARTS_TO_THROW;
		missPercent = 100 * missPercent/SCORING_DARTS_TO_THROW;
		treblePercent = 100 * treblePercent/SCORING_DARTS_TO_THROW;
		
		Debug.append("Simulating throws at random doubles for " + DOUBLE_DARTS_TO_THROW + " darts");
		
		double doublesHit = 0;
		//HashMap<Integer, Point> hm = getHmOfDoublePoints(dartboard);
		Random rand = new Random();
		for (int i=0; i<DOUBLE_DARTS_TO_THROW; i++)
		{
			int doubleToAimAt = rand.nextInt(20) + 1;
			
			//Point doublePtToAimAt = hm.get(doubleToAimAt);
			Point doublePtToAimAt = getPointForScoreAdjustedForBias(doubleToAimAt, dartboard, DartboardSegment.SEGMENT_TYPE_DOUBLE);
			
			Point pt = throwDartAtPoint(doublePtToAimAt, dartboard);
			Dart dart = dartboard.convertPointToDart(pt, true);
			
			if (dart.getTotal() == (doubleToAimAt * 2)
			  && dart.isDouble())
			{
				doublesHit++;
			}
		}
		
		Debug.append("Finished simulation");
		
		double doublePercent = 100 * doublesHit/DOUBLE_DARTS_TO_THROW;
		return new SimulationWrapper(avgScore, missPercent, doublePercent, treblePercent, hmPointToCount);
	}
	/*private HashMap<Integer, Point> getHmOfDoublePoints(Dartboard dartboard)
	{
		HashMap<Integer, Point> ret = new HashMap<>();
		for (int i=1; i<=20; i++)
		{
			Point doublePtToAimAt = getPointForScoreAdjustedForBias(i, dartboard, DartboardSegment.SEGMENT_TYPE_DOUBLE);
			ret.put(i, doublePtToAimAt);
		}
		
		return ret;
	}*/
	
	/**
	 * Gets / Sets
	 */
	public SuperHashMap<Integer, Dart> getHmScoreToDart()
	{
		return hmScoreToDart;
	}
	public void setHmScoreToDart(SuperHashMap<Integer, Dart> hmScoreToDart)
	{
		this.hmScoreToDart = hmScoreToDart;
	}
	public int getScoringDart()
	{
		return scoringDart;
	}
	public void setScoringDart(int scoringDart)
	{
		this.scoringDart = scoringDart;
	}
	/*public double getHorizontalBias()
	{
		return horizontalBias;
	}
	public void setHorizontalBias(double horizontalBias)
	{
		this.horizontalBias = horizontalBias;
	}
	public double getVerticalBias()
	{
		return verticalBias;
	}
	public void setVerticalBias(double verticalBias)
	{
		this.verticalBias = verticalBias;
	}*/
	public int getMercyThreshold()
	{
		return mercyThreshold;
	}
	public void setMercyThreshold(int mercyThreshold)
	{
		this.mercyThreshold = mercyThreshold;
	}
	public int getSegmentTypeForDartNo(int dartNo)
	{
		return hmDartNoToSegmentType.getOrDefault(dartNo, getDefaultSegmentType(dartNo));
	}
	public void setSegmentTypeForDartNo(int dartNo, int segmentType)
	{
		hmDartNoToSegmentType.put(dartNo, segmentType);
	}
	public int getStopThresholdForDartNo(int dartNo)
	{
		return hmDartNoToStopThreshold.getOrDefault(dartNo, getDefaultStopThreshold(dartNo));
	}
	public void setStopThresholdForDartNo(int dartNo, int stopThreshold)
	{
		hmDartNoToStopThreshold.put(dartNo, stopThreshold);
	}
}
