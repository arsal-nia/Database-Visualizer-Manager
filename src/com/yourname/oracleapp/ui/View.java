package com.yourname.oracleapp.ui;

import com.yourname.oracleapp.view.*;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class View {

    private Connection conn;
    private SQLExecutor sqlExecutor;

    public View(Connection conn) {
        this.conn = conn;
        this.sqlExecutor = new SQLExecutor(conn);
    }



    public String[] getTables() {
        List<String> tables = new ArrayList<>();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, conn.getSchema(), "%", new String[]{"TABLE"});
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tables.toArray(new String[0]);
    }

    public String[] getColumns(String table) {
        List<String> cols = new ArrayList<>();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, conn.getSchema(), table, "%");
            while (rs.next()) {
                cols.add(rs.getString("COLUMN_NAME"));
            }
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return cols.toArray(new String[0]);
    }



    public DataResult loadData(String table, List<String> cols, String limit) {
        String colSQL = (cols == null || cols.isEmpty()) ? "*" : String.join(", ", cols);
        String sql = "SELECT " + colSQL + " FROM " + table;

        if (!"All".equalsIgnoreCase(limit)) {
            sql += " FETCH FIRST " + limit + " ROWS ONLY";
        }

        QueryResult qr = sqlExecutor.runSQL(sql);
        if (qr.getData() == null) {
            throw new RuntimeException(qr.getMessage());
        }
        return qr.getData();
    }

    public DataResult searchInData(DataResult data, String keyword) {
        List<Object[]> filtered = new ArrayList<>();

        for (Object[] row : data.getRows()) {
            for (Object cell : row) {
                if (cell != null && cell.toString().toLowerCase().contains(keyword.toLowerCase())) {
                    filtered.add(row);
                    break;
                }
            }
        }
        return new DataResult(data.getColumnNames(), filtered);
    }



    public QueryResult runSQL(String sql) {
        return sqlExecutor.runSQL(sql);
    }

    public List<String> getQueryHistory() {
        return sqlExecutor.getQueryHistory();
    }



    public void exportToCSV(DataResult data, File file) throws Exception {
        CSVExporter.export(data, file);
    }
}
