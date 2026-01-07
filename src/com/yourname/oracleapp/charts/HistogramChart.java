package com.yourname.oracleapp.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.statistics.HistogramDataset;

public class HistogramChart extends BaseChart {

    @Override
    public JFreeChart createChart(Object[][] data, String[] columns, String xCol, String yCol) {
        HistogramDataset dataset = new HistogramDataset();
        int xIndex = -1;

        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(xCol)) xIndex = i;
        }

        try {
            double[] values = new double[data.length];
            int count = 0;
            for (Object[] row : data) {
                if (row[xIndex] != null) {
                    values[count++] = Double.parseDouble(row[xIndex].toString());
                }
            }
            double[] trimmed = new double[count];
            System.arraycopy(values, 0, trimmed, 0, count);
            dataset.addSeries(xCol, trimmed, 10);
        } catch (Exception ignore) {}

        JFreeChart chart = ChartFactory.createHistogram(
                "Histogram: " + xCol,
                xCol,
                "Frequency",
                dataset,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, this.chartColor);

        return chart;
    }
}