package com.yourname.oracleapp.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class LineChart extends BaseChart {

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
                if (row[xIndex] != null && row[yIndex] != null) {
                    double y = Double.parseDouble(row[yIndex].toString());
                    dataset.addValue(y, yCol, row[xIndex].toString());
                }
            } catch (NumberFormatException ignore) {}
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Line Chart: " + yCol + " vs " + xCol,
                xCol,
                yCol,
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, this.chartColor);

        return chart;
    }
}