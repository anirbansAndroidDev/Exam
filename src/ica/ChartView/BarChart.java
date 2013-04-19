package ica.ChartView;

import java.util.List;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;

public class BarChart {

	public GraphicalView getView(Context context, String[] titles,
			int[] colors, double[] values, List<String> Subject_Name) {

		/*
		 * XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
		 * 
		 * setChartSettings(renderer, "Average marks obtained per subject.",
		 * "Subjects", "Avg. Marks Obtained", 0.5, 12.5, 0, 100, Color.GRAY,
		 * Color.LTGRAY);
		 * 
		 * // /CHART SETTINGS//////
		 * 
		 * renderer.setChartTitle("Average marks obtained per subject.");
		 * renderer.setXTitle("Subjects");
		 * renderer.setYTitle("Avg. Marks Obtained");
		 * 
		 * renderer.setYAxisMin(0); renderer.setYAxisMax(100);
		 * 
		 * for (int i = 0; i < Subject_Name.size(); i++) {
		 * renderer.addXTextLabel(i, Subject_Name.get(i)); }
		 * 
		 * renderer.setAxesColor(Color.GRAY);
		 * renderer.setLabelsColor(Color.LTGRAY);
		 * 
		 * // ///////////////////////
		 * 
		 * for (int srsCnt = 0; srsCnt < titles.length; srsCnt++) {
		 * renderer.getSeriesRendererAt(srsCnt).setDisplayChartValues(true);
		 * 
		 * } // renderer.getSeriesRendererAt(1).setDisplayChartValues(true);
		 * 
		 * //renderer.setXLabels(12); //renderer.setYLabels(10);
		 * 
		 * renderer.setXLabelsAlign(Align.LEFT);
		 * renderer.setYLabelsAlign(Align.LEFT); renderer.setPanEnabled(true,
		 * false);
		 * 
		 * renderer.setZoomEnabled(true);
		 * 
		 * renderer.setZoomRate(1.1f); renderer.setBarSpacing(0.5f);
		 * renderer.setClickEnabled(true);
		 * 
		 * GraphicalView ViewResult = null;
		 * 
		 * try { ViewResult = ChartFactory.getBarChartView(context,
		 * buildBarDataset(titles, values), renderer, Type.STACKED); } catch
		 * (Exception e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		XYMultipleSeriesRenderer renderer = getBarDemoRenderer(Subject_Name);
		setChartSettings(renderer, values.length);

		GraphicalView BarChartView = null;

		try {
			// ViewResult = ChartFactory.getBarChartView(context,
			// getBarDataset(), renderer, Type.STACKED);
			BarChartView = ChartFactory.getBarChartView(context,
					getBarDataset(values), renderer, Type.STACKED);

		} catch (Exception e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BarChartView;
	}

	private XYMultipleSeriesDataset getBarDataset(double[] values) {

		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		CategorySeries series = new CategorySeries("Subject series");
		for (double ival : values) {
			series.add(ival);
		}
		dataset.addSeries(series.toXYSeries());
		return dataset;
	}

	public XYMultipleSeriesRenderer getBarDemoRenderer(List<String> Subject_Name) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setBarSpacing(1);
		renderer.setApplyBackgroundColor(false);
		renderer.setBackgroundColor(Color.TRANSPARENT);

		renderer.setXLabelsAlign(Align.LEFT);
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.setPanEnabled(true, false);

		renderer.setZoomEnabled(true);

		renderer.setZoomRate(1.1f);
		renderer.setBarSpacing(0.5f);
		renderer.setClickEnabled(true);

		int i = 1;

		if (Subject_Name != null && Subject_Name.size() > 0) {
			for (String strval : Subject_Name) {
				renderer.addXTextLabel(i++, strval);
			}
		}

		/*
		 * renderer.addXTextLabel(1, "Sun"); renderer.addXTextLabel(2, "Mon");
		 * renderer.addXTextLabel(3, "Tue"); renderer.addXTextLabel(4, "Wed");
		 * renderer.addXTextLabel(5, "Thu"); renderer.addXTextLabel(6, "Fri");
		 * renderer.addXTextLabel(7, "Sat");
		 */
	//	 0, 30, 50, 0 
		renderer.setMargins(new int[] { 120, 30, 50, 0 });
		SimpleSeriesRenderer r = new SimpleSeriesRenderer();
		r.setColor(Color.rgb(51, 204, 255));

		renderer.addSeriesRenderer(r);
		return renderer;
	}

	private void setChartSettings(XYMultipleSeriesRenderer renderer,
			int xaxisLength) {

		renderer.setChartTitle("Average marks obtained per subject.");
	
		renderer.setXTitle("Subjects");
		renderer.setYTitle("Avg. Marks Obtained");

		renderer.setXAxisMin(0);
		renderer.setXAxisMax(7);
		renderer.setXLabelsAngle(30);
		renderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
		
		renderer.setYAxisMin(0);
		renderer.setYAxisMax(100);
		
		renderer.setApplyBackgroundColor(false);
		renderer.setBackgroundColor(Color.TRANSPARENT);

	}

	public String getName() {
		return "Sales stacked bar chart";
	}

	public String getDesc() {
		return "The monthly sales for the last 2 years (stacked bar chart)";
	}

	public Intent execute(Context context) {
		// TODO Auto-generated method stub
		return null;
	}
}
