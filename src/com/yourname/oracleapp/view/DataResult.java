package com.yourname.oracleapp.view;

import java.util.List;

public class DataResult {

    private String[] columnNames;
    private List<Object[]> rows;

    public DataResult(String[] columnNames, List<Object[]> rows) {
        this.columnNames = columnNames;
        this.rows = rows;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public List<Object[]> getRows() {
        return rows;
    }
}
