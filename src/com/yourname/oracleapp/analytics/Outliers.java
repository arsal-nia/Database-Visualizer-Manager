package com.yourname.oracleapp.analytics;

import java.util.*;
import java.io.*;
import java.math.BigDecimal;

public class Outliers {

    public List<List<Object>> detectRowsUsingIQR(List<String> columnNames, List<List<Object>> rows, Map<String,String> types) {
        int cols = columnNames.size();
        List<Range> numericRanges = new ArrayList<>(Collections.nCopies(cols, null));
        for (int c = 0; c < cols; c++) {
            String col = columnNames.get(c);
            String t = types.get(col);
            if (!isNumericType(t)) continue;
            List<BigDecimal> vals = new ArrayList<>();
            for (List<Object> r : rows) {
                Object o = r.get(c);
                if (o instanceof Number) vals.add(new BigDecimal(o.toString()));
                else if (o instanceof String) {
                    try { vals.add(new BigDecimal((String)o)); } catch (Exception ex) {}
                }
            }
            if (vals.size() < 4) continue;
            vals.sort(Comparator.naturalOrder());
            BigDecimal q1 = quantile(vals, 0.25);
            BigDecimal q3 = quantile(vals, 0.75);
            BigDecimal iqr = q3.subtract(q1);
            BigDecimal low = q1.subtract(iqr.multiply(new BigDecimal("1.5")));
            BigDecimal high = q3.add(iqr.multiply(new BigDecimal("1.5")));
            numericRanges.set(c, new Range(low, high));
        }
        LinkedHashSet<List<Object>> flagged = new LinkedHashSet<>();
        for (List<Object> r : rows) {
            boolean isOut = false;
            for (int c = 0; c < cols; c++) {
                Range rng = numericRanges.get(c);
                if (rng == null) continue;
                Object o = r.get(c);
                BigDecimal bd = null;
                if (o instanceof Number) bd = new BigDecimal(o.toString());
                else if (o instanceof String) {
                    try { bd = new BigDecimal((String)o); } catch (Exception ex) {}
                }
                if (bd == null) continue;
                if (bd.compareTo(rng.low) < 0 || bd.compareTo(rng.high) > 0) { isOut = true; break; }
            }
            if (isOut) flagged.add(new ArrayList<>(r));
        }
        return new ArrayList<>(flagged);
    }

    public void exportRows(String filename, List<String> columnNames, List<List<Object>> rows) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, false))) {
            for (int i = 0; i < columnNames.size(); i++) {
                pw.print(columnNames.get(i));
                if (i < columnNames.size() - 1) pw.print("\t");
            }
            pw.println();
            for (List<Object> r : rows) {
                for (int i = 0; i < r.size(); i++) {
                    Object v = r.get(i);
                    pw.print(v == null ? "" : v.toString());
                    if (i < r.size() - 1) pw.print("\t");
                }
                pw.println();
            }
        } catch (Exception e) {}
    }

    private boolean isNumericType(String t) {
        if (t == null) return false;
        t = t.toUpperCase();
        return t.contains("NUMBER") || t.contains("INT") || t.contains("FLOAT") || t.contains("DECIMAL") || t.contains("DOUBLE");
    }

    private static BigDecimal quantile(List<BigDecimal> sorted, double q) {
        int n = sorted.size();
        double p = q * (n - 1);
        int i = (int)p;
        double f = p - i;
        if (i + 1 < n) {
            BigDecimal a = sorted.get(i);
            BigDecimal b = sorted.get(i+1);
            return a.multiply(BigDecimal.valueOf(1 - f)).add(b.multiply(BigDecimal.valueOf(f)));
        } else {
            return sorted.get(i);
        }
    }

    private static class Range {
        BigDecimal low;
        BigDecimal high;
        Range(BigDecimal l, BigDecimal h) { low = l; high = h; }
    }
}
