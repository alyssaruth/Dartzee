package burlton.dartzee.code.screen.stats.player;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.ui.RectangleEdge;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Adapting the source code from BoxAndWhiskerRenderer to fix a few bugs with the horizontal layout
 * and make some customisations.
 *
 */
public class FixedBoxAndWhiskerRenderer extends BoxAndWhiskerRenderer
{
	public FixedBoxAndWhiskerRenderer()
	{
		super();
		
		setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
	}
	
	/**
     * Draws the visual representation of a single data item when the plot has 
     * a horizontal orientation.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the plot is being drawn.
     * @param plot  the plot (can be used to obtain standard color 
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     */
    @Override
	public void drawHorizontalItem(Graphics2D g2,
                                   CategoryItemRendererState state,
                                   Rectangle2D dataArea,
                                   CategoryPlot plot,
                                   CategoryAxis domainAxis,
                                   ValueAxis rangeAxis,
                                   CategoryDataset dataset,
                                   int row,
                                   int column) 
    {
        BoxAndWhiskerCategoryDataset bawDataset 
                = (BoxAndWhiskerCategoryDataset) dataset;

        double categoryEnd = domainAxis.getCategoryEnd(column, 
                getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double categoryStart = domainAxis.getCategoryStart(column, 
                getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double categoryWidth = Math.abs(categoryEnd - categoryStart);

        double yy = categoryStart;
        int seriesCount = getRowCount();
        int categoryCount = getColumnCount();

        if (seriesCount > 1) 
        {
            double seriesGap = dataArea.getWidth() * getItemMargin()
                               / (categoryCount * (seriesCount - 1));
            double usedWidth = (state.getBarWidth() * seriesCount) 
                               + (seriesGap * (seriesCount - 1));
            // offset the start of the boxes if the total width used is smaller
            // than the category width
            
            //FIX: Double the offset for the second row so it's pushed up further off of the edge
            //double offset = (categoryWidth - usedWidth) / 2;
            double offset = categoryWidth - usedWidth;
            
            //FIX: Don't want the offset for the first row as it shunts it right up to the edge
            //yy = yy + offset + (row * (state.getBarWidth() + seriesGap));
            yy = yy + (row * (offset + (state.getBarWidth() + seriesGap)));
        } 
        else 
        {
            // offset the start of the box if the box width is smaller than 
            // the category width
            double offset = (categoryWidth - state.getBarWidth()) / 2;
            yy = yy + offset;
        }

        Paint itemPaint = getItemPaint(row, column);
        if (itemPaint != null) {
            g2.setPaint(itemPaint);
        }
        Stroke s = getItemStroke(row, column);
        g2.setStroke(s);

        RectangleEdge location = plot.getRangeAxisEdge();

        Number xQ1 = bawDataset.getQ1Value(row, column);
        Number xQ3 = bawDataset.getQ3Value(row, column);
        Number xMax = bawDataset.getMaxRegularValue(row, column);
        Number xMin = bawDataset.getMinRegularValue(row, column);

        Shape box = null;
        if (xQ1 != null && xQ3 != null && xMax != null && xMin != null) {

            double xxQ1 = rangeAxis.valueToJava2D(xQ1.doubleValue(), dataArea, 
                    location);
            double xxQ3 = rangeAxis.valueToJava2D(xQ3.doubleValue(), dataArea,
                    location);
            double xxMax = rangeAxis.valueToJava2D(xMax.doubleValue(), dataArea,
                    location);
            double xxMin = rangeAxis.valueToJava2D(xMin.doubleValue(), dataArea,
                    location);
            double yymid = yy + state.getBarWidth() / 2.0;
            
            // draw the upper shadow...
            g2.draw(new Line2D.Double(xxMax, yymid, xxQ3, yymid));
            g2.draw(new Line2D.Double(xxMax, yy, xxMax, 
                    yy + state.getBarWidth()));

            // draw the lower shadow...
            g2.draw(new Line2D.Double(xxMin, yymid, xxQ1, yymid));
            g2.draw(new Line2D.Double(xxMin, yy, xxMin,
                    yy + state.getBarWidth()));

            // draw the box...
            box = new Rectangle2D.Double(Math.min(xxQ1, xxQ3), yy, 
                    Math.abs(xxQ1 - xxQ3), state.getBarWidth());
            if (this.getFillBox()) {
                g2.fill(box);
            } 
            g2.draw(box);

        }

        g2.setPaint(this.getArtifactPaint());
        //double aRadius = 0;                 // average radius

        // draw mean - SPECIAL AIMS REQUIREMENT...
        Number xMean = bawDataset.getMeanValue(row, column);
        if (xMean != null
        //FIX #1: Respect whether we've set the mean to be visible or not
          && isMeanVisible()) 
        {
            double xxMean = rangeAxis.valueToJava2D(xMean.doubleValue(), 
                    dataArea, location);
            
            //FIX #2: Don't want a stupid blob, draw a dashed line instead...
            /*aRadius = state.getBarWidth() / 20;
            Ellipse2D.Double avgEllipse = new Ellipse2D.Double(xxMean 
                    - aRadius, yy + aRadius, aRadius * 2, aRadius * 2);
            g2.fill(avgEllipse);
            g2.draw(avgEllipse);*/
            Graphics2D g = (Graphics2D)g2.create();
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
            g.setStroke(dashed);
            g.draw(new Line2D.Double(xxMean, yy, xxMean, 
                    yy + state.getBarWidth()));
        }

        // draw median...
        Number xMedian = bawDataset.getMedianValue(row, column);
        if (xMedian != null) {
            double xxMedian = rangeAxis.valueToJava2D(xMedian.doubleValue(), 
                    dataArea, location);
            g2.draw(new Line2D.Double(xxMedian, yy, xxMedian, 
                    yy + state.getBarWidth()));
        }
        
        // collect entity and tool tip information...
        if (state.getInfo() != null && box != null) {
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                String tip = null;
                CategoryToolTipGenerator tipster 
                        = getToolTipGenerator(row, column);
                if (tipster != null) {
                    tip = tipster.generateToolTip(dataset, row, column);
                }
                String url = null;
                if (getItemURLGenerator(row, column) != null) {
                    url = getItemURLGenerator(row, column).generateURL(
                            dataset, row, column);
                }
                CategoryItemEntity entity = new CategoryItemEntity(box, tip, 
                        url, dataset, row, dataset.getColumnKey(column), 
                        column);
                entities.add(entity);
            }
        }
        
        //FIX #3: Draw the outliers...
        g2.setPaint(itemPaint);
        List<Integer> outliers = bawDataset.getOutliers(row, column);
        for (Integer outlier : outliers)
        {
        	//FIX #4: Extend the plot to show the outliers...
        	if (outlier > rangeAxis.getUpperBound())
            {
            	rangeAxis.setUpperBound(outlier + 2);
            }
        	
        	if (outlier < rangeAxis.getLowerBound())
        	{
        		rangeAxis.setLowerBound(outlier - 2);
        	}
        	
        	double outlierDouble = rangeAxis.valueToJava2D(outlier, dataArea, location);
        	double aRadius = state.getBarWidth() / 40;
        	/*Ellipse2D.Double outlierEllipse = new Ellipse2D.Double(outlierDouble 
                    - aRadius, yy + aRadius, aRadius * 2, aRadius * 2);*/
        	//Draw crosses
        	double xLeft = outlierDouble - aRadius;
        	double xRight = outlierDouble + aRadius;
        	double yTop = yy + (state.getBarWidth()/2) - aRadius;
        	double yBottom = yy + (state.getBarWidth()/2) + aRadius;
        	Line2D.Double line = new Line2D.Double(xLeft, yTop, xRight, yBottom);
        	
        	Line2D.Double line2 = new Line2D.Double(xLeft, yBottom, xRight, yTop);
        	
        	g2.draw(line);
        	g2.draw(line2);
            
            
        }
    } 
}
