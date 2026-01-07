package com.yourname.oracleapp.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ScatterChart extends BaseChart {

    @Override
    public JFreeChart createChart(Object[][] data, String[] columns, String xCol, String yCol) {
        XYSeries series = new XYSeries(yCol);
        int xIndex = -1, yIndex = -1;

        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(xCol)) xIndex = i;
            if (columns[i].equals(yCol)) yIndex = i;
        }

        for (Object[] row : data) {
            try {
                double x = Double.parseDouble(row[xIndex].toString());
                double y = Double.parseDouble(row[yIndex].toString());
                series.add(x, y);
            } catch (Exception ignore) {}
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Scatter Plot: " + yCol + " vs " + xCol,
                xCol, yCol, dataset,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, this.chartColor);

        return chart;
    }
}