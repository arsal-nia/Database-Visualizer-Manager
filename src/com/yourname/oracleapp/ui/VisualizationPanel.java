package com.yourname.oracleapp.ui;

import com.yourname.oracleapp.model.ResultSetTableModel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.util.List;

public class VisualizationPanel extends JPanel {
    private static final Color PRIMARY_BG = new Color(28, 48, 74);
    private static final Color SECONDARY_BG = new Color(43, 74, 111);
    private static final Color FOREGROUND_COLOR = new Color(217, 217, 217);
    private static final Color ACCENT_COLOR = new Color(99, 127, 154);

    private Visualization visualization;
    private JTextArea queryBox;
    private JTable resultTable;
    private JComboBox<String> xColBox, yColBox, chartTypeBox;
    private JButton runBtn, drawBtn, colorBtn, summarizeBtn, backBtn;
    private JLabel statusLabel;
    private JPanel chartContainer;
    private ChartPanel currentChartPanel;
    private boolean isChartDrawn = false;

    private List<String> preloadedColumns;
    private List<List<Object>> preloadedRows;

    public VisualizationPanel(Connection conn, List<String> columnNames, List<List<Object>> rows, Runnable onBack) {
        this.visualization = new Visualization(conn);
        this.preloadedColumns = columnNames;
        this.preloadedRows = rows;

        setLayout(new BorderLayout(10,10));
        setBackground(PRIMARY_BG);

        JPanel topPanel = new JPanel(new BorderLayout(5,5));
        topPanel.setBackground(PRIMARY_BG);
        queryBox = new JTextArea("SELECT * FROM EMPLOYEES");
        queryBox.setBackground(SECONDARY_BG);
        queryBox.setForeground(FOREGROUND_COLOR);
        queryBox.setCaretColor(FOREGROUND_COLOR);

        runBtn = new JButton("Run Query");
        runBtn.setBackground(SECONDARY_BG);
        runBtn.setForeground(FOREGROUND_COLOR);

        topPanel.add(new JScrollPane(queryBox), BorderLayout.CENTER);
        topPanel.add(runBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(PRIMARY_BG);
        chartContainer.setMinimumSize(new Dimension(100, 100));

        resultTable = new JTable();
        resultTable.setBackground(new Color(20, 35, 55));
        resultTable.setForeground(FOREGROUND_COLOR);

        resultTable.getTableHeader().setBackground(SECONDARY_BG);
        resultTable.getTableHeader().setForeground(FOREGROUND_COLOR);
        resultTable.getTableHeader().setOpaque(true);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(SECONDARY_BG);
        headerRenderer.setForeground(FOREGROUND_COLOR);
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        resultTable.getTableHeader().setDefaultRenderer(headerRenderer);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        scrollPane.getViewport().setBackground(new Color(20, 35, 55));
        scrollPane.setMinimumSize(new Dimension(100, 100));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, chartContainer);
        splitPane.setBackground(PRIMARY_BG);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout());
        controls.setBackground(PRIMARY_BG);

        xColBox = createDarkComboBox();
        yColBox = createDarkComboBox();
        chartTypeBox = createDarkComboBox(new String[]{
                "Bar", "Line", "Pie", "Scatter", "Area", "Histogram"
        });

        colorBtn = createDarkButton("Pick Color");
        drawBtn = createDarkButton("Draw Chart");

        summarizeBtn = createDarkButton("Summarize Chart by AI");
        summarizeBtn.setEnabled(true);
        summarizeBtn.setBackground(SECONDARY_BG);

        backBtn = createDarkButton("Back");

        statusLabel = new JLabel("Ready.");
        statusLabel.setForeground(FOREGROUND_COLOR);

        controls.add(createDarkLabel("X-axis:")); controls.add(xColBox);
        controls.add(createDarkLabel("Y-axis:")); controls.add(yColBox);
        controls.add(createDarkLabel("Type:")); controls.add(chartTypeBox);
        controls.add(colorBtn);
        controls.add(drawBtn);
        controls.add(summarizeBtn);
        controls.add(backBtn);
        controls.add(statusLabel);
        add(controls, BorderLayout.SOUTH);

        runBtn.addActionListener(e -> runQuery());
        drawBtn.addActionListener(e -> drawChart());
        colorBtn.addActionListener(e -> chooseColor());
        summarizeBtn.addActionListener(e -> summarizeChart());
        backBtn.addActionListener(e -> onBack.run());

