package ica.ChartView;

import ica.exam.R;

import java.util.Collections;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;


public class FuelChart {

	private static final String DATE_FORMAT = "dd/MM/yyyy";

	public GraphicalView getView(Context context, List<Result> results) {
		String title = context.getString(R.string.chartTitle);

		int[] colors = new int[] { Color.GREEN };
		PointStyle[] styles = new PointStyle[] { PointStyle.POINT};
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);

		Result max = Collections.max(results);
		Result min = Collections.min(results);

		setChartSettings(renderer, context.getString(R.string.chartTitle),
				context.getString(R.string.date),
				context.getString(R.string.value), results.get(0)
						.getDate().getTime(), results.get(results.size() - 1)
						.getDate().getTime(), min.getValue(), max.getValue(), Color.GRAY, Color.LTGRAY);
		renderer.setXLabels(5);
		renderer.setYLabels(10);
		SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(0);
		seriesRenderer.setDisplayChartValues(true);

		return ChartFactory.getTimeChartView(context,
				buildDateDataset(title, results), renderer, DATE_FORMAT);
	}

	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String title, String xTitle, String yTitle, double xMin,
			double xMax, double yMin, double yMax, int axesColor,
			int labelsColor) {
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
	}

	protected XYMultipleSeriesRenderer buildRenderer(int[] colors,
			PointStyle[] styles) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		setRendererProperties(renderer, colors, styles);
		return renderer;
	}

	protected XYMultipleSeriesDataset buildDateDataset(String title,
			List<Result> results) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		TimeSeries series = new TimeSeries(title);
		for (Result result : results) {
			series.add(result.getDate(), result.getValue());
		}
		dataset.addSeries(series);
		return dataset;
	}

	protected void setRendererProperties(XYMultipleSeriesRenderer renderer, int[] colors,
			PointStyle[] styles) {
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setPointSize(5f);
		renderer.setMargins(new int[] { 20, 30, 15, 20 });
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(styles[i]);
			renderer.addSeriesRenderer(r);
		}
	}
}
