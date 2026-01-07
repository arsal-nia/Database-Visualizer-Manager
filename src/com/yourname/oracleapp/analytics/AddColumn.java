package com.yourname.oracleapp.analytics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.math.BigDecimal;

public class AddColumn {

    private static final Color PRIMARY_BG = new Color(28, 48, 74);
    private static final Color SECONDARY_BG = new Color(43, 74, 111);
    private static final Color FONT_LIGHT = new Color(217, 217, 217);
    private static final Color LIST_TABLE_BG = new Color(20, 35, 55);

    public interface ColumnCallback { void onColumnsAdded(List<List<Object>> updatedRows); }

    public static void showDialog(Window owner, com.yourname.oracleapp.ui.Analytics.TableData tableData, List<List<Object>> workingRows, ColumnCallback callback) {
        JDialog dlg = new JDialog(owner, "Add Column", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480,300);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(PRIMARY_BG);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(PRIMARY_BG);
        center.setBorder(new EmptyBorder(12,12,12,12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.EAST;
        JLabel la = new JLabel("Column 1:");
        la.setForeground(FONT_LIGHT);
        center.add(la, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        JComboBox<String> colA = new JComboBox<>();
        for (String c : tableData.getColumnNames()) colA.addItem(c);
        colA.setBackground(SECONDARY_BG);
        colA.setForeground(FONT_LIGHT);
        center.add(colA, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.EAST;
        JLabel lb = new JLabel("Column 2:");
        lb.setForeground(FONT_LIGHT);
        center.add(lb, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        JComboBox<String> colB = new JComboBox<>();
        for (String c : tableData.getColumnNames()) colB.addItem(c);
        colB.setBackground(SECONDARY_BG);
        colB.setForeground(FONT_LIGHT);
        center.add(colB, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.EAST;
        JLabel lc = new JLabel("Operation:");
        lc.setForeground(FONT_LIGHT);
        center.add(lc, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        JComboBox<String> ops = new JComboBox<>(new String[]{"Add","Subtract","Multiply","Divide"});
        ops.setBackground(SECONDARY_BG);
        ops.setForeground(FONT_LIGHT);
        center.add(ops, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.EAST;
        JLabel ln = new JLabel("New Column Name:");
        ln.setForeground(FONT_LIGHT);
        center.add(ln, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        JTextField newName = new JTextField();
        newName.setBackground(SECONDARY_BG);
        newName.setForeground(FONT_LIGHT);
        newName.setCaretColor(FONT_LIGHT);
        center.add(newName, gbc);

        dlg.add(center, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,20,10));
        btns.setBackground(PRIMARY_BG);
        JButton add = new JButton("Add");
        add.setBackground(SECONDARY_BG);
        add.setForeground(FONT_LIGHT);
        JButton cancel = new JButton("Cancel");
        cancel.setBackground(SECONDARY_BG);
        cancel.setForeground(FONT_LIGHT);

        add.addActionListener(e -> {
            String a = (String) colA.getSelectedItem();
            String b = (String) colB.getSelectedItem();
            String op = (String) ops.getSelectedItem();
            String newColName = newName.getText().trim();
            if (a == null || b == null || op == null || newColName.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please select columns, operation and provide new column name.");
                return;
            }
            int idxA = tableData.getColumnNames().indexOf(a);
            int idxB = tableData.getColumnNames().indexOf(b);
            boolean numA = isNumericType(tableData.getColumnTypes().get(a));
            boolean numB = isNumericType(tableData.getColumnTypes().get(b));
            if ((!numA || !numB) && !op.equals("Add")) {
                JOptionPane.showMessageDialog(dlg, "Non-numeric columns can only use 'Add' (concatenate).");
                return;
            }
            if (tableData.getColumnNames().contains(newColName)) {
                JOptionPane.showMessageDialog(dlg, "Column name already exists.");
                return;
            }
            tableData.getColumnNames().add(newColName);
            if (numA && numB) tableData.getColumnTypes().put(newColName, "NUMBER");
            else tableData.getColumnTypes().put(newColName, "VARCHAR2");
            for (List<Object> row : workingRows) {
                Object va = idxA < row.size() ? row.get(idxA) : null;
                Object vb = idxB < row.size() ? row.get(idxB) : null;
                Object newVal = null;
                if (numA && numB) {
                    BigDecimal da = toBigDecimalSafe(va);
                    BigDecimal db = toBigDecimalSafe(vb);
                    if (da == null || db == null) newVal = null;
                    else {
                        switch (op) {
                            case "Add": newVal = da.add(db); break;
                            case "Subtract": newVal = da.subtract(db); break;
                            case "Multiply": newVal = da.multiply(db); break;
                            case "Divide": if (db.compareTo(BigDecimal.ZERO) == 0) newVal = null; else newVal = da.divide(db, 6, BigDecimal.ROUND_HALF_UP); break;
                            default: newVal = null;
                        }
                    }
                } else {
                    String sa = va == null ? "" : va.toString();
                    String sb = vb == null ? "" : vb.toString();
                    newVal = sa + (sa.isEmpty()||sb.isEmpty() ? "" : " ") + sb;
                }
                row.add(newVal);
            }
            callback.onColumnsAdded(workingRows);
            dlg.setVisible(false);
        });

        cancel.addActionListener(e -> dlg.setVisible(false));
        btns.add(cancel);
        btns.add(add);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
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