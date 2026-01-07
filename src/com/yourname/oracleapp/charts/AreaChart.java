package com.yourname.oracleapp.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class AreaChart extends BaseChart {

    @Override
    public JFreeChart createChart(Object[][] data, String[] columns, String xCol, String yCol) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int xIndex = -1, yIndex = -1;
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(xCol)) xIndex = i;
            if (columns[i].equals(yCol)) yIndex = i;
        }

        for (Object[] row : data) {
            try {
                double value = Double.parseDouble(row[yIndex].toString());
                dataset.addValue(value, yCol, row[xIndex].toString());
            } catch (Exception ignore) {}
        }

        JFreeChart chart = ChartFactory.createAreaChart(
                "Area Chart: " + yCol + " vs " + xCol,
                xCol,
                yCol,
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, this.chartColor);

        return chart;
    }
}