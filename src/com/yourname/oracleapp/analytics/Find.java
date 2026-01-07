package com.yourname.oracleapp.analytics;

import java.util.*;
import java.math.BigDecimal;

public class Find {
    public enum Operation {
        COUNT, SUM, AVERAGE, MIN, MAX, NULLS, MEAN, MEDIAN, MODE, STDDEV
    }

    public Map<String, Object> compute(Operation op, List<String> columnNames, List<List<Object>> rows, Map<String, String> columnTypes) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int c = 0; c < columnNames.size(); c++) {
            String col = columnNames.get(c);
            if (op == Operation.COUNT) {
                result.put(col, rows.size());
                continue;
            }
            if (op == Operation.NULLS) {
                int nulls = 0;
                for (List<Object> row : rows) {
                    Object o = row.size() > c ? row.get(c) : null;
                    if (o == null) nulls++;
                    else if (o instanceof String && ((String) o).trim().isEmpty()) nulls++;
                }
                result.put(col, nulls);
                continue;
            }
            boolean numeric = isNumericType(columnTypes.get(col));
            List<BigDecimal> vals = new ArrayList<>();
            for (List<Object> row : rows) {
                Object o = row.size() > c ? row.get(c) : null;
                if (o instanceof Number) vals.add(toBigDecimal((Number)o));
                else if (o instanceof String) {
                    try { vals.add(new BigDecimal((String)o)); } catch (Exception ex) {}
                }
            }
            if (!numeric || vals.isEmpty()) {
                result.put(col, "Not Applicable");
                continue;
            }
            switch (op) {
                case SUM:
                    BigDecimal sum = BigDecimal.ZERO;
                    for (BigDecimal b : vals) sum = sum.add(b);
                    result.put(col, stripTrailing(sum));
                    break;
                case AVERAGE:
                case MEAN:
                    BigDecimal total = BigDecimal.ZERO;
                    for (BigDecimal b : vals) total = total.add(b);
                    BigDecimal avg = total.divide(new BigDecimal(vals.size()), 6, BigDecimal.ROUND_HALF_UP);
                    result.put(col, stripTrailing(avg));
                    break;
                case MIN:
                    BigDecimal min = vals.get(0);
                    for (BigDecimal b : vals) if (b.compareTo(min) < 0) min = b;
                    result.put(col, stripTrailing(min));
                    break;
                case MAX:
                    BigDecimal max = vals.get(0);
                    for (BigDecimal b : vals) if (b.compareTo(max) > 0) max = b;
                    result.put(col, stripTrailing(max));
                    break;
                case MEDIAN:
                    vals.sort(Comparator.naturalOrder());
                    int n = vals.size();
                    if (n % 2 == 1) result.put(col, stripTrailing(vals.get(n/2)));
                    else {
                        BigDecimal a = vals.get(n/2 - 1);
                        BigDecimal b = vals.get(n/2);
                        result.put(col, stripTrailing(a.add(b).divide(new BigDecimal(2), 6, BigDecimal.ROUND_HALF_UP)));
                    }
                    break;
                case MODE:
                    Map<BigDecimal, Integer> freq = new HashMap<>();
                    for (BigDecimal b : vals) freq.put(b, freq.getOrDefault(b,0)+1);
                    int best = 0;
                    BigDecimal mode = null;
                    for (Map.Entry<BigDecimal,Integer> e : freq.entrySet()) {
                        if (e.getValue() > best || (e.getValue()==best && (mode==null || e.getKey().compareTo(mode)<0))) {
                            best = e.getValue();
                            mode = e.getKey();
                        }
                    }
                    result.put(col, stripTrailing(mode));
                    break;
                case STDDEV:
                    BigDecimal mean = BigDecimal.ZERO;
                    for (BigDecimal b : vals) mean = mean.add(b);
                    mean = mean.divide(new BigDecimal(vals.size()), 12, BigDecimal.ROUND_HALF_UP);
                    BigDecimal sumsq = BigDecimal.ZERO;
                    for (BigDecimal b : vals) {
                        BigDecimal d = b.subtract(mean);
                        sumsq = sumsq.add(d.multiply(d));
                    }
                    BigDecimal variance = sumsq.divide(new BigDecimal(vals.size()), 12, BigDecimal.ROUND_HALF_UP);
                    double sd = Math.sqrt(variance.doubleValue());
                    BigDecimal sdBd = new BigDecimal(Double.toString(sd));
                    result.put(col, stripTrailing(sdBd));
                    break;
                default:
                    result.put(col, "Not Applicable");
            }
        }
        return result;
    }

    private boolean isNumericType(String type) {
        if (type == null) return false;
        String t = type.toUpperCase();
        return t.contains("NUMBER") || t.contains("FLOAT") || t.contains("INT") || t.contains("DECIMAL") || t.contains("DOUBLE") || t.contains("NUM");
    }

    private BigDecimal toBigDecimal(Number n) {
        if (n instanceof BigDecimal) return (BigDecimal)n;
        if (n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte) return new BigDecimal(n.longValue());
        return BigDecimal.valueOf(n.doubleValue());
    }

    private Object stripTrailing(BigDecimal bd) {
        bd = bd.stripTrailingZeros();
        if (bd.scale() <= 0) return bd.toBigInteger();
        return bd;
    }
}
