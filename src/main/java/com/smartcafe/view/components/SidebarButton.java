package com.smartcafe.view.components;

import com.smartcafe.config.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Full-width navigation button for the dashboard sidebar.
 * Shows an icon (Unicode character) and a label, with active-state highlight.
 */
public class SidebarButton extends JButton {

    private boolean active   = false;
    private boolean hovered  = false;
    private final String icon;

    public SidebarButton(String icon, String label) {
        super(label);
        this.icon = icon;

        setHorizontalAlignment(SwingConstants.LEFT);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(AppConfig.FONT_SIDEBAR);
        setForeground(AppConfig.COLOR_TEXT_SECONDARY);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        setPreferredSize(new Dimension(AppConfig.SIDEBAR_WIDTH, 44));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e)  { hovered = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)   { hovered = false; repaint(); }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        setForeground(active ? AppConfig.COLOR_TEXT_PRIMARY : AppConfig.COLOR_TEXT_SECONDARY);
        repaint();
    }

    public boolean isActive() { return active; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        if (active) {
            // Coffee-brown left accent bar
            g2.setColor(AppConfig.COLOR_PRIMARY);
            g2.fillRect(0, 6, 3, h - 12);

            // Subtle highlight fill
            g2.setColor(new Color(
                    AppConfig.COLOR_PRIMARY.getRed(),
                    AppConfig.COLOR_PRIMARY.getGreen(),
                    AppConfig.COLOR_PRIMARY.getBlue(), 30));
            g2.fill(new RoundRectangle2D.Float(3, 2, w - 6f, h - 4f, 8, 8));
        } else if (hovered) {
            g2.setColor(new Color(255, 255, 255, 10));
            g2.fill(new RoundRectangle2D.Float(3, 2, w - 6f, h - 4f, 8, 8));
        }

        // Icon (emoji / Unicode symbol)
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        g2.setColor(active ? AppConfig.COLOR_ACCENT : AppConfig.COLOR_TEXT_SECONDARY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.drawString(icon, 16, h / 2 + 6);

        // Label
        g2.setFont(AppConfig.FONT_SIDEBAR);
        g2.setColor(getForeground());
        g2.drawString(getText(), 44, h / 2 + 5);

        g2.dispose();
    }
}
