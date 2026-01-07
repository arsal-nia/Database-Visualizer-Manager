package com.yourname.oracleapp.ui;

import com.yourname.oracleapp.analytics.*; // Still import, but only for enums/callbacks
import com.yourname.oracleapp.ui.Analytics;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.*;
import java.util.List;

public class AnalyticsPanel extends JPanel {
    private static final Color PRIMARY_BG = new Color(28, 48, 74);
    private static final Color SECONDARY_BG = new Color(43, 74, 111);
    private static final Color FONT_LIGHT = new Color(217, 217, 217);
    private static final Color LIST_TABLE_BG = new Color(20, 35, 55);
    private static final Color GRID_COLOR = new Color(60, 90, 120);

    private Analytics analytics;
    private Connection conn;
    private JComboBox<String> tableCombo;
    private JButton findButton;
    private JButton outliersButton;
    private JButton sortButton;
    private JButton columnButton;
    private JButton filterButton;
    private JButton visualizeButton;
    private JPopupMenu findMenu;
    private JPopupMenu outlierMenu;
    private JPopupMenu columnMenu;
    private JPopupMenu filterMenu;
    private JTable resultTable;
    private JLabel statusLabel;
    private Analytics.TableData currentData;
    private List<List<Object>> originalRows;
    private List<List<Object>> workingRows;
    private List<List<Object>> baseDataForFilter;
    private JButton exportOutliersButton;
    private JButton exportDataButton;
    private JPanel topPanel;
    private JPanel controlPanel;
    private JComboBox<String> sortColumnCombo;
    private JComboBox<String> sortOrderCombo;
    private JDialog sortDialog;
    private boolean isFilterApplied = false;

    public interface BackListener { void onBack(); }

