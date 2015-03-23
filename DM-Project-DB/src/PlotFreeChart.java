import java.awt.Color;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class PlotFreeChart extends ApplicationFrame {
	public static double rangeMin;
	public static double rangeMax;
	public static double domainMin;
	public static double domainMax;

	public PlotFreeChart(final String title, List<XYSeries> series) {
        super(title);
        final XYDataset dataset = createDataset(series);
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);

    }
    
    private XYDataset createDataset(List<XYSeries> series) {
    	final XYSeriesCollection dataset = new XYSeriesCollection();
        for (int i = 0; i < series.size(); ++i) {
        	dataset.addSeries(series.get(i));
        }
        rangeMin = dataset.getRangeLowerBound(true);
    	rangeMax = dataset.getRangeUpperBound(true);
    	domainMin = dataset.getDomainLowerBound(true);
    	domainMax = dataset.getDomainUpperBound(true);
        return dataset;
    }
    
    private JFreeChart createChart(final XYDataset dataset) {
        final JFreeChart chart = ChartFactory.createXYLineChart(
            "Trajectories",      // chart title
            "X",                      // x axis label
            "Y",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            //true,                     // include legend
            false,
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);
        
        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        //renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setRange(rangeMin - 1, rangeMax + 1);
        return chart;
    }

    public static void Run(List<XYSeries> series) {
        final PlotFreeChart demo = new PlotFreeChart("Trajectories", series);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }
}