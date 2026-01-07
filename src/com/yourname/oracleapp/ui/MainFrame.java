package com.yourname.oracleapp.ui;

import javax.swing.*;
import java.sql.Connection;

public class MainFrame {

    private Connection conn;
    private JFrame frame;
    private MainFramePanel panel;

    public MainFrame(Connection conn) {
        this.conn = conn;
        init();
    }

    private void init() {
        frame = new JFrame("Oracle Swing Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new MainFramePanel(conn);
        frame.setContentPane(panel);

        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