    public AnalyticsPanel(Connection conn, BackListener backListener) {
        this.analytics = new Analytics(conn);
        this.conn = conn;
        setLayout(new BorderLayout());
        setBackground(PRIMARY_BG);

        topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(SECONDARY_BG);
        topPanel.setBorder(new EmptyBorder(8,8,8,8));

        JPanel ribbon = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ribbon.setBackground(LIST_TABLE_BG);

        findButton = new JButton("Find");
        styleRibbonButton(findButton,140,36);
        outliersButton = new JButton("Outliers");
        styleRibbonButton(outliersButton,140,36);
        sortButton = new JButton("Sort");
        styleRibbonButton(sortButton,140,36);
        columnButton = new JButton("Column");
        styleRibbonButton(columnButton,140,36);
        filterButton = new JButton("Filter");
        styleRibbonButton(filterButton,140,36);
        visualizeButton = new JButton("Visualize");
        styleRibbonButton(visualizeButton,140,36);

        ribbon.add(findButton);
        ribbon.add(outliersButton);
        ribbon.add(sortButton);
        ribbon.add(columnButton);
        ribbon.add(filterButton);
        ribbon.add(visualizeButton);

        findMenu = new JPopupMenu();
        String[] findOps = {"Count","Sum","Average","Min","Max","Null Values","Mean","Median","Mode","StdDev"};
        int menuW = findButton.getPreferredSize().width;
        for (String s : findOps) {
            JMenuItem mi = new JMenuItem(s);
            mi.setPreferredSize(new Dimension(menuW,28));
            findMenu.add(mi);
            mi.addActionListener(e -> onFindSelected(s));
        }

        outlierMenu = new JPopupMenu();
        JMenuItem getOut = new JMenuItem("Get Outliers");
        JMenuItem removeOut = new JMenuItem("Remove Outliers");
        getOut.setPreferredSize(new Dimension(outliersButton.getPreferredSize().width,28));
        removeOut.setPreferredSize(new Dimension(outliersButton.getPreferredSize().width,28));
        outlierMenu.add(getOut);
        outlierMenu.add(removeOut);
        getOut.addActionListener(e -> onGetOutliers());
        removeOut.addActionListener(e -> onRemoveOutliers());

        columnMenu = new JPopupMenu();
        JMenuItem addCol = new JMenuItem("Add Column");
        JMenuItem removeCol = new JMenuItem("Remove Column");
        addCol.setPreferredSize(new Dimension(columnButton.getPreferredSize().width,28));
        removeCol.setPreferredSize(new Dimension(columnButton.getPreferredSize().width,28));
        columnMenu.add(addCol);
        columnMenu.add(removeCol);

        // Filter dropdown menu
        filterMenu = new JPopupMenu();
        JMenuItem addFilter = new JMenuItem("Add Filter");
        JMenuItem removeFilter = new JMenuItem("Remove Filter");
        addFilter.setPreferredSize(new Dimension(filterButton.getPreferredSize().width,28));
        removeFilter.setPreferredSize(new Dimension(filterButton.getPreferredSize().width,28));
        filterMenu.add(addFilter);
        filterMenu.add(removeFilter);

        findButton.addActionListener(e -> {
            if (currentData == null) { JOptionPane.showMessageDialog(this, "User Error! Select table first"); return; }
            findMenu.show(findButton,0,findButton.getHeight());
        });

        outliersButton.addActionListener(e -> {
            if (currentData == null) { JOptionPane.showMessageDialog(this, "User Error! Select table first"); return; }
            outlierMenu.show(outliersButton,0,outliersButton.getHeight());
        });

        sortButton.addActionListener(e -> {
            if (currentData == null) { JOptionPane.showMessageDialog(this, "User Error! Select table first"); return; }
            showSortDialog();
        });

        columnButton.addActionListener(e -> {
            if (currentData == null) { JOptionPane.showMessageDialog(this, "User Error! Select table first"); return; }
            columnMenu.show(columnButton,0,columnButton.getHeight());
        });


        filterButton.addActionListener(e -> {
            if (currentData == null) { JOptionPane.showMessageDialog(this, "User Error! Select table first"); return; }
            filterMenu.show(filterButton,0,filterButton.getHeight());
        });

        addFilter.addActionListener(e -> {
            if (currentData == null) { JOptionPane.showMessageDialog(this, "User Error! Select table first"); return; }

            // *** FIX: Call Analytics method instead of Filter utility directly ***
            analytics.openFilterDialog(SwingUtilities.getWindowAncestor(this), currentData, baseDataForFilter, filtered -> {
                workingRows = filtered;
                displayRows(currentData.getColumnNames(), workingRows);
                exportDataButton.setVisible(true);
                exportOutliersButton.setVisible(false);
                isFilterApplied = true;
                statusLabel.setText("Filter applied. Rows: " + workingRows.size());
            });
        });

        removeFilter.addActionListener(e -> {
            if (currentData == null) { JOptionPane.showMessageDialog(this, "User Error! Select table first"); return; }
            if (!isFilterApplied) {
                JOptionPane.showMessageDialog(this, "No filter is currently applied.");
                return;
            }

            workingRows = deepCopyRows(baseDataForFilter);
            displayRows(currentData.getColumnNames(), workingRows);
            isFilterApplied = false;
            statusLabel.setText("Filter removed. Showing " + workingRows.size() + " rows.");
        });

        visualizeButton.addActionListener(e -> {
            if (currentData == null) { JOptionPane.showMessageDialog(this, "User Error! Select table first"); return; }
            openVisualizationPanel();
        });

        addCol.addActionListener(e -> {
            if (currentData == null) { JOptionPane.showMessageDialog(this, "User Error! Select table first"); return; }
            // *** FIX: Call Analytics method instead of AddColumn utility directly ***
            analytics.openAddColumnDialog(SwingUtilities.getWindowAncestor(this), currentData, workingRows, updated -> {
                workingRows = updated;
                // Since columns/types may have changed, update base data
                baseDataForFilter = deepCopyRows(workingRows);
                displayRows(currentData.getColumnNames(), workingRows);
                exportDataButton.setVisible(true);
                exportOutliersButton.setVisible(false);
                isFilterApplied = false;
                statusLabel.setText("Added new column.");
            });
        });

        removeCol.addActionListener(e -> {
            if (currentData == null) { JOptionPane.showMessageDialog(this, "User Error! Select table first"); return; }
            // *** FIX: Call Analytics method instead of RemoveColumn utility directly ***
            analytics.openRemoveColumnDialog(SwingUtilities.getWindowAncestor(this), currentData, workingRows, updated -> {
                workingRows = updated;
                // Since columns/types may have changed, update base data
                baseDataForFilter = deepCopyRows(workingRows);
                displayRows(currentData.getColumnNames(), workingRows);
                exportDataButton.setVisible(true);
                exportOutliersButton.setVisible(false);
                isFilterApplied = false;
                statusLabel.setText("Column removed.");
            });
        });

        topPanel.add(ribbon, BorderLayout.NORTH);

        controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(PRIMARY_BG);
        controlPanel.setBorder(new EmptyBorder(12,12,12,12));

        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT,10,8));
        leftControls.setBackground(PRIMARY_BG);
        JLabel tblLabel = new JLabel("Table:");
        tblLabel.setForeground(FONT_LIGHT);
        tableCombo = new JComboBox<>();
        tableCombo.setBackground(SECONDARY_BG);
        tableCombo.setForeground(FONT_LIGHT);
        tableCombo.setPreferredSize(new Dimension(260,26));
        leftControls.add(tblLabel);
        leftControls.add(tableCombo);
        controlPanel.add(leftControls, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT,12,8));
        rightControls.setBackground(PRIMARY_BG);
        exportOutliersButton = new JButton("Export Outliers");
        exportOutliersButton.setBackground(LIST_TABLE_BG);
        exportOutliersButton.setForeground(Color.GREEN);
        exportOutliersButton.setPreferredSize(new Dimension(140,28));
        exportOutliersButton.setVisible(false);

        exportDataButton = new JButton("Export Data");
        exportDataButton.setBackground(LIST_TABLE_BG);
        exportDataButton.setForeground(Color.GREEN);
        exportDataButton.setPreferredSize(new Dimension(120,28));
        exportDataButton.setVisible(false);

        JButton backButton = new JButton("Back");
        backButton.setBackground(SECONDARY_BG);
        backButton.setForeground(FONT_LIGHT);
        backButton.setPreferredSize(new Dimension(80,26));

        rightControls.add(exportOutliersButton);
        rightControls.add(exportDataButton);
        rightControls.add(backButton);
        controlPanel.add(rightControls, BorderLayout.EAST);

        topPanel.add(controlPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setTopComponent(topPanel);

        resultTable = new JTable();
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultTable.setBackground(LIST_TABLE_BG);
        resultTable.setForeground(FONT_LIGHT);
        resultTable.setGridColor(GRID_COLOR);

        JTableHeader header = resultTable.getTableHeader();
        header.setBackground(SECONDARY_BG);
        header.setForeground(Color.WHITE);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(SECONDARY_BG);
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        header.setDefaultRenderer(headerRenderer);

        JScrollPane tableScroll = new JScrollPane(resultTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.getViewport().setBackground(PRIMARY_BG);
        split.setBottomComponent(tableScroll);
        split.setResizeWeight(0.40);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        statusLabel = new JLabel("Ready.");
        statusLabel.setForeground(FONT_LIGHT);
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(PRIMARY_BG);
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        try {
            analytics.loadTables();
            for (String t : analytics.getTables()) tableCombo.addItem(t);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading tables: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        tableCombo.addActionListener(e -> {
            String table = (String) tableCombo.getSelectedItem();
            if (table == null) return;
            try {
                currentData = analytics.loadTableData(table);
                originalRows = deepCopyRows(currentData.getRows());
                workingRows = deepCopyRows(originalRows);

                baseDataForFilter = deepCopyRows(originalRows);
                displayRows(currentData.getColumnNames(), workingRows);
                statusLabel.setText("Table " + table + " loaded.");
                hideAllExport();
                isFilterApplied = false;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading table data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Error occurred.");
            }
        });

        exportOutliersButton.addActionListener(e -> {
            if (workingRows == null) return;
            // *** FIX: Call Analytics method instead of Outliers utility directly ***
            List<List<Object>> outRows = analytics.getOutliers(currentData, workingRows);
            exportDataWithFileChooser(outRows, "outliers");
        });

        exportDataButton.addActionListener(e -> {
            if (workingRows == null) return;
            exportDataWithFileChooser(workingRows, "data");
        });

        backButton.addActionListener(e -> {
            if (backListener != null) backListener.onBack();
        });
    }

    private void exportDataWithFileChooser(List<List<Object>> rows, String defaultPrefix) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Data");

        String tableName = (String) tableCombo.getSelectedItem();
        String defaultFileName = tableName + "_" + defaultPrefix + ".csv";
        fileChooser.setSelectedFile(new File(defaultFileName));

        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Text Files (*.txt)", "txt");
        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.addChoosableFileFilter(txtFilter);
        fileChooser.setFileFilter(csvFilter);

        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();

            boolean isCsv = filePath.toLowerCase().endsWith(".csv");
            boolean isTxt = filePath.toLowerCase().endsWith(".txt");

            if (!isCsv && !isTxt) {
                if (fileChooser.getFileFilter() == csvFilter) {
                    filePath += ".csv";
                    isCsv = true;
                } else {
                    filePath += ".txt";
                    isTxt = true;
                }
            }

            try {
                exportRowsToFile(filePath, currentData.getColumnNames(), rows, isCsv);
                JOptionPane.showMessageDialog(this,
                        "Data exported successfully to:\n" + filePath,
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting file: " + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportRowsToFile(String filePath, List<String> columns, List<List<Object>> rows, boolean isCsv) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            String delimiter = isCsv ? "," : "\t";

            writer.println(String.join(delimiter, columns));

            for (List<Object> row : rows) {
                List<String> rowStrings = new ArrayList<>();
                for (Object obj : row) {
                    String value = (obj == null) ? "" : obj.toString();
                    if (isCsv && (value.contains(",") || value.contains("\"") || value.contains("\n"))) {
                        value = "\"" + value.replace("\"", "\"\"") + "\"";
                    }
                    rowStrings.add(value);
                }
                writer.println(String.join(delimiter, rowStrings));
            }
        }
    }

    private void styleRibbonButton(JButton b, int w, int h) {
        b.setPreferredSize(new Dimension(w,h));
        b.setBackground(LIST_TABLE_BG);
        b.setForeground(FONT_LIGHT);
        b.setFocusPainted(false);
        b.setMargin(new Insets(0,0,0,0));
    }

    private void onFindSelected(String name) {
        Find.Operation op;
        switch (name) {
            case "Count": op = Find.Operation.COUNT; break;
            case "Sum": op = Find.Operation.SUM; break;
            case "Average": op = Find.Operation.AVERAGE; break;
            case "Min": op = Find.Operation.MIN; break;
            case "Max": op = Find.Operation.MAX; break;
            case "Null Values": op = Find.Operation.NULLS; break;
            case "Mean": op = Find.Operation.MEAN; break;
            case "Median": op = Find.Operation.MEDIAN; break;
            case "Mode": op = Find.Operation.MODE; break;
            case "StdDev": op = Find.Operation.STDDEV; break;
            default: return;
        }
        applyFind(op);
    }

    private void applyFind(Find.Operation op) {
        if (currentData == null) { JOptionPane.showMessageDialog(this, "User Error! Select table first"); return; }

        // Change local variable type from generic Map to LinkedHashMap to match
        // the guaranteed return type of the Analytics facade method.
        LinkedHashMap<String, Object> ordered = analytics.applyFindOperation(op, currentData, workingRows);

        displayResultMap(ordered);
        hideAllExport();
        isFilterApplied = false;
        statusLabel.setText(op.name() + " applied.");
    }

    private void onGetOutliers() {
        if (currentData == null) { JOptionPane.showMessageDialog(this, "User Error! Select table first"); return; }
        // *** FIX: Call Analytics method instead of Outliers utility directly ***
        List<List<Object>> outRows = analytics.getOutliers(currentData, workingRows);

        if (outRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No outliers found.");
            resultTable.setModel(new DefaultTableModel());
            hideAllExport();
            statusLabel.setText("No outliers.");
            return;
        }
        displayRows(currentData.getColumnNames(), outRows);
        exportOutliersButton.setVisible(true);
        exportDataButton.setVisible(false);
        statusLabel.setText("Outliers found: " + outRows.size());
    }

    private void onRemoveOutliers() {
        if (currentData == null) { JOptionPane.showMessageDialog(this, "User Error! Select table first"); return; }

        // 1. Get outliers
        List<List<Object>> outRows = analytics.getOutliers(currentData, workingRows);

        if (outRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No outliers to remove.");
            statusLabel.setText("No outliers to remove.");
            return;
        }

        // 2. Remove outliers
        // *** FIX: Call Analytics method to handle the removal logic ***
        workingRows = analytics.removeOutliers(currentData, workingRows, outRows);

        baseDataForFilter = deepCopyRows(workingRows);
        displayRows(currentData.getColumnNames(), workingRows);
        exportDataButton.setVisible(true);
        exportOutliersButton.setVisible(false);
        isFilterApplied = false;
        statusLabel.setText("Removed outliers. New rows: " + workingRows.size());
    }

    private void showSortDialog() {
        if (sortDialog == null) {
            sortDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Sort", Dialog.ModalityType.APPLICATION_MODAL);
            sortDialog.setSize(380,180);
            sortDialog.setLayout(new BorderLayout());
            JPanel center = new JPanel(new GridBagLayout());
            center.setBackground(PRIMARY_BG);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6,6,6,6);

            gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
            JLabel lbl = new JLabel("Sort By:");
            lbl.setForeground(FONT_LIGHT);
            center.add(lbl, gbc);

            gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
            sortColumnCombo = new JComboBox<>();
            sortColumnCombo.setBackground(SECONDARY_BG);
            sortColumnCombo.setForeground(FONT_LIGHT);
            sortColumnCombo.setPreferredSize(new Dimension(200,26));
            center.add(sortColumnCombo, gbc);

            gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
            sortOrderCombo = new JComboBox<>(new String[]{"ASC","DESC"});
            sortOrderCombo.setBackground(SECONDARY_BG);
            sortOrderCombo.setForeground(FONT_LIGHT);
            sortOrderCombo.setPreferredSize(new Dimension(120,26));
            center.add(sortOrderCombo, gbc);

            sortDialog.add(center, BorderLayout.CENTER);

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,20,10));
            btns.setBackground(PRIMARY_BG);
            JButton apply = new JButton("Apply");
            JButton cancel = new JButton("Cancel");
            apply.addActionListener(e -> { applySort(); sortDialog.setVisible(false); });
            cancel.addActionListener(e -> sortDialog.setVisible(false));
            btns.add(apply);
            btns.add(cancel);
            sortDialog.add(btns, BorderLayout.SOUTH);
        }
        sortColumnCombo.removeAllItems();
        for (String c : currentData.getColumnNames()) sortColumnCombo.addItem(c);
        sortDialog.setLocationRelativeTo(this);
        sortDialog.setVisible(true);
    }

    private void applySort() {
        if (currentData == null) return;
        String col = (String) sortColumnCombo.getSelectedItem();
        String order = (String) sortOrderCombo.getSelectedItem();
        if (col == null || order == null) return;

        // *** FIX: Call Analytics method to perform the sort logic ***
        analytics.applySort(currentData, workingRows, col, order);

        displayRows(currentData.getColumnNames(), workingRows);
        exportDataButton.setVisible(true);
        exportOutliersButton.setVisible(false);
        // Don't reset filter state - sorting preserves filter
        statusLabel.setText("Sorted by " + col + " " + order);
    }

    private void openVisualizationPanel() {
        JFrame vizFrame = new JFrame("Visualization");
        vizFrame.setSize(1000, 700);
        vizFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // VisualizationPanel is still called directly as it's a separate UI component
        // that receives the data to visualize.
        VisualizationPanel vizPanel = new VisualizationPanel(
                conn,
                new ArrayList<>(currentData.getColumnNames()),
                deepCopyRows(workingRows),
                () -> vizFrame.dispose()
        );

        vizFrame.add(vizPanel);
        vizFrame.setLocationRelativeTo(this);
        vizFrame.setVisible(true);
    }

    private void displayResultMap(LinkedHashMap<String, Object> map) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Column");
        model.addColumn("Result");
        for (Map.Entry<String,Object> e : map.entrySet()) model.addRow(new Object[]{e.getKey(), e.getValue()});
        resultTable.setModel(model);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private void displayRows(List<String> cols, List<List<Object>> rows) {
        DefaultTableModel model = new DefaultTableModel();
        for (String c : cols) model.addColumn(c);
        for (List<Object> r : rows) model.addRow(r.toArray());
        resultTable.setModel(model);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    private void hideAllExport() {
        exportOutliersButton.setVisible(false);
        exportDataButton.setVisible(false);
    }

    private List<List<Object>> deepCopyRows(List<List<Object>> rows) {
        List<List<Object>> copy = new ArrayList<>();
        for (List<Object> r : rows) copy.add(new ArrayList<>(r));
        return copy;
    }
}