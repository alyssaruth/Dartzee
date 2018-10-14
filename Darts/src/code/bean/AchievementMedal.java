package code.bean;

import java.awt.Point;
import java.util.Timer;
import java.util.TimerTask;

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
	private static final int INNER_RADIUS = 80;
	
	private int currentAngle = 0;
	private int finalAngle = 350;
	
	public AchievementMedal() 
	{
		setSize(180, 180);
		
		animateProgressBar();
	}
	
	private void animateProgressBar()
	{
		Timer timer = new Timer("Timer-Achievement");
		currentAngle = 0;
		
		for (int i=1; i<=finalAngle; i+=4)
		{
			timer.schedule(new IncrementProgressBar(), 5 * i);
		}
	}
	
	/**
	 * ArcTo always goes anti-clockwise, as far as I can tell.
	 * So this uses MoveTos to get around this unfortunate fact.
	 */
	private Path drawSemiRing(Color fill, double degrees) 
	{
		Point centerPt = new Point(CENTER_X, CENTER_Y);
        Point innerArcTo = GeometryUtil.translatePoint(centerPt, INNER_RADIUS, degrees, false);
        Point outerArcTo = GeometryUtil.translatePoint(centerPt, OUTER_RADIUS, degrees, false);
		
        Path path = new Path();
        path.setFill(fill);
        path.setStroke(fill);
        path.setFillRule(FillRule.EVEN_ODD);
        
        //Move to where the inner arc goes to (e.g. 45 degrees around from the top of the circle).
        MoveTo moveTo = new MoveTo(innerArcTo.getX(), innerArcTo.getY());
        path.getElements().add(moveTo);
        
        //Draw an arc back up to the top.
        arcPathBackToTop(path, degrees, INNER_RADIUS);
        
        //Move back to where the inner arc goes to
        path.getElements().add(moveTo);
        
        //Draw the line out to the outer arc
        LineTo lineTo = new LineTo(outerArcTo.getX(), outerArcTo.getY());
        path.getElements().add(lineTo);
        
        //Draw the larger arc back up to the top
        arcPathBackToTop(path, degrees, OUTER_RADIUS);
        
        //Draw the vertical line down to join up the top two points
        LineTo lineTo2 = new LineTo(CENTER_X, CENTER_Y - INNER_RADIUS);
        path.getElements().add(lineTo2);

        return path;
    }
	private void arcPathBackToTop(Path path, double degrees, int radiusToUse)
	{
		if (degrees <= 180)
        {
	        ArcTo arcToInner = new ArcTo();
	        arcToInner.setX(CENTER_X);
	        arcToInner.setY(CENTER_Y - radiusToUse);
	        arcToInner.setRadiusX(radiusToUse);
	        arcToInner.setRadiusY(radiusToUse);
	        
	        path.getElements().add(arcToInner);
        }
        else
        {
        	ArcTo miniArc = new ArcTo();
        	miniArc.setRadiusX(radiusToUse);
        	miniArc.setRadiusY(radiusToUse);
        	miniArc.setX(CENTER_X);
        	miniArc.setY(CENTER_Y + radiusToUse);
        	
        	ArcTo semiArc = new ArcTo();
        	semiArc.setRadiusX(radiusToUse);
        	semiArc.setRadiusY(radiusToUse);
        	semiArc.setX(CENTER_X);
        	semiArc.setY(CENTER_Y - radiusToUse);
        	
        	path.getElements().add(miniArc);
        	path.getElements().add(semiArc);
        }
	}
	
	
	private class IncrementProgressBar extends TimerTask
	{
		@Override
		public void run() 
		{
			if (isVisible())
			{
				if (currentAngle < finalAngle)
				{
					currentAngle+=4;
				}
				
				Color color = null;
				if (currentAngle < 10)
				{
					color = Color.GREY;
				}
				else if (currentAngle < 25)
				{
					color = Color.RED;
				}
				else if (currentAngle < 50)
				{
					color = Color.ORANGE;
				}
				else if (currentAngle < 90)
				{
					color = Color.YELLOW;
				}
				else if (currentAngle < 170)
				{
					color = Color.GREEN;
				}
				else if (currentAngle < 270)
				{
					color = Color.CYAN;
				}
				else
				{
					color = Color.PINK;
				}
				
				Path p = drawSemiRing(color, currentAngle);
			    setScene(new Scene(new Group(p), 180, 180));
			}
		}
	}
	
}
