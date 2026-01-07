package com.yourname.oracleapp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private Connection conn;


    public Connection connect(String url, String username, String password) throws SQLException {
        try {
            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);
            return conn;
        } catch (SQLException e) {
            throw new SQLException("Failed to connect: " + e.getMessage(), e);
        }
    }

    public void disconnect() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    public Connection getConnection() {
        return conn;
    }
}
