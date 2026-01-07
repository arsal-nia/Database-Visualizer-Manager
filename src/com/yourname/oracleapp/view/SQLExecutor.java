package com.yourname.oracleapp.view;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLExecutor {

    private Connection conn;
    private List<String> queryHistory = new ArrayList<>();

    public SQLExecutor(Connection conn) {
        this.conn = conn;
    }

    public List<String> getQueryHistory() {
        return queryHistory;
    }

    public QueryResult runSQL(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return new QueryResult(null, "No SQL query provided.");
        }

        sql = sql.replaceAll("[\n\r\t]", " ").trim();
        if (sql.endsWith(";")) sql = sql.substring(0, sql.length() - 1);

        queryHistory.add(sql);

        try (Statement stmt = conn.createStatement()) {
            boolean hasResultSet = stmt.execute(sql);

            if (hasResultSet) {

                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();

                    String[] colNames = new String[colCount];
                    for (int i = 0; i < colCount; i++) colNames[i] = meta.getColumnName(i + 1);

                    List<Object[]> rows = new ArrayList<>();
                    while (rs.next()) {
                        Object[] row = new Object[colCount];
                        for (int i = 0; i < colCount; i++) row[i] = rs.getObject(i + 1);
                        rows.add(row);
                    }

                    return new QueryResult(new DataResult(colNames, rows), "Query executed successfully.");
                }
            } else {

                int count = stmt.getUpdateCount();
                if (count == -1) {
                    if (!conn.getAutoCommit()) conn.commit();
                    return new QueryResult(null, "DDL executed successfully.");
                } else {
                    if (!conn.getAutoCommit()) conn.commit();
                    return new QueryResult(null, count + " row(s) affected.");
                }
            }

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignore) {}
            return new QueryResult(null, "SQL Error: " + e.getMessage());
        }
    }
}