        if (preloadedColumns != null && preloadedRows != null && !preloadedColumns.isEmpty() && !preloadedRows.isEmpty()) {
            loadPreloadedData();
        }
    }

    private void loadPreloadedData() {
        try {
            Object[][] dataArray = new Object[preloadedRows.size()][];
            for (int i = 0; i < preloadedRows.size(); i++) {
                List<Object> row = preloadedRows.get(i);
                dataArray[i] = row.toArray();
            }

            String[] columnsArray = preloadedColumns.toArray(new String[0]);

            visualization.setData(dataArray, columnsArray);

            resultTable.setModel(new ResultSetTableModel(dataArray, columnsArray));

            xColBox.removeAllItems();
            yColBox.removeAllItems();
            for (String col : columnsArray) {
                xColBox.addItem(col);
                yColBox.addItem(col);
            }

            statusLabel.setText("Data loaded from Analytics (" + preloadedRows.size() + " rows)");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading preloaded data: " + ex.getMessage());
            statusLabel.setText("Error loading data.");
        }
    }

    private JComboBox<String> createDarkComboBox() {
        JComboBox<String> box = new JComboBox<>();
        box.setBackground(SECONDARY_BG);
        box.setForeground(FOREGROUND_COLOR);
        return box;
    }

    private JComboBox<String> createDarkComboBox(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setBackground(SECONDARY_BG);
        box.setForeground(FOREGROUND_COLOR);
        return box;
    }

    private JButton createDarkButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(SECONDARY_BG);
        button.setForeground(FOREGROUND_COLOR);
        return button;
    }

    private JLabel createDarkLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(FOREGROUND_COLOR);
        return label;
    }

    private void runQuery() {
        try {
            visualization.runQuery(queryBox.getText().trim());

            resultTable.setModel(new ResultSetTableModel(
                    visualization.getData(),
                    visualization.getColumns()
            ));

            xColBox.removeAllItems();
            yColBox.removeAllItems();
            for (String col : visualization.getColumns()) {
                xColBox.addItem(col);
                yColBox.addItem(col);
            }

            statusLabel.setText("Query executed successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            statusLabel.setText("Query failed.");
        }
    }

    private void drawChart() {
        if (visualization.getData() == null || visualization.getColumns() == null) {
            JOptionPane.showMessageDialog(this, "Please run a query first!");
            return;
        }

        String xCol = (String) xColBox.getSelectedItem();
        String yCol = (String) yColBox.getSelectedItem();
        String type = (String) chartTypeBox.getSelectedItem();

        JFreeChart chart = visualization.generateChart(type, xCol, yCol);
        chart.setAntiAlias(true);

        currentChartPanel = new ChartPanel(chart);
        currentChartPanel.setBackground(PRIMARY_BG);

        chartContainer.removeAll();
        chartContainer.add(currentChartPanel, BorderLayout.CENTER);

        chartContainer.revalidate();
        chartContainer.repaint();
        this.revalidate();

        isChartDrawn = true;
        statusLabel.setText("Chart drawn successfully. Click 'Summarize by AI' for insights.");
    }

    private void chooseColor() {
        Color color = JColorChooser.showDialog(this, "Pick Chart Color", visualization.getChartColor());
        if (color != null) visualization.setChartColor(color);
    }


    private void summarizeChart() {

        if (!isChartDrawn || currentChartPanel == null) {
            JOptionPane.showMessageDialog(this,
                    "Please draw a chart first before requesting a summary!",
                    "No Chart Available",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String chartType = (String) chartTypeBox.getSelectedItem();
        String xAxis = (String) xColBox.getSelectedItem();
        String yAxis = (String) yColBox.getSelectedItem();

        if (chartType == null || xAxis == null || yAxis == null) {
            JOptionPane.showMessageDialog(this,
                    "Chart configuration is incomplete. Please ensure X-axis and Y-axis are selected.",
                    "Configuration Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {

            JDialog loadingDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Generating Summary", Dialog.ModalityType.MODELESS);
            loadingDialog.setLayout(new BorderLayout());
            JLabel loadingLabel = new JLabel("Analyzing chart with AI... Please wait.", SwingConstants.CENTER);
            loadingLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            loadingDialog.add(loadingLabel);
            loadingDialog.setSize(350, 100);
            loadingDialog.setLocationRelativeTo(this);
            loadingDialog.setVisible(true);

            BufferedImage chartImage = currentChartPanel.getChart().createBufferedImage(800, 600);

            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                     return visualization.getChartSummary(chartImage, chartType, xAxis, yAxis);
                }

                @Override
                protected void done() {
                    loadingDialog.dispose();
                    try {
                        String summary = get();
                        showSummaryDialog(summary);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(VisualizationPanel.this,
                                "Error generating summary: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error capturing chart: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSummaryDialog(String summary) {
        JDialog summaryDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "AI Chart Summary", Dialog.ModalityType.APPLICATION_MODAL);
        summaryDialog.setLayout(new BorderLayout(10, 10));
        summaryDialog.setSize(600, 400);

        JLabel titleLabel = new JLabel("AI-Generated Chart Analysis", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        summaryDialog.add(titleLabel, BorderLayout.NORTH);

        JTextArea summaryArea = new JTextArea(summary);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setLineWrap(true);
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        summaryArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(summaryArea);
        summaryDialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> summaryDialog.dispose());
        buttonPanel.add(closeBtn);
        summaryDialog.add(buttonPanel, BorderLayout.SOUTH);

        summaryDialog.setLocationRelativeTo(this);
        summaryDialog.setVisible(true);
    }
}