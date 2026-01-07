package com.yourname.oracleapp.model;

import javax.swing.table.AbstractTableModel;
import java.sql.*;

public class ResultSetTableModel extends AbstractTableModel {
    private Object[][] data;
    private String[] columnNames;

    public ResultSetTableModel(ResultSet rs) throws SQLException {
         loadResultSet(rs);
    }

    public ResultSetTableModel(Object[][] data, String[] columnNames) {
        this.data = data;
        this.columnNames = columnNames;
    }


    private void loadResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        columnNames = new String[colCount];
        for (int i = 1; i <= colCount; i++) {
            columnNames[i - 1] = meta.getColumnLabel(i);
        }

        rs.last();
        int rowCount = rs.getRow();
        rs.beforeFirst();

        data = new Object[rowCount][colCount];
        int rowIndex = 0;
        while (rs.next()) {
            for (int col = 1; col <= colCount; col++) {
                data[rowIndex][col - 1] = rs.getObject(col);
            }
            rowIndex++;
        }
    }

    @Override
    public int getRowCount() {

        return data != null ? data.length : 0;
    }

    @Override
    public int getColumnCount() {
        return columnNames != null ? columnNames.length : 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    public Object[][] getData() {
        return data;
    }

    public String[] getColumns() {
        return columnNames;
    }
}