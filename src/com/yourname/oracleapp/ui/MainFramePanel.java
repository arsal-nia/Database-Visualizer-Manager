package com.yourname.oracleapp.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.Connection;

public class MainFramePanel extends JPanel {

    private CardLayout cardLayout;
    private JPanel cards;
    private Connection conn;


    private static final Color DEEP_NAVY = new Color(30, 52, 73);
    private static final Color MID_BLUE = new Color(51, 84, 114);
    private static final Color PALE_GRAY = new Color(225, 227, 230);
    private static final Color SLATE_GRAY = new Color(102, 127, 145);

    private static final int BUTTON_WIDTH = 240;
    private static final int BUTTON_HEIGHT = 54;

    public MainFramePanel(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());
        setBackground(PALE_GRAY);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setBackground(PALE_GRAY);

        cards.add(createHomePanel(), "HOME");

        add(cards, BorderLayout.CENTER);
        cardLayout.show(cards, "HOME");
    }

    private JPanel createHomePanel() {
        JPanel homeWrapper = new JPanel(new BorderLayout());
        homeWrapper.setBackground(PALE_GRAY);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(DEEP_NAVY);
        sidebar.setPreferredSize(new Dimension(280, 800));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel appTitle = new JLabel("<html><b style='font-size:18px;'>DB MANAGER</b></html>", SwingConstants.CENTER);
        appTitle.setForeground(Color.WHITE);
        sidebar.add(appTitle, BorderLayout.NORTH);

        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(40, 0, 40, 0));

        navList.add(createNavButton("Insert / Update / Delete", "/icons/data_entry.png", e -> showCrudPanel()));
        navList.add(Box.createVerticalStrut(15));
        navList.add(createNavButton("View Data", "/icons/view_data.png", e -> showViewPanel()));
        navList.add(Box.createVerticalStrut(15));
        navList.add(createNavButton("Analytics", "/icons/analytics.png", e -> showAnalyticsPanel()));
        navList.add(Box.createVerticalStrut(15));
        navList.add(createNavButton("Visualization", "/icons/chart.png", e -> showVisualizationPanel()));

        sidebar.add(navList, BorderLayout.CENTER);

        JButton exitBtn = createNavButton("Exit", "/icons/disconnect.png", e -> System.exit(0));
        sidebar.add(exitBtn, BorderLayout.SOUTH);

        homeWrapper.add(sidebar, BorderLayout.WEST);

        JPanel contentArea = new JPanel(new GridBagLayout());
        contentArea.setBackground(PALE_GRAY);
        contentArea.setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel welcomeLabel = new JLabel("Welcome to SQL DB Manager", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        welcomeLabel.setForeground(DEEP_NAVY);

        JLabel instructLabel = new JLabel("<html><div style='text-align: center;'>Use the sidebar to navigate<br> and manage your data.</div></html>", SwingConstants.CENTER);
        instructLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        instructLabel.setForeground(SLATE_GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        contentArea.add(welcomeLabel, gbc);

        gbc.gridy = 1;
        contentArea.add(Box.createVerticalStrut(20), gbc);

        gbc.gridy = 2;
        contentArea.add(instructLabel, gbc);

        homeWrapper.add(contentArea, BorderLayout.CENTER);
        return homeWrapper;
    }

    private JButton createNavButton(String text, String iconPath, ActionListener action) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(MID_BLUE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        button.addActionListener(action);

        try {
            URL iconURL = getClass().getResource(iconPath);
            if (iconURL != null) {
                ImageIcon originalIcon = new ImageIcon(iconURL);
                Image image = originalIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(image));
                button.setHorizontalAlignment(SwingConstants.LEFT);
                button.setIconTextGap(15);
            }
        } catch (Exception ex) {
            System.err.println("Icon not found: " + iconPath);
        }

        button.addChangeListener(e -> {
            ButtonModel model = button.getModel();
            if (model.isRollover() || model.isPressed()) {
                button.setBackground(MID_BLUE.brighter());
            } else {
                button.setBackground(MID_BLUE);
            }
        });

        return button;
    }

    private void showViewPanel() {
        ViewPanel v = new ViewPanel(conn, this::showHome);
        cards.add(v, "VIEW");
        cardLayout.show(cards, "VIEW");
    }

    private void showCrudPanel() {
        CrudPanel c = new CrudPanel(conn, this::showHome);
        cards.add(c, "CRUD");
        cardLayout.show(cards, "CRUD");
    }

    private void showAnalyticsPanel() {
        AnalyticsPanel a = new AnalyticsPanel(conn, this::showHome);
        cards.add(a, "ANALYTICS");
        cardLayout.show(cards, "ANALYTICS");
    }

    private void showVisualizationPanel() {
        VisualizationPanel v = new VisualizationPanel(conn, null, null, this::showHome);
        cards.add(v, "VISUAL");
        cardLayout.show(cards, "VISUAL");
    }

    private void showHome() {
        cardLayout.show(cards, "HOME");
    }
}
