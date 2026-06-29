package com.smartcafe.view.components;

import com.smartcafe.config.AppConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom-painted button with coffee-theme colours, rounded corners, and
 * smooth hover/press feedback.
 *
 * FlatLaf's default button already has rounded corners, but this component
 * gives us pixel-perfect colour control without fighting UIManager overrides.
 */
public class RoundedButton extends JButton {

    public enum Style { PRIMARY, SECONDARY, DANGER, SUCCESS, GHOST }

    private final Style style;
    private Color bgNormal, bgHover, bgPress, fgColor, borderColor;
    private boolean hovered, pressed;
    private int arc;

    public RoundedButton(String text, Style style) {
        this(text, style, AppConfig.BORDER_RADIUS);
    }

    public RoundedButton(String text, Style style, int arc) {
        super(text);
        this.style = style;
        this.arc   = arc;
        applyStyle();

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(AppConfig.FONT_BUTTON);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(fgColor);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e)  { hovered = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)   { hovered = false; repaint(); }
            @Override public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
            @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
        });
    }

    private void applyStyle() {
        switch (style) {
            case PRIMARY -> {
                bgNormal = AppConfig.COLOR_PRIMARY;
                bgHover  = AppConfig.COLOR_PRIMARY_LIGHT;
                bgPress  = AppConfig.COLOR_PRIMARY_DARK;
                fgColor  = Color.WHITE;
                borderColor = null;
            }
            case SECONDARY -> {
                bgNormal = new Color(0, 0, 0, 0);
                bgHover  = new Color(AppConfig.COLOR_PRIMARY.getRed(),
                                     AppConfig.COLOR_PRIMARY.getGreen(),
                                     AppConfig.COLOR_PRIMARY.getBlue(), 40);
                bgPress  = new Color(AppConfig.COLOR_PRIMARY.getRed(),
                                     AppConfig.COLOR_PRIMARY.getGreen(),
                                     AppConfig.COLOR_PRIMARY.getBlue(), 70);
                fgColor     = AppConfig.COLOR_PRIMARY;
                borderColor = AppConfig.COLOR_PRIMARY;
            }
            case DANGER -> {
                bgNormal = AppConfig.COLOR_ERROR;
                bgHover  = AppConfig.COLOR_ERROR.brighter();
                bgPress  = AppConfig.COLOR_ERROR.darker();
                fgColor  = Color.WHITE;
                borderColor = null;
            }
            case SUCCESS -> {
                bgNormal = AppConfig.COLOR_SUCCESS;
                bgHover  = AppConfig.COLOR_SUCCESS.brighter();
                bgPress  = AppConfig.COLOR_SUCCESS.darker();
                fgColor  = Color.WHITE;
                borderColor = null;
            }
            case GHOST -> {
                bgNormal = new Color(0, 0, 0, 0);
                bgHover  = new Color(255, 255, 255, 18);
                bgPress  = new Color(255, 255, 255, 35);
                fgColor  = AppConfig.COLOR_TEXT_SECONDARY;
                borderColor = AppConfig.COLOR_BORDER;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w = getWidth(), h = getHeight();
        Color bg = pressed ? bgPress : (hovered ? bgHover : bgNormal);

        // Border for secondary / ghost styles
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 2f, h - 2f, arc, arc));
        }

        // Fill
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        // Disabled overlay
        if (!isEnabled()) {
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
        }

        // Text
        g2.setColor(isEnabled() ? fgColor : AppConfig.COLOR_TEXT_HINT);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), tx, ty);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width + 28, Math.max(d.height + 10, AppConfig.FIELD_HEIGHT));
    }
}
