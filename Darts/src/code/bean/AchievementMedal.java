package code.bean;

import java.awt.Point;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import code.utils.GeometryUtil;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 * Class to contain the logic for the actual medals displayed for achievements.
 */
public class AchievementMedal extends JFXPanel
{
	private static final int CENTER_X = 90;
	private static final int CENTER_Y = 90;
	private static final int OUTER_RADIUS = 85;
	private static final int INNER_RADIUS = 75;
	
	private double currentAngle = 0;
	private double finalAngle = 358;
	private Color color = null;
	
	private int millisBetweenSteps = -1;
	
	private Timer timer = new Timer("Timer-Achievement");
	
	private double increment = 0;
	
	public AchievementMedal(int angle, Color color) 
	{
		setSize(200, 200);
		
		this.finalAngle = angle;
		this.color = color;
		
		animateProgressBar();
	}

	public void animateProgressBar()
	{
		timer.cancel();
		timer = new Timer("Timer-Achievement");
		
		currentAngle = 0;
		
		//Let's say we want them to take 2 seconds to draw.
		//Work out how much to increase by
		increment = Math.max(finalAngle/30, 1);
		
		//Work out how many increments there will be...
		int animationSteps = (int)Math.ceil(finalAngle/increment);
		millisBetweenSteps = 1000/animationSteps;
		
		//Schedule the 1st immediately
		timer.schedule(new IncrementProgressBar(), 0);
	}
	
	/**
	 * ArcTo always goes anti-clockwise, as far as I can tell.
	 * So this uses MoveTos to get around this unfortunate fact.
	 */
	private Path drawPartialRing(double degrees) 
	{
		Point centerPt = new Point(CENTER_X, CENTER_Y);
        Point innerArcTo = GeometryUtil.translatePoint(centerPt, INNER_RADIUS, degrees, false);
        Point outerArcTo = GeometryUtil.translatePoint(centerPt, OUTER_RADIUS, degrees, false);
        
        if (degrees == 360)
        {
        	innerArcTo = new Point(CENTER_X, CENTER_Y - INNER_RADIUS);
        	outerArcTo = new Point(CENTER_X, CENTER_Y - OUTER_RADIUS);
        }
		
        Path path = new Path();
        path.setFill(color);
        path.setStroke(Color.BLACK);
        path.setFillRule(FillRule.EVEN_ODD);
        
        //path.setEffect(new Lighting());
        
        //Move to where the inner arc goes to (e.g. 45 degrees around from the top of the circle).
        MoveTo moveTo = new MoveTo(innerArcTo.getX(), innerArcTo.getY());
        path.getElements().add(moveTo);
        
        //Draw an arc back up to the top.
        arcPathBackToTop(path, degrees, INNER_RADIUS);
        
        //Move back to where the inner arc goes to
        path.getElements().add(moveTo);
        
        //Draw the line out to the outer arc
        if (degrees < 360)
        {
	        LineTo lineTo = new LineTo(outerArcTo.getX(), outerArcTo.getY());
	        path.getElements().add(lineTo);
        }
        else
        {
        	MoveTo moveToOuter = new MoveTo(outerArcTo.getX(), outerArcTo.getY());
        	path.getElements().add(moveToOuter);
        }
        
        //Draw the larger arc back up to the top
        arcPathBackToTop(path, degrees, OUTER_RADIUS);
        
        //Draw the vertical line down to join up the top two points
        if (degrees < 360)
        {
	        LineTo lineTo2 = new LineTo(CENTER_X, CENTER_Y - INNER_RADIUS);
	        path.getElements().add(lineTo2);
        }

        return path;
    }
	private void arcPathBackToTop(Path path, double degrees, int radiusToUse)
	{
		if (degrees > 270)
		{
			ArcTo arcTo270 = new ArcTo(radiusToUse, radiusToUse, 0, CENTER_X - radiusToUse, CENTER_Y, false, false);
			path.getElements().add(arcTo270);
		}
		
		if (degrees > 180)
		{
			ArcTo arcTo180 = new ArcTo(radiusToUse, radiusToUse, 0, CENTER_X, CENTER_Y + radiusToUse, false, false);
			path.getElements().add(arcTo180);
		}
		
		if (degrees > 90)
		{
			ArcTo arcTo90 = new ArcTo(radiusToUse, radiusToUse, 0, CENTER_X + radiusToUse, CENTER_Y, false, false);
			path.getElements().add(arcTo90);
		}
		
		ArcTo arcToTop = new ArcTo(radiusToUse, radiusToUse, 0, CENTER_X, CENTER_Y - radiusToUse, false, false);
		path.getElements().add(arcToTop);
	}
	
	
	private class IncrementProgressBar extends TimerTask
	{
		@Override
		public void run() 
		{
			if (currentAngle < finalAngle)
			{
				currentAngle += increment;
				currentAngle = Math.min(currentAngle, finalAngle);
			}
			
			Path p = drawPartialRing(currentAngle);
			Scene scene = new Scene(new Group(p), 180, 180);
			
			scene.setFill(Color.TRANSPARENT);
			
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					setScene(scene);
				    
				    if (currentAngle == finalAngle)
				    {
				    	timer.cancel();
				    }
				    else
				    {
				    	timer.schedule(new IncrementProgressBar(), millisBetweenSteps);
				    }
				}
			});
		    
		}
	}
	
}
