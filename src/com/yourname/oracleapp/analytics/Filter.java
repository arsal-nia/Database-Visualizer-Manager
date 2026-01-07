package com.yourname.oracleapp.analytics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.math.BigDecimal;

public class Filter {

    private static final Color PRIMARY_BG = new Color(28, 48, 74);
    private static final Color SECONDARY_BG = new Color(43, 74, 111);
    private static final Color FONT_LIGHT = new Color(217, 217, 217);

    public interface FilterCallback { void onFiltered(List<List<Object>> filteredRows); }

    public static void showDialog(Window owner, com.yourname.oracleapp.ui.Analytics.TableData tableData, List<List<Object>> workingRows, FilterCallback callback) {
        JDialog dlg = new JDialog(owner, "Filter Data", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(520, 200);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(PRIMARY_BG);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(PRIMARY_BG);
        center.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0.0;
        JLabel whereLabel = new JLabel("WHERE:");
        whereLabel.setForeground(FONT_LIGHT);
        whereLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        center.add(whereLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.3; gbc.anchor = GridBagConstraints.WEST;
        JComboBox<String> columnCombo = new JComboBox<>();
        for (String c : tableData.getColumnNames()) columnCombo.addItem(c);
        columnCombo.setBackground(SECONDARY_BG);
        columnCombo.setForeground(FONT_LIGHT);
        columnCombo.setPreferredSize(new Dimension(150, 26));
        center.add(columnCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.7; gbc.anchor = GridBagConstraints.WEST;
        JTextField conditionField = new JTextField();
        conditionField.setBackground(SECONDARY_BG);
        conditionField.setForeground(FONT_LIGHT);
        conditionField.setCaretColor(FONT_LIGHT);
        conditionField.setPreferredSize(new Dimension(250, 26));
        center.add(conditionField, gbc);

        dlg.add(center, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btns.setBackground(PRIMARY_BG);
        JButton cancel = new JButton("Cancel");
        cancel.setBackground(SECONDARY_BG);
        cancel.setForeground(FONT_LIGHT);
        JButton apply = new JButton("Apply");
        apply.setBackground(SECONDARY_BG);
        apply.setForeground(FONT_LIGHT);

        cancel.addActionListener(e -> dlg.setVisible(false));

        apply.addActionListener(e -> {
            String column = (String) columnCombo.getSelectedItem();
            String condition = conditionField.getText().trim();

            if (column == null || condition.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please select a column and enter a condition.");
                return;
            }

            int colIdx = tableData.getColumnNames().indexOf(column);
            if (colIdx < 0) {
                JOptionPane.showMessageDialog(dlg, "Invalid column selection.");
                return;
            }

            try {
                List<List<Object>> filtered = applyFilter(workingRows, colIdx, column, condition, tableData.getColumnTypes().get(column));
                callback.onFiltered(filtered);
                dlg.setVisible(false);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid query entered");
            }
        });

        btns.add(cancel);
        btns.add(apply);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    private static List<List<Object>> applyFilter(List<List<Object>> rows, int colIdx, String columnName, String condition, String colType) throws Exception {
        List<List<Object>> result = new ArrayList<>();

        String conditionLower = condition.toLowerCase().trim();

        for (List<Object> row : rows) {
            if (colIdx >= row.size()) continue;

            Object value = row.get(colIdx);

            if (evaluateCondition(value, conditionLower, colType)) {
                result.add(new ArrayList<>(row));
            }
        }

        return result;
    }

    private static boolean evaluateCondition(Object value, String condition, String colType) throws Exception {
        boolean isNumeric = isNumericType(colType);

        if (condition.startsWith("in ")) {
            return evaluateInCondition(value, condition.substring(3).trim());
        } else if (condition.startsWith("not in ")) {
            return !evaluateInCondition(value, condition.substring(7).trim());
        } else if (condition.startsWith("like ")) {
            return evaluateLikeCondition(value, condition.substring(5).trim());
        } else if (condition.startsWith("not like ")) {
            return !evaluateLikeCondition(value, condition.substring(9).trim());
        } else if (condition.startsWith("is null")) {
            return value == null;
        } else if (condition.startsWith("is not null")) {
            return value != null;
        } else if (condition.startsWith("between ")) {
            return evaluateBetweenCondition(value, condition.substring(8).trim(), isNumeric);
        } else if (condition.startsWith(">=")) {
            return evaluateComparison(value, condition.substring(2).trim(), ">=", isNumeric);
        } else if (condition.startsWith("<=")) {
            return evaluateComparison(value, condition.substring(2).trim(), "<=", isNumeric);
        } else if (condition.startsWith(">")) {
            return evaluateComparison(value, condition.substring(1).trim(), ">", isNumeric);
        } else if (condition.startsWith("<")) {
            return evaluateComparison(value, condition.substring(1).trim(), "<", isNumeric);
        } else if (condition.startsWith("=")) {
            return evaluateComparison(value, condition.substring(1).trim(), "=", isNumeric);
        } else if (condition.startsWith("!=") || condition.startsWith("<>")) {
            String op = condition.startsWith("!=") ? "!=" : "<>";
            return evaluateComparison(value, condition.substring(2).trim(), op, isNumeric);
        } else {
            throw new Exception("Invalid condition syntax");
        }
    }

    private static boolean evaluateInCondition(Object value, String inClause) throws Exception {
        if (!inClause.startsWith("(") || !inClause.endsWith(")")) {
            throw new Exception("Invalid IN syntax");
        }

        String content = inClause.substring(1, inClause.length() - 1);
        String[] values = content.split(",");

        String valueStr = value == null ? "" : value.toString();

        for (String v : values) {
            v = v.trim();
            if (v.startsWith("'") && v.endsWith("'")) {
                v = v.substring(1, v.length() - 1);
            }
            if (valueStr.equals(v)) {
                return true;
            }
        }

        return false;
    }

    private static boolean evaluateLikeCondition(Object value, String pattern) throws Exception {
        if (!pattern.startsWith("'") || !pattern.endsWith("'")) {
            throw new Exception("Invalid LIKE syntax");
        }

        pattern = pattern.substring(1, pattern.length() - 1);
        String valueStr = value == null ? "" : value.toString();

        String regex = pattern.replace("%", ".*").replace("_", ".");
        return valueStr.matches(regex);
    }

    private static boolean evaluateBetweenCondition(Object value, String betweenClause, boolean isNumeric) throws Exception {
        String[] parts = betweenClause.split(" and ");
        if (parts.length != 2) {
            throw new Exception("Invalid BETWEEN syntax");
        }

        String lower = parts[0].trim();
        String upper = parts[1].trim();

        if (isNumeric) {
            BigDecimal val = toBigDecimalSafe(value);
            BigDecimal lowerVal = new BigDecimal(lower);
            BigDecimal upperVal = new BigDecimal(upper);

            if (val == null) return false;
            return val.compareTo(lowerVal) >= 0 && val.compareTo(upperVal) <= 0;
        } else {
            if (lower.startsWith("'") && lower.endsWith("'")) {
                lower = lower.substring(1, lower.length() - 1);
            }
            if (upper.startsWith("'") && upper.endsWith("'")) {
                upper = upper.substring(1, upper.length() - 1);
            }

            String valueStr = value == null ? "" : value.toString();
            return valueStr.compareTo(lower) >= 0 && valueStr.compareTo(upper) <= 0;
        }
    }

    private static boolean evaluateComparison(Object value, String compareValue, String operator, boolean isNumeric) throws Exception {
        if (isNumeric) {
            BigDecimal val = toBigDecimalSafe(value);
            BigDecimal cmpVal = new BigDecimal(compareValue);

            if (val == null) return false;

            int cmp = val.compareTo(cmpVal);
            switch (operator) {
                case "<": return cmp < 0;
                case ">": return cmp > 0;
                case "=": return cmp == 0;
                case "<=": return cmp <= 0;
                case ">=": return cmp >= 0;
                case "!=":
                case "<>": return cmp != 0;
                default: return false;
            }
        } else {
            if (compareValue.startsWith("'") && compareValue.endsWith("'")) {
                compareValue = compareValue.substring(1, compareValue.length() - 1);
            }

            String valueStr = value == null ? "" : value.toString();
            int cmp = valueStr.compareTo(compareValue);

            switch (operator) {
                case "<": return cmp < 0;
                case ">": return cmp > 0;
                case "=": return cmp == 0;
                case "<=": return cmp <= 0;
                case ">=": return cmp >= 0;
                case "!=":
                case "<>": return cmp != 0;
                default: return false;
            }
        }
    }

    private static boolean isNumericType(String t) {
        if (t == null) return false;
        t = t.toUpperCase();
        return t.contains("NUMBER") || t.contains("INT") || t.contains("FLOAT") || t.contains("DECIMAL") || t.contains("DOUBLE");
    }

    private static BigDecimal toBigDecimalSafe(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return new BigDecimal(o.toString());
        try { return new BigDecimal(o.toString()); } catch (Exception ex) { return null; }
    }
}