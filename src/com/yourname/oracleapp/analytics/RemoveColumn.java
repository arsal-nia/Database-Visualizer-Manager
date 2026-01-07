package com.yourname.oracleapp.analytics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class RemoveColumn {

    private static final Color PRIMARY_BG = new Color(28, 48, 74);   // #1C304A (Main Background)
    private static final Color SECONDARY_BG = new Color(43, 74, 111); // #2B4A6F (Panel, Button BG)
    private static final Color FONT_LIGHT = new Color(217, 217, 217); // #D9D9D9 (Text/Foreground)

    public interface RemoveCallback { void onColumnRemoved(List<List<Object>> updatedRows); }

    public static void showDialog(Window owner, com.yourname.oracleapp.ui.Analytics.TableData tableData, List<List<Object>> workingRows, RemoveCallback callback) {
        JDialog dlg = new JDialog(owner, "Remove Column", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420,180);
        dlg.setLayout(new BorderLayout());
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(PRIMARY_BG);
        center.setBorder(new EmptyBorder(12,12,12,12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.EAST;
        JLabel lbl = new JLabel("Select Column:");
        lbl.setForeground(FONT_LIGHT);
        center.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        JComboBox<String> cols = new JComboBox<>();
        for (String c : tableData.getColumnNames()) cols.addItem(c);
        cols.setBackground(SECONDARY_BG);
        cols.setForeground(FONT_LIGHT);
        center.add(cols, gbc);

        dlg.add(center, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,20,10));
        btns.setBackground(PRIMARY_BG);
        JButton remove = new JButton("Remove");
        JButton cancel = new JButton("Cancel");
        remove.setBackground(SECONDARY_BG);
        remove.setForeground(FONT_LIGHT);
        cancel.setBackground(SECONDARY_BG);
        cancel.setForeground(FONT_LIGHT);

        remove.addActionListener(e -> {
            String sel = (String) cols.getSelectedItem();
            if (sel == null) { JOptionPane.showMessageDialog(dlg, "Select a column to remove."); return; }
            int idx = tableData.getColumnNames().indexOf(sel);
            if (idx < 0) { JOptionPane.showMessageDialog(dlg, "Column not found."); return; }

            if (tableData.getColumnNames().size() == 1) {
                JOptionPane.showMessageDialog(dlg, "Cannot remove the last remaining column.");
                return;
            }

            tableData.getColumnNames().remove(idx);
            tableData.getColumnTypes().remove(sel);
            for (List<Object> row : workingRows) {
                if (idx < row.size()) row.remove(idx);
            }
            callback.onColumnRemoved(workingRows);
            dlg.setVisible(false);
        });

        cancel.addActionListener(e -> dlg.setVisible(false));
        btns.add(cancel);
        btns.add(remove);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }
}