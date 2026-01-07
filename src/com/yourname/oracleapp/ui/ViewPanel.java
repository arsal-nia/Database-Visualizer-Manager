package com.yourname.oracleapp.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.File;
import java.util.List;
import com.yourname.oracleapp.view.DataResult;
import com.yourname.oracleapp.view.QueryResult;


public class ViewPanel extends JPanel {

    private static final Color PRIMARY_BG = new Color(28, 48, 74);
    private static final Color SECONDARY_BG = new Color(43, 74, 111);
    private static final Color FONT_LIGHT = new Color(217, 217, 217);
    private static final Color LIST_TABLE_BG = new Color(20, 35, 55);
    private static final Color GRID_COLOR = new Color(60, 90, 120);

    private View viewLogic;
    private JList<String> tableList;
    private JList<String> columnList;
    private JTable dataTable;
    private JScrollPane dataScroll;
    private JPanel bottomPanel;
    private JComboBox<String> rowLimitBox;
    private JButton loadBtn, backBtn, loadColumnsBtn, runSQLBtn, exportBtn;
    private JTextArea sqlQueryArea;
    private JTextField searchBox;
    private JComboBox<String> historyBox;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private DataResult currentData;


    public ViewPanel(java.sql.Connection conn, Runnable onBack) {
        this.viewLogic = new View(conn);
        setLayout(new BorderLayout(10, 10));
        setBackground(PRIMARY_BG);

        JPanel leftPanel = initLeftPanel();
        initCenterTable();
        initBottomPanel(onBack);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dataScroll, bottomPanel);
        verticalSplit.setResizeWeight(0.7);
        verticalSplit.setOneTouchExpandable(true);
        verticalSplit.setBackground(PRIMARY_BG);

        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, verticalSplit);
        horizontalSplit.setOneTouchExpandable(true);
        horizontalSplit.setResizeWeight(0.25);
        horizontalSplit.setBackground(PRIMARY_BG);

        add(horizontalSplit, BorderLayout.CENTER);
    }

    private JPanel initLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setPreferredSize(new Dimension(250, 400));
        leftPanel.setBackground(SECONDARY_BG);

        tableList = new JList<>(viewLogic.getTables());
        tableList.setBackground(LIST_TABLE_BG);
        tableList.setForeground(FONT_LIGHT);
        JScrollPane tableScroll = new JScrollPane(tableList);

        TitledBorder tableBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GRID_COLOR),
                "Tables");
        tableBorder.setTitleColor(FONT_LIGHT);
        tableScroll.setBorder(tableBorder);
        tableScroll.getViewport().setBackground(LIST_TABLE_BG);

        columnList = new JList<>();
        columnList.setBackground(LIST_TABLE_BG);
        columnList.setForeground(FONT_LIGHT);
        JScrollPane columnScroll = new JScrollPane(columnList);

        TitledBorder colBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GRID_COLOR),
                "Columns");
        colBorder.setTitleColor(FONT_LIGHT);
        columnScroll.setBorder(colBorder);
        columnScroll.getViewport().setBackground(LIST_TABLE_BG);

        loadColumnsBtn = styledButton("Load Columns");
        loadColumnsBtn.addActionListener(e -> loadColumns());

        leftPanel.add(tableScroll, BorderLayout.NORTH);
        leftPanel.add(columnScroll, BorderLayout.CENTER);
        leftPanel.add(loadColumnsBtn, BorderLayout.SOUTH);

        return leftPanel;
    }

    private void initCenterTable() {
        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        dataTable.setBackground(LIST_TABLE_BG);
        dataTable.setForeground(FONT_LIGHT);
        dataTable.setGridColor(GRID_COLOR);
        dataTable.setSelectionBackground(new Color(55, 85, 125));

        dataTable.getTableHeader().setForeground(FONT_LIGHT);
        dataTable.getTableHeader().setBackground(SECONDARY_BG);
        dataTable.getTableHeader().setOpaque(true);


        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(SECONDARY_BG);
        headerRenderer.setForeground(FONT_LIGHT);
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        headerRenderer.setBorder(BorderFactory.createLineBorder(GRID_COLOR));
        dataTable.getTableHeader().setDefaultRenderer(headerRenderer);


        dataScroll = new JScrollPane(dataTable);
        dataScroll.getViewport().setBackground(PRIMARY_BG);
    }

    private void initBottomPanel(Runnable onBack) {
        bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(SECONDARY_BG);


        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBackground(SECONDARY_BG);

        rowLimitBox = new JComboBox<>(new String[]{"10", "50", "All"});
        styleComboBox(rowLimitBox);

        loadBtn = styledButton("Load Data");
        loadBtn.addActionListener(e -> loadData());

        exportBtn = styledButton("Export CSV");
        exportBtn.addActionListener(e -> exportToCSV());

        backBtn = styledButton("Back");
        backBtn.addActionListener(e -> onBack.run());

        JLabel rowsLabel = new JLabel("Rows:");
        rowsLabel.setForeground(FONT_LIGHT);

        searchBox = new JTextField(15);
        styleField(searchBox);
        JButton searchBtn = styledButton("Search");
        searchBtn.addActionListener(e -> searchTable());

        historyBox = new JComboBox<>();
        styleComboBox(historyBox);
        JButton loadHistoryBtn = styledButton("Load History");
        loadHistoryBtn.addActionListener(e -> loadHistoryQuery());

        controlPanel.add(rowsLabel);
        controlPanel.add(rowLimitBox);
        controlPanel.add(loadBtn);
        controlPanel.add(exportBtn);
        controlPanel.add(searchBox);
        controlPanel.add(searchBtn);
        controlPanel.add(historyBox);
        controlPanel.add(loadHistoryBtn);
        controlPanel.add(backBtn);

        JPanel sqlPanel = new JPanel(new BorderLayout(5, 5));
        sqlPanel.setBackground(SECONDARY_BG);
        sqlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GRID_COLOR),
                "Run SQL Query",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.PLAIN, 12), FONT_LIGHT));

        sqlQueryArea = new JTextArea(4, 50);
        styleField(sqlQueryArea);
        JScrollPane sqlScroll = new JScrollPane(sqlQueryArea);
        sqlScroll.getViewport().setBackground(LIST_TABLE_BG);
        sqlPanel.add(sqlScroll, BorderLayout.CENTER);

        runSQLBtn = styledButton("Run SQL");
        runSQLBtn.addActionListener(e -> runSQLQuery());
        sqlPanel.add(runSQLBtn, BorderLayout.SOUTH);

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(FONT_LIGHT);

        bottomPanel.add(controlPanel, BorderLayout.NORTH);
        bottomPanel.add(sqlPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
    }

    private JButton styledButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(SECONDARY_BG);
        b.setForeground(FONT_LIGHT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);

        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GRID_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(new Color(60, 100, 145));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(SECONDARY_BG);
            }
        });
        return b;
    }

    private void styleComboBox(JComboBox<String> c) {

        c.setBackground(SECONDARY_BG);
        c.setForeground(FONT_LIGHT);


        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GRID_COLOR, 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        c.setOpaque(true);
    }

    private void styleField(JComponent f) {
        f.setBackground(LIST_TABLE_BG);
        f.setForeground(FONT_LIGHT);

        if (f instanceof JTextComponent) {
            ((JTextComponent) f).setCaretColor(FONT_LIGHT);
        }
        f.setBorder(BorderFactory.createLineBorder(GRID_COLOR));
    }


    private void loadColumns() {
        String table = tableList.getSelectedValue();
        if (table == null) {
            JOptionPane.showMessageDialog(this, "Select a table first.");
            return;
        }
        columnList.setListData(viewLogic.getColumns(table));
    }

    private void loadData() {
        String table = tableList.getSelectedValue();
        if (table == null) {
            JOptionPane.showMessageDialog(this, "Select a table first.");
            return;
        }
        List<String> selectedCols = columnList.getSelectedValuesList();
        String limit = (String) rowLimitBox.getSelectedItem();
        currentData = viewLogic.loadData(table, selectedCols, limit);
        updateTable(currentData);
        statusLabel.setText("Loaded table: " + table);
    }

    private void updateTable(DataResult data) {
        tableModel.setColumnIdentifiers(data.getColumnNames());
        tableModel.setRowCount(0);

        for (Object[] row : data.getRows()) {
            tableModel.addRow(row);
        }
    }


    private void searchTable() {
        if (currentData == null || currentData.getRows().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Load a table or run a query first.");
            return;
        }

        String keyword = searchBox.getText().trim();
        if (keyword.isEmpty()) return;

        DataResult filtered = viewLogic.searchInData(currentData, keyword);

        if (filtered.getRows().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results found for \"" + keyword + "\".");
        }

        updateTable(filtered);
        statusLabel.setText("Search results for: \"" + keyword + "\"");
    }


    private void runSQLQuery() {
        String sql = sqlQueryArea.getText().trim();
        if (sql.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a SQL query first.");
            return;
        }

        QueryResult result = viewLogic.runSQL(sql);

        historyBox.removeAllItems();
        for (String q : viewLogic.getQueryHistory()) historyBox.addItem(q);

        if (result.getData() != null) {
            currentData = result.getData();
            updateTable(currentData);
        }

        statusLabel.setText(result.getMessage());

        String upper = sql.toUpperCase();
        if (upper.startsWith("CREATE TABLE") || upper.startsWith("DROP TABLE") ||
                upper.startsWith("TRUNCATE TABLE")) {
            tableList.setListData(viewLogic.getTables());
        }
    }


    private void loadHistoryQuery() {
        String selected = (String) historyBox.getSelectedItem();
        if (selected != null) sqlQueryArea.setText(selected);
    }

    private void exportToCSV() {
        if (currentData == null || currentData.getRows().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data to export.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                viewLogic.exportToCSV(currentData, file);
                statusLabel.setText("Data exported to: " + file.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + e.getMessage());
            }
        }
    }

}