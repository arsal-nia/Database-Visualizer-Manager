package com.yourname.oracleapp.charts;

import org.jfree.chart.JFreeChart;
import java.awt.Color;

public abstract class BaseChart {
    protected Color chartColor = Color.BLUE;

    public void setChartColor(Color color) {
        this.chartColor = color;
    }

    public abstract JFreeChart createChart(Object[][] data, String[] columns, String xCol, String yCol);
}
