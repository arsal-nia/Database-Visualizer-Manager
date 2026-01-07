package com.yourname.oracleapp.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class PieChart extends BaseChart {

    @Override
    public JFreeChart createChart(Object[][] data, String[] columns, String xCol, String yCol) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        int xIndex = -1, yIndex = -1;

        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(xCol)) xIndex = i;
            if (columns[i].equals(yCol)) yIndex = i;
        }

        for (Object[] row : data) {
            try {
                if (row[xIndex] != null && row[yIndex] != null) {
                    double value = Double.parseDouble(row[yIndex].toString());
                    dataset.setValue(row[xIndex].toString(), value);
                }
            } catch (NumberFormatException ignore) {}
        }

        return ChartFactory.createPieChart("Pie Chart", dataset, true, true, false);
    }
}
