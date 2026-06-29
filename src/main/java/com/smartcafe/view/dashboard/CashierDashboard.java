package com.smartcafe.view.dashboard;

import com.smartcafe.config.AppConfig;
import com.smartcafe.controller.AuthController;
import com.smartcafe.model.User;
import com.smartcafe.util.SessionManager;
import com.smartcafe.view.components.CardPanel;
import com.smartcafe.view.components.RoundedButton;
import com.smartcafe.view.components.SidebarButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Cashier dashboard — POS and order management. */
public class CashierDashboard extends JPanel {

    private static final String[][] NAV = {
        {"🏠", "Dashboard"},
        {"🖥️", "POS"},
        {"📋", "My Orders"},
        {"🪑", "Tables"},
    };

    private final AuthController controller;

    public CashierDashboard(AuthController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(AppConfig.COLOR_BG);
        build();
    }

    private void build() {
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(AppConfig.COLOR_HEADER_BG);
        h.setPreferredSize(new Dimension(0, AppConfig.HEADER_HEIGHT));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)); left.setOpaque(false);
        JLabel cup = new JLabel("☕"); cup.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        JLabel nm  = new JLabel(AppConfig.APP_NAME);
        nm.setFont(AppConfig.FONT_LABEL); nm.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        left.add(cup); left.add(nm);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0)); right.setOpaque(false);
        User u = SessionManager.getCurrentUser();
        JLabel info = new JLabel("<html><b>" + (u != null ? u.getFullName() : "—") + "</b>"
                + " &nbsp;<span style='color:#8B5E3C;'>Cashier</span></html>");
        info.setFont(AppConfig.FONT_BODY); info.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        RoundedButton logout = new RoundedButton("Sign Out", RoundedButton.Style.GHOST);
        logout.addActionListener(e -> controller.logout());
        right.add(info); right.add(logout);

        h.add(left, BorderLayout.WEST); h.add(right, BorderLayout.EAST);
        h.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppConfig.COLOR_BORDER),
                new EmptyBorder(0, 20, 0, 20)));
        return h;
    }

    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setBackground(AppConfig.COLOR_SIDEBAR_BG);
        sb.setPreferredSize(new Dimension(AppConfig.SIDEBAR_WIDTH, 0));
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, AppConfig.COLOR_BORDER),
                new EmptyBorder(16, 0, 16, 0)));

        SidebarButton[] btns = new SidebarButton[NAV.length];
        for (int i = 0; i < NAV.length; i++) {
            SidebarButton btn = new SidebarButton(NAV[i][0], NAV[i][1]);
            btns[i] = btn;
            int idx = i;
            btn.addActionListener(e -> {
                for (SidebarButton b : btns) b.setActive(false);
                btns[idx].setActive(true);
                if (idx > 0) showComingSoon(NAV[idx][1]);
            });
            sb.add(btn);
        }
        btns[0].setActive(true);
        sb.add(Box.createVerticalGlue());
        return sb;
    }

    private JScrollPane buildContent() {
        JPanel content = new JPanel();
        content.setBackground(AppConfig.COLOR_BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(28, 28, 28, 28));

        User u = SessionManager.getCurrentUser();
        JLabel welcome = new JLabel("Ready to serve, " + (u != null ? u.getFullName() : "Cashier") + " 👋");
        welcome.setFont(AppConfig.FONT_TITLE); welcome.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Take orders and process payments.");
        sub.setFont(AppConfig.FONT_SUBTITLE); sub.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false); stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String[] d : new String[][]{
                {"📋","My Orders Today","—"}, {"💵","Cash Collected","—"}, {"⏳","Pending Orders","—"}
        }) {
            CardPanel c = new CardPanel(AppConfig.COLOR_SURFACE, AppConfig.CARD_RADIUS, true);
            c.setLayout(new BorderLayout()); c.setBorder(new EmptyBorder(16, 18, 16, 18));
            JLabel ic = new JLabel(d[0]); ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
            JLabel vl = new JLabel(d[2]); vl.setFont(AppConfig.FONT_HEADING.deriveFont(26f));
            vl.setForeground(AppConfig.COLOR_TEXT_PRIMARY);
            JLabel ll = new JLabel(d[1]); ll.setFont(AppConfig.FONT_SMALL);
            ll.setForeground(AppConfig.COLOR_TEXT_SECONDARY);
            JPanel bot = new JPanel(); bot.setOpaque(false);
            bot.setLayout(new BoxLayout(bot, BoxLayout.Y_AXIS)); bot.add(vl); bot.add(ll);
            JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false); top.add(ic, BorderLayout.WEST);
            c.add(top, BorderLayout.NORTH); c.add(bot, BorderLayout.CENTER);
            stats.add(c);
        }

        content.add(welcome); content.add(Box.createVerticalStrut(4));
        content.add(sub);     content.add(Box.createVerticalStrut(28));
        content.add(stats);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null); scroll.setBackground(AppConfig.COLOR_BG);
        scroll.getViewport().setBackground(AppConfig.COLOR_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void showComingSoon(String mod) {
        JOptionPane.showMessageDialog(this,
                "<html><b>" + mod + "</b> will be available in the next implementation step.</html>",
                "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }
}
