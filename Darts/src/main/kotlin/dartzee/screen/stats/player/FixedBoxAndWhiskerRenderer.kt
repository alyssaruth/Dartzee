package dartzee.screen.stats.player

import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.ValueAxis
import org.jfree.chart.entity.CategoryItemEntity
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer
import org.jfree.chart.renderer.category.CategoryItemRendererState
import org.jfree.data.category.CategoryDataset
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset
import java.awt.BasicStroke
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.Stroke
import java.awt.geom.Line2D
import java.awt.geom.Rectangle2D

/**
 * Adapting the source code from BoxAndWhiskerRenderer to fix a few bugs with the horizontal layout
 * and make some customisations.
 *
 */
class FixedBoxAndWhiskerRenderer : BoxAndWhiskerRenderer()
{

    /**
     * Draws the visual representation of a single data item when the plot has
     * a horizontal orientation.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the plot is being drawn.
     * @param plot  the plot (can be used to obtain standard color
     * information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     */
    override fun drawHorizontalItem(
        g2: Graphics2D,
        state: CategoryItemRendererState,
        dataArea: Rectangle2D,
        plot: CategoryPlot,
        domainAxis: CategoryAxis,
        rangeAxis: ValueAxis,
        dataset: CategoryDataset,
        row: Int,
        column: Int
    ) {
        val bawDataset = dataset as BoxAndWhiskerCategoryDataset
        val categoryEnd = domainAxis.getCategoryEnd(
            column,
            columnCount, dataArea, plot.domainAxisEdge
        )
        val categoryStart = domainAxis.getCategoryStart(
            column,
            columnCount, dataArea, plot.domainAxisEdge
        )
        val categoryWidth = Math.abs(categoryEnd - categoryStart)
        var yy = categoryStart
        val seriesCount = rowCount
        val categoryCount = columnCount
        yy = if (seriesCount > 1)
        {
            val seriesGap = (dataArea.width * itemMargin
                    / (categoryCount * (seriesCount - 1)))
            val usedWidth = (state.barWidth * seriesCount
                    + seriesGap * (seriesCount - 1))
            // offset the start of the boxes if the total width used is smaller
            // than the category width
            //FIX: Double the offset for the second row so it's pushed up further off of the edge
            //double offset = (categoryWidth - usedWidth) / 2;
            val offset = categoryWidth - usedWidth
            //FIX: Don't want the offset for the first row as it shunts it right up to the edge
            //yy = yy + offset + (row * (state.getBarWidth() + seriesGap));
            yy + row * (offset + (state.barWidth + seriesGap))
        } else { // offset the start of the box if the box width is smaller than
            // the category width
            val offset = (categoryWidth - state.barWidth) / 2
            yy + offset
        }
        val itemPaint = getItemPaint(row, column)
        if (itemPaint != null) {
            g2.paint = itemPaint
        }
        val s = getItemStroke(row, column)
        g2.stroke = s
        val location = plot.rangeAxisEdge
        val xQ1 = bawDataset.getQ1Value(row, column)
        val xQ3 = bawDataset.getQ3Value(row, column)
        val xMax = bawDataset.getMaxRegularValue(row, column)
        val xMin = bawDataset.getMinRegularValue(row, column)
        var box: Shape? = null
        if (xQ1 != null && xQ3 != null && xMax != null && xMin != null) {
            val xxQ1 = rangeAxis.valueToJava2D(
                xQ1.toDouble(), dataArea,
                location
            )
            val xxQ3 = rangeAxis.valueToJava2D(
                xQ3.toDouble(), dataArea,
                location
            )
            val xxMax = rangeAxis.valueToJava2D(
                xMax.toDouble(), dataArea,
                location
            )
            val xxMin = rangeAxis.valueToJava2D(
                xMin.toDouble(), dataArea,
                location
            )
            val yymid = yy + state.barWidth / 2.0
            // draw the upper shadow...
            g2.draw(Line2D.Double(xxMax, yymid, xxQ3, yymid))
            g2.draw(
                Line2D.Double(
                    xxMax, yy, xxMax,
                    yy + state.barWidth
                )
            )
            // draw the lower shadow...
            g2.draw(Line2D.Double(xxMin, yymid, xxQ1, yymid))
            g2.draw(
                Line2D.Double(
                    xxMin, yy, xxMin,
                    yy + state.barWidth
                )
            )
            // draw the box...
            box = Rectangle2D.Double(
                Math.min(xxQ1, xxQ3), yy,
                Math.abs(xxQ1 - xxQ3), state.barWidth
            )
            if (fillBox) {
                g2.fill(box)
            }
            g2.draw(box)
        }
        g2.paint = artifactPaint
        //double aRadius = 0;                 // average radius
// draw mean - SPECIAL AIMS REQUIREMENT...
        val xMean = bawDataset.getMeanValue(row, column)
        if (xMean != null //FIX #1: Respect whether we've set the mean to be visible or not
            && isMeanVisible
        ) {
            val xxMean = rangeAxis.valueToJava2D(
                xMean.toDouble(),
                dataArea, location
            )
            //FIX #2: Don't want a stupid blob, draw a dashed line instead...
/*aRadius = state.getBarWidth() / 20;
            Ellipse2D.Double avgEllipse = new Ellipse2D.Double(xxMean
                    - aRadius, yy + aRadius, aRadius * 2, aRadius * 2);
            g2.fill(avgEllipse);
            g2.draw(avgEllipse);*/
            val g = g2.create() as Graphics2D
            val dashed: Stroke = BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0f, floatArrayOf(5f), 0f)
            g.stroke = dashed
            g.draw(
                Line2D.Double(
                    xxMean, yy, xxMean,
                    yy + state.barWidth
                )
            )
        }
        // draw median...
        val xMedian = bawDataset.getMedianValue(row, column)
        if (xMedian != null) {
            val xxMedian = rangeAxis.valueToJava2D(
                xMedian.toDouble(),
                dataArea, location
            )
            g2.draw(
                Line2D.Double(
                    xxMedian, yy, xxMedian,
                    yy + state.barWidth
                )
            )
        }
        // collect entity and tool tip information...
        if (state.info != null && box != null) {
            val entities = state.entityCollection
            if (entities != null) {
                var tip: String? = null
                val tipster = getToolTipGenerator(row, column)
                if (tipster != null) {
                    tip = tipster.generateToolTip(dataset, row, column)
                }
                var url: String? = null
                if (getItemURLGenerator(row, column) != null) {
                    url = getItemURLGenerator(row, column).generateURL(
                        dataset, row, column
                    )
                }
                val entity = CategoryItemEntity(
                    box, tip,
                    url, dataset, row, dataset.getColumnKey(column),
                    column
                )
                entities.add(entity)
            }
        }
        //FIX #3: Draw the outliers...
        g2.paint = itemPaint
        val outliers: List<Int> = bawDataset.getOutliers(row, column) as List<Int>
        for (outlier in outliers) { //FIX #4: Extend the plot to show the outliers...
            if (outlier > rangeAxis.upperBound) {
                rangeAxis.upperBound = outlier + 2.toDouble()
            }
            if (outlier < rangeAxis.lowerBound) {
                rangeAxis.lowerBound = outlier - 2.toDouble()
            }
            val outlierDouble = rangeAxis.valueToJava2D(outlier.toDouble(), dataArea, location)
            val aRadius = state.barWidth / 40
            /*Ellipse2D.Double outlierEllipse = new Ellipse2D.Double(outlierDouble
                    - aRadius, yy + aRadius, aRadius * 2, aRadius * 2);*/
//Draw crosses
            val xLeft = outlierDouble - aRadius
            val xRight = outlierDouble + aRadius
            val yTop = yy + state.barWidth / 2 - aRadius
            val yBottom = yy + state.barWidth / 2 + aRadius
            val line = Line2D.Double(xLeft, yTop, xRight, yBottom)
            val line2 = Line2D.Double(xLeft, yBottom, xRight, yTop)
            g2.draw(line)
            g2.draw(line2)
        }
    }

    init {
        toolTipGenerator = BoxAndWhiskerToolTipGenerator()
    }
}