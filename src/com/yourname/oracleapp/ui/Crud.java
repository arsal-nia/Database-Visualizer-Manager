package com.yourname.oracleapp.ui;

import java.sql.*;
import java.util.*;

public class Crud {
    private final Connection conn;

    public Crud(Connection conn) {
        this.conn = conn;
        try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
    }

    public String[] getTables() {
        List<String> tables = new ArrayList<>();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            String schema = conn.getSchema();
            if (schema == null) schema = meta.getUserName();
            ResultSet rs = meta.getTables(null, schema.toUpperCase(), "%", new String[]{"TABLE"});
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables.toArray(new String[0]);
    }

    public List<Map<String, String>> getColumns(String table) {
        List<Map<String, String>> cols = new ArrayList<>();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, conn.getSchema(), table, "%");
            while (rs.next()) {
                Map<String, String> col = new LinkedHashMap<>();
                col.put("name", rs.getString("COLUMN_NAME"));
                col.put("type", rs.getString("TYPE_NAME"));
                cols.add(col);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cols;
    }

    public String getPrimaryKey(String table) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getPrimaryKeys(null, conn.getSchema(), table);
            if (rs.next()) {
                String pk = rs.getString("COLUMN_NAME");
                rs.close();
                return pk;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Object[]> loadTableData(String table) {
        List<Object[]> rows = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            while (rs.next()) {
                Object[] row = new Object[colCount];
                for (int i = 0; i < colCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public boolean insert(String table, Map<String, String> data, boolean skipPK, String pkName) {
        try {
            StringBuilder cols = new StringBuilder();
            StringBuilder vals = new StringBuilder();
            for (String col : data.keySet()) {
                if (skipPK && col.equalsIgnoreCase(pkName)) continue;
                cols.append(col).append(", ");
                vals.append("?, ");
            }
            if (cols.length() == 0) return false;
            cols.setLength(cols.length() - 2);
            vals.setLength(vals.length() - 2);

            String sql = "INSERT INTO " + table + " (" + cols + ") VALUES (" + vals + ")";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int i = 1;
                for (String col : data.keySet()) {
                    if (skipPK && col.equalsIgnoreCase(pkName)) continue;
                    String v = data.get(col);
                    ps.setString(i++, v.isEmpty() ? null : v);
                }
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(String table, Map<String, String> data, String pkName) {
        try {
            StringBuilder set = new StringBuilder();
            for (String col : data.keySet()) {
                if (!col.equalsIgnoreCase(pkName)) {
                    set.append(col).append(" = ?, ");
                }
            }
            if (set.length() == 0) return false;
            set.setLength(set.length() - 2);

            String sql = "UPDATE " + table + " SET " + set + " WHERE " + pkName + " = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int i = 1;
                for (String col : data.keySet()) {
                    if (!col.equalsIgnoreCase(pkName)) {
                        String v = data.get(col);
                        ps.setString(i++, v.isEmpty() ? null : v);
                    }
                }
                ps.setString(i, data.get(pkName));
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String table, String pkName, String pkValue) {
        try {
            String sql = "DELETE FROM " + table + " WHERE " + pkName + " = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pkValue);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}