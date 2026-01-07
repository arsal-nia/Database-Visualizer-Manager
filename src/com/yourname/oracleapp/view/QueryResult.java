package com.yourname.oracleapp.view;

public class QueryResult {

    private DataResult data;
    private String message;

    public QueryResult(DataResult data, String message) {
        this.data = data;
        this.message = message;
    }

    public DataResult getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
