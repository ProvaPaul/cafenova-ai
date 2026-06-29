package com.smartcafe.view.components;

import com.smartcafe.config.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A JPanel that paints itself with rounded corners and an optional drop shadow.
 * Use this as the container for login/signup forms and dashboard stat tiles.
 */
public class CardPanel extends JPanel {

    private Color  background;
    private int    radius;
    private boolean shadow;

    public CardPanel() {
        this(AppConfig.COLOR_SURFACE, AppConfig.CARD_RADIUS, true);
    }

    public CardPanel(Color background, int radius, boolean shadow) {
        this.background = background;
        this.radius     = radius;
        this.shadow     = shadow;
        setOpaque(false);    // we paint the background ourselves
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        if (shadow) {
            // Soft multi-pass shadow
            for (int i = 6; i >= 1; i--) {
                g2.setColor(new Color(0, 0, 0, 12 * i));
                g2.fill(new RoundRectangle2D.Float(i, i + 2, w - i * 2f, h - i * 2f, radius, radius));
            }
        }

        g2.setColor(background);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius, radius));

        g2.dispose();
        super.paintComponent(g);
    }

    public void setCardBackground(Color c) { this.background = c; repaint(); }
    public void setRadius(int r)           { this.radius = r;     repaint(); }
}
