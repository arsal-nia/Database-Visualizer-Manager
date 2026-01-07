package com.yourname.oracleapp.view;

import java.io.File;
import java.io.PrintWriter;

public class CSVExporter {

    public static void export(DataResult data, File file) throws Exception {
        try (PrintWriter pw = new PrintWriter(file)) {

            String[] cols = data.getColumnNames();
            for (int i = 0; i < cols.length; i++) {
                pw.print(cols[i]);
                if (i < cols.length - 1) pw.print(",");
            }
            pw.println();

            for (Object[] row : data.getRows()) {
                for (int i = 0; i < row.length; i++) {
                    String cell = row[i] == null ? "" : row[i].toString().replace("\"", "\"\"");
                    if (cell.contains(",") || cell.contains("\"")) {
                        cell = "\"" + cell + "\"";
                    }
                    pw.print(cell);
                    if (i < row.length - 1) pw.print(",");
                }
                pw.println();
            }
        }
    }
}
