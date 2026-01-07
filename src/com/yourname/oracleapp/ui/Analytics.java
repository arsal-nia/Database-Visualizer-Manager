package com.yourname.oracleapp.ui;

import com.yourname.oracleapp.analytics.*; // Import the analytical utility classes
import com.yourname.oracleapp.analytics.AddColumn.ColumnCallback;
import com.yourname.oracleapp.analytics.Filter.FilterCallback;
import com.yourname.oracleapp.analytics.RemoveColumn.RemoveCallback;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.awt.Window;

public class Analytics {
    private Connection connection;
    private List<String> tables = new ArrayList<>();
    private Map<String, String> columnTypes = new HashMap<>();

    public Analytics(Connection conn) {
        this.connection = conn;
    }

    // Existing methods (loadTables, getTables, loadTableData, getColumnTypes, isNumericColumn) remain here

    public void loadTables() throws SQLException {
        tables.clear();
        DatabaseMetaData meta = connection.getMetaData();
        String schema = connection.getSchema();
        if (schema == null || schema.isBlank()) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT USER FROM dual")) {
                if (rs.next()) schema = rs.getString(1);
            }
        }
        ResultSet rs = meta.getTables(null, schema.toUpperCase(), "%", new String[]{"TABLE"});
        while (rs.next()) {
            tables.add(rs.getString("TABLE_NAME"));
        }
    }

    public List<String> getTables() {
        return tables;
    }

    public TableData loadTableData(String table) throws SQLException {
        columnTypes.clear();
        List<String> columnNames = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();
        String schema = connection.getSchema();
        if (schema == null || schema.isBlank()) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT USER FROM dual")) {
                if (rs.next()) schema = rs.getString(1);
            }
        }
        String sql = "SELECT * FROM " + table;
        try (Statement stmt = connection.createStatement(
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY)) {
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                String colName = meta.getColumnName(i);
                String type = meta.getColumnTypeName(i);
                columnNames.add(colName);
                columnTypes.put(colName, type);
            }
            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    row.add(rs.getObject(i));
                }
                rows.add(row);
            }
        }
        return new TableData(columnNames, rows, new HashMap<>(columnTypes));
    }

    public Map<String, String> getColumnTypes() {
        return new HashMap<>(columnTypes);
    }

    public boolean isNumericColumn(String name) {
        String t = columnTypes.get(name);
        if (t == null) return false;
        t = t.toUpperCase();
        return t.contains("NUMBER") || t.contains("DECIMAL") || t.contains("FLOAT") || t.contains("INT") || t.contains("DOUBLE");
    }

    // --- New Methods to centralize data manipulation logic ---

    /**
     * Delegates the find operation (SUM, AVG, MIN, MAX, etc.) to the Find utility.
     * @param op The operation to perform.
     * @param data The current TableData instance.
     * @param workingRows The rows to operate on.
     * @return A map of column names to the computed result.
     */
    public LinkedHashMap<String, Object> applyFindOperation(Find.Operation op, TableData data, List<List<Object>> workingRows) {
        Find finder = new Find();
        Map<String, Object> raw = finder.compute(op, data.getColumnNames(), workingRows, data.getColumnTypes());

        // This object is a LinkedHashMap
        LinkedHashMap<String, Object> ordered = new LinkedHashMap<>();
        for (String c : data.getColumnNames()) {
            ordered.put(c, raw.get(c));
        }
        return ordered; // Returns LinkedHashMap
    }

    /**
     * Delegates outlier detection using IQR to the Outliers utility.
     * @param data The current TableData instance.
     * @param workingRows The rows to operate on.
     * @return A list of rows identified as outliers.
     */
    public List<List<Object>> getOutliers(TableData data, List<List<Object>> workingRows) {
        Outliers outlierDetector = new Outliers();
        return outlierDetector.detectRowsUsingIQR(data.getColumnNames(), workingRows, data.getColumnTypes());
    }

    /**
     * Removes the identified outlier rows from the working set.
     * @param data The current TableData instance.
     * @param workingRows The full set of rows.
     * @param outlierRows The rows identified as outliers.
     * @return A new list of rows with outliers removed.
     */
    public List<List<Object>> removeOutliers(TableData data, List<List<Object>> workingRows, List<List<Object>> outlierRows) {
        List<List<Object>> newRows = new ArrayList<>();
        // Simple O(N*M) comparison for equality check - can be optimized for large datasets
        // but sufficient for typical analytical use cases.
        for (List<Object> r : workingRows) {
            boolean isOut = false;
            for (List<Object> or : outlierRows) {
                if (rowsEqual(r, or)) { // Use helper method for row comparison
                    isOut = true;
                    break;
                }
            }
            if (!isOut) newRows.add(new ArrayList<>(r));
        }
        return newRows;
    }

    /**
     * Opens the Add Column dialog.
     */
    public void openAddColumnDialog(Window owner, TableData data, List<List<Object>> workingRows, ColumnCallback callback) {
        AddColumn.showDialog(owner, data, workingRows, callback);
    }

    /**
     * Opens the Remove Column dialog.
     */
    public void openRemoveColumnDialog(Window owner, TableData data, List<List<Object>> workingRows, RemoveCallback callback) {
        RemoveColumn.showDialog(owner, data, workingRows, callback);
    }

    /**
     * Opens the Filter dialog.
     */
    public void openFilterDialog(Window owner, TableData data, List<List<Object>> baseDataForFilter, FilterCallback callback) {
        Filter.showDialog(owner, data, baseDataForFilter, callback);
    }

    /**
     * Applies the sorting logic to the current set of working rows.
     * @param data The current TableData instance.
     * @param workingRows The list of rows to be sorted (will be modified).
     * @param colName The column name to sort by.
     * @param order "ASC" or "DESC".
     */
    public void applySort(TableData data, List<List<Object>> workingRows, String colName, String order) {
        int idx = data.getColumnNames().indexOf(colName);
        if (idx < 0) return;

        boolean numeric = data.getColumnTypes().getOrDefault(colName, "").toUpperCase().matches(".*(NUMBER|INT|DECIMAL|FLOAT|DOUBLE|NUM).*");

        workingRows.sort((a, b) -> {
            Object oa = idx < a.size() ? a.get(idx) : null;
            Object ob = idx < b.size() ? b.get(idx) : null;

            if (numeric) {
                Double da = toDouble(oa);
                Double db = toDouble(ob);
                if (da == null && db == null) return 0;
                if (da == null) return -1;
                if (db == null) return 1;
                return da.compareTo(db);
            } else {
                String sa = oa == null ? "" : oa.toString();
                String sb = ob == null ? "" : ob.toString();
                if (sa.isEmpty() && sb.isEmpty()) return 0;
                if (sa.isEmpty()) return -1; // Null/Empty comes first
                if (sb.isEmpty()) return 1;  // Null/Empty comes first
                return sa.compareTo(sb);
            }
        });

        if ("DESC".equals(order)) {
            Collections.reverse(workingRows);
        }
    }

    // --- Helper Methods ---

    private Double toDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try { return Double.parseDouble(o.toString()); } catch (Exception ex) { return null; }
    }

    private boolean rowsEqual(List<Object> a, List<Object> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            Object oa = i < a.size() ? a.get(i) : null;
            Object ob = i < b.size() ? b.get(i) : null;
            if (oa == null && ob == null) continue;
            if (oa == null || ob == null) return false;
            if (!oa.equals(ob)) return false;
        }
        return true;
    }

    public static class TableData {
        private List<String> columnNames;
        private List<List<Object>> rows;
        private Map<String, String> columnTypes;
        public TableData(List<String> columnNames, List<List<Object>> rows, Map<String, String> columnTypes) {
            this.columnNames = columnNames;
            this.rows = rows;
            this.columnTypes = columnTypes;
        }
        public List<String> getColumnNames() {
            return columnNames;
        }
        public List<List<Object>> getRows() {
            return rows;
        }
        public Map<String, String> getColumnTypes() {
            return columnTypes;
        }
    }
}