package com.smartcafe.config;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Central registry for application-wide constants: colours, fonts, sizes,
 * and the one-time FlatLaf theme setup call.
 *
 * Why FlatDarkLaf?
 *   Modern cafe staff environments often have high ambient light; a high-contrast
 *   dark theme reduces eye strain and looks premium on any monitor calibration.
 *
 * Why define colours here rather than per-component?
 *   A single source of truth means redesigning the palette is a one-file change.
 */
public final class AppConfig {

    // ── Application metadata ──────────────────────────────────────────────────
    public static final String APP_NAME    = "Smart Cafe Management System";
    public static final String APP_VERSION = "1.0.0";

    // ── Coffee-inspired palette ───────────────────────────────────────────────
    public static final Color COLOR_PRIMARY       = new Color(0x8B5E3C); // espresso brown
    public static final Color COLOR_PRIMARY_DARK  = new Color(0x5C3D1E); // dark roast
    public static final Color COLOR_PRIMARY_LIGHT = new Color(0xAD7C50); // medium roast
    public static final Color COLOR_ACCENT        = new Color(0xD4A96A); // latte cream

    public static final Color COLOR_SUCCESS = new Color(0x4CAF50);
    public static final Color COLOR_ERROR   = new Color(0xEF5350);
    public static final Color COLOR_WARNING = new Color(0xFF9800);
    public static final Color COLOR_INFO    = new Color(0x42A5F5);

    // ── Surfaces (dark-mode scale) ────────────────────────────────────────────
    public static final Color COLOR_BG          = new Color(0x12151A); // outermost background
    public static final Color COLOR_SURFACE      = new Color(0x1C2030); // card / panel
    public static final Color COLOR_SURFACE_2    = new Color(0x242840); // elevated card
    public static final Color COLOR_SIDEBAR_BG   = new Color(0x0E1117); // sidebar
    public static final Color COLOR_HEADER_BG    = new Color(0x1A1E28); // top header bar

    // ── Text ─────────────────────────────────────────────────────────────────
    public static final Color COLOR_TEXT_PRIMARY   = new Color(0xF0EAD6); // warm white
    public static final Color COLOR_TEXT_SECONDARY = new Color(0xA0AEC0); // muted
    public static final Color COLOR_TEXT_HINT      = new Color(0x5A6578); // placeholder

    // ── Border ───────────────────────────────────────────────────────────────
    public static final Color COLOR_BORDER       = new Color(0x2D3748);
    public static final Color COLOR_BORDER_FOCUS = COLOR_PRIMARY;

    // ── Fonts (Segoe UI is present on Windows; falls back gracefully elsewhere) ──
    public static final Font FONT_HEADING  = new Font("Segoe UI", Font.BOLD,  28);
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD,  12);
    public static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_SIDEBAR  = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_MONO     = new Font("Consolas",  Font.PLAIN, 12);

    // ── Layout constants ─────────────────────────────────────────────────────
    public static final int WINDOW_WIDTH   = 1280;
    public static final int WINDOW_HEIGHT  = 800;
    public static final int SIDEBAR_WIDTH  = 230;
    public static final int HEADER_HEIGHT  = 60;
    public static final int BORDER_RADIUS  = 12;
    public static final int CARD_RADIUS    = 14;
    public static final int FIELD_HEIGHT   = 42;

    private AppConfig() {}

    /**
     * Must be called before any Swing component is constructed.
     * FlatLaf replaces the default Metal L&F and then we layer on
     * per-component tweaks via UIManager.
     */
    public static void setupTheme() {
        FlatDarkLaf.setup();

        // Global arc for rounded corners
        UIManager.put("Button.arc",        BORDER_RADIUS);
        UIManager.put("Component.arc",     8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ProgressBar.arc",   4);
        UIManager.put("CheckBox.arc",      4);

        // Thin, pill-shaped scrollbar
        UIManager.put("ScrollBar.width",        8);
        UIManager.put("ScrollBar.thumbArc",    999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));

        // Focus ring colour — espresso brown instead of default blue
        UIManager.put("Component.focusColor",         COLOR_PRIMARY);
        UIManager.put("Component.focusedBorderColor", COLOR_PRIMARY);

        // Make the default font system-wide consistent
        UIManager.put("defaultFont", FONT_BODY);

        // Table header
        UIManager.put("TableHeader.font",       FONT_LABEL);
        UIManager.put("Table.rowHeight",        32);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines",   false);

        // Tooltips
        UIManager.put("ToolTip.font",        FONT_SMALL);
        UIManager.put("ToolTip.background",  COLOR_SURFACE_2);
        UIManager.put("ToolTip.foreground",  COLOR_TEXT_PRIMARY);
        UIManager.put("ToolTip.border",
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true));
    }
}
