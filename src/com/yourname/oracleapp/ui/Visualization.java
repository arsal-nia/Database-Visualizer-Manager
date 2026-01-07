package com.yourname.oracleapp.ui;

import com.yourname.oracleapp.charts.*;
import com.yourname.oracleapp.ai.ChartSummarizer;
import org.jfree.chart.JFreeChart;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.sql.*;

public class Visualization {
    private Connection conn;
    private Object[][] data;
    private String[] columns;
    private Color chartColor = Color.BLUE;

    public Visualization(Connection conn) {
        this.conn = conn;
    }

    public Object[][] getData() {
        return data;
    }

    public String[] getColumns() {
        return columns;
    }

    public void setChartColor(Color color) {
        this.chartColor = color;
    }

    public Color getChartColor() {
        return chartColor;
    }

    public void setData(Object[][] data, String[] columns) {
        this.data = data;
        this.columns = columns;
    }

    public void runQuery(String query) throws SQLException {
        if (!query.toLowerCase().startsWith("select")) {
            throw new SQLException("Query must start with SELECT.");
        }

        try (Statement stmt = conn.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery(query)) {

            com.yourname.oracleapp.model.ResultSetTableModel model =
                    new com.yourname.oracleapp.model.ResultSetTableModel(rs);

            this.data = model.getData();
            this.columns = model.getColumns();

        }
    }

    public JFreeChart generateChart(String type, String xCol, String yCol) {
        BaseChart chart;

        switch (type.toLowerCase()) {
            case "line": chart = new LineChart(); break;
            case "pie": chart = new PieChart(); break;
            case "scatter": chart = new ScatterChart(); break;
            case "area": chart = new AreaChart(); break;
            case "histogram": chart = new HistogramChart(); break;
            case "bar":
            default: chart = new BarChart(); break;
        }

        chart.setChartColor(chartColor);
        return chart.createChart(data, columns, xCol, yCol);
    }

    public String getChartSummary(BufferedImage chartImage, String chartType, String xAxis, String yAxis) throws Exception {
        ChartSummarizer summarizer = new ChartSummarizer();
        return summarizer.summarizeChart(chartImage, chartType, xAxis, yAxis);
    }

}