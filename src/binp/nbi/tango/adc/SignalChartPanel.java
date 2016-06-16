package binp.nbi.tango.adc;

import binp.nbi.tango.util.ZipBufferedReader;
import binp.nbi.tango.util.Constants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

public class SignalChartPanel extends ChartPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3243572960865124971L;

	public static final String version = "2.1";

	private double zeroStart = 0.0;
	private double zeroLength = 0.0;
	private double zeroX = 0.0;
	private double zeroY = 0.0;
	private double markStart = 0.0;
	private double markLength = 0.0;
	private double markX = 0.0;
	private double markY = 0.0;
	private double displayUnit = 1.0;
	private String unit = "";
	private String label = "Signal";
	
	public SignalChartPanel(JFreeChart chart, boolean useBuffer) {
		super(chart, useBuffer);
		// TODO Auto-generated constructor stub
	}

	public SignalChartPanel() {
		super(ChartFactory.createXYLineChart("Line Chart ", // chart title
				"Time, ms", // x axis label
				"Signal, V", // y axis label
				new XYSeriesCollection(), // data
				PlotOrientation.VERTICAL, 
				false, // include legend
				false, // tooltips
				false // urls
				), true);
		
		// CUSTOMISATION OF THE CHART
		
		setPreferredSize(new Dimension(400, 300));	

		// Get a reference to the plot for further customization
		XYPlot plot = getChart().getXYPlot();
	
		Color backgroundColor = new Color(28, 100, 140);
		plot.setBackgroundPaint(backgroundColor);
		plot.setAxisOffset(RectangleInsets.ZERO_INSETS);
		plot.setDomainGridlinePaint(Color.white); // x grid lines color
		plot.setRangeGridlinePaint(Color.white);  // y grid lines color
		getChart().getTitle().setFont(new Font("SansSerif", Font.BOLD, 16));
	
		// Set trace colors
		setLineColor(Color.red, Color.blue, Color.green, Color.gray);
	
		// Disable tooltips
		//getChart().getXYPlot().getRenderer().setBaseToolTipGenerator(null);
	}
	
	public SignalChartPanel(String fileName) {
		this();
		if (fileName != null && !"".equals(fileName)) {
			readParameters(fileName);
			readData(fileName);
			setChartParam();
		}
	}

	public SignalChartPanel(String fileName, String entryName) {
		this();
		if (fileName != null && entryName != null && !"".equals(fileName) && !"".equals(entryName)) {
			readParameters(fileName, entryName);
			readData(fileName, entryName);
			setChartParam();
		}
	}

	public void setTitle() {
		String title = String.format("Signal: %s = %7.3f %s", label, markY
				- zeroY, unit);
		getChart().setTitle(title);
	}

	public void setYTitle() {
		String yTitle = label;
		if ("".equals(yTitle)) yTitle = "Signal";
		yTitle = yTitle + ", " + unit;
		setYTitle(yTitle);
	}

	public void setYTitle(String title) {
		XYPlot plot = getChart().getXYPlot();
		plot.getRangeAxis().setLabel(title);
	}

	public void setXTitle(String title) {
		XYPlot plot = getChart().getXYPlot();
		plot.getDomainAxis().setLabel(title);
	}

	public void setXTitle() {
		setXTitle("Time, ms");
	}

	public void resetZoom() {
		restoreAutoBounds();
	}
	
	public void clearPlotMarkers() {
		XYPlot plot = getChart().getXYPlot();
		// Stop refreshing the plot
		boolean savedNotify = plot.isNotify();
		plot.setNotify(false);
		// Remove markers
		plot.clearDomainMarkers();
		plot.clearRangeMarkers();
		// Restore refreshing
		plot.setNotify(savedNotify);
	}

	public void setPlotMarkers() {
		// Get current Plot from Chart
		XYPlot plot = getChart().getXYPlot();

		// Remember refreshing state
		boolean savedNotify = plot.isNotify();	
		// Stop refreshing the plot
		plot.setNotify(false);

		// Remove old markers
		plot.clearDomainMarkers();
		plot.clearRangeMarkers();

		// Add new markers
		// Add zero marker
		if (zeroLength > 0.0) {
			ValueMarker rangeMarker = new ValueMarker(zeroY);
			rangeMarker.setPaint(Color.magenta);
			rangeMarker.setLabel(null);
			plot.addRangeMarker(rangeMarker);
		}
		// Add mark marker
		if (markLength > 0.0) {
			ValueMarker domainMarker = new ValueMarker(markX);
			domainMarker.setPaint(Color.magenta);
			domainMarker.setLabel(null);
			plot.addDomainMarker(domainMarker);
			ValueMarker rangeMarker = new ValueMarker(markY);
			rangeMarker.setPaint(Color.magenta);
			rangeMarker.setLabel(null);
			plot.addRangeMarker(rangeMarker);
		}

		// Restore refreshing state
		plot.setNotify(savedNotify);
	}

	public void setLineColor(Color color0, Color ... colors) {
		XYPlot plot = getChart().getXYPlot();
		XYItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesPaint(0, color0);
		int i = 1; 
		for(Color c: colors) 
			renderer.setSeriesPaint(i++, c);
	}

	public void setLineColor(int index, Color color) {
		XYPlot plot = getChart().getXYPlot();
		XYItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesPaint(index, color);
	}

	public void setLineWidth(int index, float lineWidth) {
		XYPlot plot = getChart().getXYPlot();
		XYItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesStroke(index, new BasicStroke(lineWidth));
	}

	public void setLineWidth(float lineWidth, float ... lineWidthArray) {
		XYPlot plot = getChart().getXYPlot();
		XYItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesStroke(0, new BasicStroke(lineWidth));
		int i = 1; 
		for(float lw: lineWidthArray) 
			renderer.setSeriesStroke(i++, new BasicStroke(lw));
	}

	public void addAnnotation(String text, double x, double y) {
		XYTextAnnotation annotation = new XYTextAnnotation(text, x, y);
		Font font = new Font("SansSerif", Font.PLAIN, 16);
		annotation.setFont(font);
		annotation.setPaint(Color.magenta);
		getChart().getXYPlot().addAnnotation(annotation);
	}
	
	public double getZeroStart() {
		return zeroStart;
	}
	
	public double getZeroX() {
		return zeroX;
	}

	public double getMarkStart() {
		return markStart;
	}

	public double getZeroLength() {
		return zeroLength;
	}

	public double getZeroY() {
		return zeroY;
	}

	public double getMarkLength() {
		return markLength;
	}

	public double getMarkX() {
		return markX;
	}

	public double getMarkY() {
		return markY;
	}

	public String getUnit() {
		return unit;
	}

	public String getLabel() {
		return label;
	}

	public double getDisplayUnit() {
		return displayUnit;
	}

	public int getSeriesCount() {
		XYPlot plot = getChart().getXYPlot();
		XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();
		return dataset.getSeriesCount();
	}
	
	public XYSeries getSeries(int seriesNumber) {
		XYPlot plot = getChart().getXYPlot();
		XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();
		return dataset.getSeries(seriesNumber);
	}
	
	public List<?> getSeries() {
		XYPlot plot = getChart().getXYPlot();
		XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();
		return dataset.getSeries();
	}
	
	public void addSeries(XYSeries series) {
		XYPlot plot = getChart().getXYPlot();
		XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();
		dataset.addSeries(series);
	}
	
	public void removeAllSeries() {
		XYPlot plot = getChart().getXYPlot();
		XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();
		dataset.removeAllSeries();
	}
	
	public void removeSeries(int seriesNumber) {
		XYPlot plot = getChart().getXYPlot();
		XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();
		dataset.removeSeries(seriesNumber);
	}
	
	public void removeSeries(XYSeries series) {
		XYPlot plot = getChart().getXYPlot();
		XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();
		dataset.removeSeries(series);
	}

	public void readData(BufferedReader br) {
		XYSeries series0 = new XYSeries("Signal");
		XYSeries series1 = new XYSeries("Zero");
		XYSeries series2 = new XYSeries("Mark");
		double zeroSum = 0.0;
		double zeroN = 0.0;
		double markSum = 0.0;
		double markN = 0.0;
		String line;
		try {
			line = br.readLine();
			while (line != null && line != "") {
				String[] xy = line.split(Constants.XY_DELIMETER);
				if (xy.length > 1) {
					try {
						double x = Double.parseDouble(xy[0]);
						double y = Double.parseDouble(xy[1]);
						y = y * displayUnit;
						series0.add(x, y);
						if (zeroLength > 0.0 && x >= zeroStart
								&& x <= zeroStart + zeroLength) {
							series1.add(x, y);
							zeroSum += y;
							zeroN++;
						}
						if (markLength > 0.0 && x >= markStart
								&& x <= markStart + markLength) {
							series2.add(x, y);
							markSum += y;
							markN++;
						}
					} catch (NumberFormatException | NullPointerException e) {
					}
				}
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (zeroN > 0.0) {
			zeroX = zeroStart + zeroLength / 2.0;
			zeroY = zeroSum / zeroN;
		} else {
			zeroX = 0.0;
			zeroY = 0.0;
		}
		if (markN > 0.0) {
			markX = markStart + markLength / 2.0;
			markY = markSum / markN;
		} else {
			markX = 0.0;
			markY = 0.0;
		}

		removeAllSeries();
		addSeries(series2);
		addSeries(series1);
		addSeries(series0);
	}

	public void readData(String fileName) {
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			readData(br);
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("File " + fileName + " not found");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void readData(String zipFileName, String zipEntryName) {
		try {
			ZipBufferedReader zbr = new ZipBufferedReader(zipFileName);
			if (zbr.findZipEntry(zipEntryName)) 
				readData(zbr.getBufferedReader());
			zbr.close();
		} catch (FileNotFoundException e) {
			System.out.println("File " + zipFileName + " not found");
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readParameters(BufferedReader br) {
		//System.out.println("readParameters br");
		try {
			String line;
			while ((line = br.readLine()) != null) {
				//System.out.println(line);
				String[] nv;
				if (line.contains(Constants.PROP_VAL_DELIMETER)) {
					//System.out.println(Constants.PROP_VAL_DELIMETER);
					nv = line.split(Constants.PROP_VAL_DELIMETER);
				} else {
					if (line.contains(Constants.PROP_VAL_DELIMETER_OLD)) {
						//System.out.println(Constants.PROP_VAL_DELIMETER);
						nv = line.split(Constants.PROP_VAL_DELIMETER_OLD);
					}
					else 
						continue;
				}
				//System.out.println(nv[0] + " - " + nv[1]);
				if (nv.length > 1) {
					if (nv[0].equals(Constants.DISPLAY_UNIT))
						displayUnit = Double.parseDouble(nv[1]);
					if (nv[0].equals(Constants.MARK_NAME + Constants.LENGTH_SUFFIX))
						markLength = Double.parseDouble(nv[1]);
					if (nv[0].equals(Constants.MARK_NAME + Constants.START_SUFFIX))
						markStart = Double.parseDouble(nv[1]);
					if (nv[0].equals(Constants.ZERO_NAME + Constants.START_SUFFIX))
						zeroStart = Double.parseDouble(nv[1]);
					if (nv[0].equals(Constants.ZERO_NAME + Constants.LENGTH_SUFFIX))
						zeroLength = Double.parseDouble(nv[1]);
					if (nv[0].equals(Constants.UNIT))
						unit = nv[1];
					if (nv[0].equals(Constants.LABEL))
						label = nv[1];
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readParameters(String txtFileName) {
		try {
			FileReader fr = new FileReader(txtFileName);
			BufferedReader br = new BufferedReader(fr);
			readParameters(br);
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("File " + txtFileName + " not found");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readParameters(String zipFileName, String zipEntryName) {
		String paramEntryName = zipEntryName.replace(Constants.CHAN, Constants.PARAM + Constants.CHAN);
		try {
			ZipBufferedReader zbr = new ZipBufferedReader(zipFileName);
			if (zbr.findZipEntry(paramEntryName)) 
				readParameters(zbr.getBufferedReader());
			zbr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("File " + zipFileName + " not found");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setChartParam() {
		XYPlot plot = getChart().getXYPlot();
		// Stop refreshing the plot
		boolean savedNotify = plot.isNotify();
		plot.setNotify(false);
		// Set plot parameters
		setTitle();
		setYTitle();
		setPlotMarkers();
		resetZoom();
		// Restore refreshing
		plot.setNotify(savedNotify);
	}

}