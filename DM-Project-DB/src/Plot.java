//package org.jfree.chart.demo;

 import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;


public class Plot extends ApplicationFrame {

	public Plot(final String title, XYSeries series) {
	    super(title);
	    final XYSeriesCollection data = new XYSeriesCollection(series);
	    final JFreeChart chart = ChartFactory.createXYLineChart(
	        "XY Series Demo",
	        "X", 
	        "Y", 
	        data,
	        PlotOrientation.VERTICAL,
	        true,
	        true,
	        false
	    );
	
	    final ChartPanel chartPanel = new ChartPanel(chart);
	    chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
	    System.out.println(chartPanel.isDomainZoomable());
	    chartPanel.setZoomAroundAnchor(true);
	    //chart.
	    setContentPane(chartPanel);
	}
	
	static public void Run(XYSeries series) {
		XYSeries center = new XYSeries("PLOT");
		List<XYDataItem> dump = series.getItems();
		double minx = series.getMinX();
		double miny = series.getMinY();
		
		final Plot demo = new Plot("Trajectories", series);
	    demo.pack();
	    RefineryUtilities.centerFrameOnScreen(demo);
	    demo.setVisible(true);
	}
	
	// ****************************************************************************
	// * JFREECHART DEVELOPER GUIDE                                               *
	// * The JFreeChart Developer Guide, written by David Gilbert, is available   *
	// * to purchase from Object Refinery Limited:                                *
	// *                                                                          *
	// * http://www.object-refinery.com/jfreechart/guide.html                     *
	// *                                                                          *
	// * Sales are used to provide funding for the JFreeChart project - please    * 
	// * support us so that we can continue developing free software.             *
	// ****************************************************************************
	
	/**
	 * Starting point for the demonstration application.
	 *
	 * @param args  ignored.
	 */
	/*
		public static void main(final String[] args) {
		
		    final Plot demo = new Plot("Trajectories");
		    demo.pack();
		    RefineryUtilities.centerFrameOnScreen(demo);
		    demo.setVisible(true);
		}
		*/
}