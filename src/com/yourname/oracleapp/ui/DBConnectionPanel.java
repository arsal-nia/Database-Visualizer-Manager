package com.yourname.oracleapp.ui;

import com.yourname.oracleapp.db.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;

public class DBConnectionPanel extends JFrame {


    private static final Color DEEP_NAVY = new Color(30, 52, 73);
    private static final Color MID_BLUE = new Color(51, 84, 114);
    private static final Color SLATE_GRAY = new Color(102, 127, 145);
    private static final Color PALE_GRAY = new Color(225, 227, 230);

    private JTextField urlField, userField;
    private JPasswordField passField;
    private JButton connectBtn;
    private JLabel statusLabel;

    static {
        setupLookAndFeel();
    }

    public DBConnectionPanel() {

        setTitle("ORACLE DB CONNECTOR");
        setPreferredSize(new Dimension(900, 600));
        setMinimumSize(new Dimension(900, 600));
        pack();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(PALE_GRAY);
        setLayout(new BorderLayout());
        setResizable(true);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(PALE_GRAY);
        mainPanel.setBorder(new EmptyBorder(50, 80, 50, 80));
        add(mainPanel, BorderLayout.CENTER);

        JPanel card = new RoundedPanel(new GridBagLayout(), 45, Color.WHITE);
        card.setPreferredSize(new Dimension(700, 420));

        card.setBorder(new EmptyBorder(50, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;


        JLabel heading = new JLabel("ORACLE DB CONNECTOR", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(DEEP_NAVY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(heading, gbc);

        gbc.gridy++; card.add(Box.createVerticalStrut(20), gbc);
        gbc.gridwidth = 1;


        JLabel urlLabel = styledLabel("Connection URL:");
        gbc.gridy++; gbc.gridx = 0;
        card.add(urlLabel, gbc);

        urlField = styledField("jdbc:oracle:thin:@//localhost:1521/orcl");
        gbc.gridx = 1;
        card.add(urlField, gbc);


        JLabel userLabel = styledLabel("Username:");
        gbc.gridy++; gbc.gridx = 0;
        card.add(userLabel, gbc);

        userField = styledField("");
        gbc.gridx = 1;
        card.add(userField, gbc);


        JLabel passLabel = styledLabel("Password:");
        gbc.gridy++; gbc.gridx = 0;
        card.add(passLabel, gbc);

        passField = new JPasswordField();
        stylePasswordField(passField);
        gbc.gridx = 1;
        card.add(passField, gbc);


        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        connectBtn = styledButton("Connect");
        card.add(connectBtn, gbc);

        gbc.gridy++;
        statusLabel = new JLabel(" Ready to connect", SwingConstants.CENTER);
        statusLabel.setForeground(SLATE_GRAY);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(statusLabel, gbc);

        mainPanel.add(card);


        connectBtn.addActionListener(e -> connectToDB());

        setVisible(true);
    }


    private JLabel styledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MID_BLUE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        return label;
    }

    private JTextField styledField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(Color.WHITE);
        field.setForeground(DEEP_NAVY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SLATE_GRAY, 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(DEEP_NAVY, 2, true),
                        new EmptyBorder(7, 7, 7, 7)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(SLATE_GRAY, 1, true),
                        new EmptyBorder(8, 8, 8, 8)
                ));
            }
        });
        return field;
    }

    private void stylePasswordField(JPasswordField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(Color.WHITE);
        field.setForeground(DEEP_NAVY);
        field.setCaretColor(DEEP_NAVY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SLATE_GRAY, 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));
    }

    private JButton styledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 17));
        button.setBackground(DEEP_NAVY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 25, 12, 25));

        button.addChangeListener(e -> {
            if (button.getModel().isRollover()) button.setBackground(MID_BLUE);
            else button.setBackground(DEEP_NAVY);
        });
        return button;
    }

    private static void setupLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) UIManager.setLookAndFeel(info.getClassName());
            }
        } catch (Exception ignored) {}
    }



    private void connectToDB() {
        String url = urlField.getText();
        String user = userField.getText();
        String pass = new String(passField.getPassword());

        DBConnection db = new DBConnection();
        try {
            Connection conn = db.connect(url, user, pass);
            statusLabel.setText("Connected successfully!");
            statusLabel.setForeground(DEEP_NAVY);

            SwingUtilities.invokeLater(() -> {
                dispose();
                new MainFrame(conn);
            });

        } catch (Exception ex) {
            statusLabel.setText("Connection failed: " + ex.getMessage());
            statusLabel.setForeground(Color.RED.darker());
        }
    }


    class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fillColor;

        public RoundedPanel(LayoutManager layout, int arc, Color fillColor) {
            super(layout);
            this.arc = arc;
            this.fillColor = fillColor;
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
        }
    }
}
