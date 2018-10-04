package code.ai;

import java.awt.Point;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.w3c.dom.Element;

import code.screen.Dartboard;
import code.utils.GeometryUtil;
import util.Debug;
import util.XmlUtil;

public class DartsModelNormalDistribution extends AbstractDartsModel
{
	private static final String ATTRIBUTE_STANDARD_DEVIATION = "StandardDeviation";
	private static final String ATTRIBUTE_STANDARD_DEVIATION_DOUBLES = "StandardDeviationDoubles";
	private static final String ATTRIBUTE_STANDARD_DEVIATION_CENTRAL = "StandardDeviationCentral";
	private static final String ATTRIBUTE_RADIUS_AVERAGE_COUNT = "RadiusAverageCount";
	
	private int mean = 0;
	private double standardDeviation = 50;
	private double standardDeviationDoubles = -1;
	private double standardDeviationCentral = -1;
	private NormalDistribution distribution = null;
	private NormalDistribution distributionDoubles = null;
	private int radiusAverageCount = 1;
	
	@Override
	public Point throwDartAtPoint(Point pt, Dartboard dartboard)
	{
		Debug.append("Throwing dart at " + pt, LOGGING);
		if (standardDeviation == 0)
		{
			Debug.stackTrace("Gaussian model with SD of 0 - this shouldn't be possible!");
			return pt;
		}
		
		
		/*NormalDistribution distribution = getDistributionToUse(pt, dartboard);
		double radius = Math.abs(distribution.sample());
		
		//Eliminate outliers by retaking?
		if (radius > (1.5 * standardDeviation))
		{
			//Retry
			radius = Math.abs(distribution.sample());
		}*/
		
		//Take an average... but this doesn't seem to work
		double radius = 0;
		for (int i=0; i<radiusAverageCount; i++)
		{
			NormalDistribution distribution = getDistributionToUse(pt, dartboard);
			radius += Math.abs(distribution.sample());
		}

		radius = radius/radiusAverageCount;
		
		//Generate the angle
		double theta = generateAngle(pt, dartboard);
		
		if (theta < 0)
		{
			theta = theta + 360;
		}
		else if (theta > 360)
		{
			theta = theta - 360;
		}
	
		Debug.appendWithoutDate("Radius = " + radius + ", theta = " + theta, LOGGING);
		
		return GeometryUtil.translatePoint(pt, radius, theta, LOGGING);
	}
	private NormalDistribution getDistributionToUse(Point pt, Dartboard dartboard)
	{
		if (dartboard.isDouble(pt)
		  && distributionDoubles != null)
		{
			return distributionDoubles;
		}
		
		return distribution;
	}
	private double generateAngle(Point pt, Dartboard dartboard)
	{
		if (dartboard.isDouble(pt)
		  || standardDeviationCentral == 0)
		{
			//Just pluck a number from 0-360.
			return GeometryUtil.generateRandomAngle();
		}
		
		//Otherwise, we have a Normal Distribution to use to generate an angle more likely to be into the dartboard (rather than out of it)
		double angleToAvoid = GeometryUtil.getAngleForPoint(pt, dartboard.getCenterPoint());
		double angleTowardsCenter = (angleToAvoid + 180) % 360;
		NormalDistribution angleDistribution = new NormalDistribution(angleTowardsCenter, standardDeviationCentral);
		return angleDistribution.sample();
	}

	@Override
	public String getModelName()
	{
		return "Gaussian";
	}

	@Override
	public int getType()
	{
		return AbstractDartsModel.TYPE_NORMAL_DISTRIBUTION;
	}
	
	@Override
	public void writeXmlSpecific(Element rootElement)
	{
		rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION, "" + standardDeviation);
		
		if (standardDeviationDoubles > 0)
		{
			rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES, "" + standardDeviationDoubles);
		}
		
		if (standardDeviationCentral > 0)
		{
			rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL, "" + standardDeviationCentral);
		}
		
		rootElement.setAttribute(ATTRIBUTE_RADIUS_AVERAGE_COUNT, "" + radiusAverageCount);
	}

	@Override
	public void readXmlSpecific(Element root)
	{
		double sd = XmlUtil.getAttributeDouble(root, ATTRIBUTE_STANDARD_DEVIATION);
		double sdDoubles = XmlUtil.getAttributeDouble(root, ATTRIBUTE_STANDARD_DEVIATION_DOUBLES);
		double sdCentral = XmlUtil.getAttributeDouble(root, ATTRIBUTE_STANDARD_DEVIATION_CENTRAL);
		int radiusAverageCount = XmlUtil.getAttributeInt(root, ATTRIBUTE_RADIUS_AVERAGE_COUNT, 1);
		
		populate(sd, sdDoubles, sdCentral, radiusAverageCount);
	}
	
	@Override
	public double getProbabilityWithinRadius(double radius)
	{
		return distribution.probability(-radius, radius);
	}
	
	public void populate(double standardDeviation, double standardDeviationDoubles, double standardDeviationCentral,
	  int radiusAverageCount)
	{
		this.standardDeviation = standardDeviation;
		this.standardDeviationDoubles = standardDeviationDoubles;
		this.standardDeviationCentral = standardDeviationCentral;
		this.radiusAverageCount = radiusAverageCount;
		
		distribution = new NormalDistribution(mean, standardDeviation);
		if (standardDeviationDoubles > 0)
		{
			distributionDoubles = new NormalDistribution(mean, standardDeviationDoubles);
		}
		else
		{
			distributionDoubles = null;
		}
	}
	
	/**
	 * Gets / sets
	 */
	public double getStandardDeviation()
	{
		return standardDeviation;
	}
	public double getStandardDeviationDoubles()
	{
		return standardDeviationDoubles;
	}
	public double getStandardDeviationCentral()
	{
		return standardDeviationCentral;
	}
	public int getRadiusAverageCount()
	{
		return radiusAverageCount;
	}
}
