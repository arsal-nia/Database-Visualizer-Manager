package com.yourname.oracleapp;

import javax.swing.*;
import com.yourname.oracleapp.ui.DBConnectionPanel;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DBConnectionPanel());
    }
}
