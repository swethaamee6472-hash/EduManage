package com.sset.erp.util;

import java.awt.*;
import javax.swing.border.Border;

/**
 * Centralized Design System & Theme configuration inspired by Google Antigravity.
 * Features a sleek dark theme, futuristic cyan & electric blue accents, and pill styling.
 */
public final class UITheme {
    private UITheme() {}

    // Professional Enterprise Light Palette
    public static final Color BG_DARK = new Color(241, 245, 249);      // #f1f5f9 Slate 100 (App Background)
    public static final Color BG_LIGHT = new Color(248, 250, 252);     // #f8fafc Slate 50 (Slightly lighter panels)
    public static final Color CARD_BG = new Color(255, 255, 255);      // #ffffff Card Surface
    public static final Color CARD_BG_HOVER = new Color(248, 250, 252);// Card Hover
    public static final Color INPUT_BG = new Color(255, 255, 255);     // Input Background

    public static final Color BORDER = new Color(226, 232, 240);       // #e2e8f0 Slate Outline
    public static final Color BORDER_FOCUS = new Color(26, 115, 232);  // Google Blue (#1a73e8)

    // Primary Accents
    public static final Color PRIMARY = new Color(26, 115, 232);       // #1a73e8 Deep Blue
    public static final Color PRIMARY_HOVER = new Color(21, 93, 168);  // Darker Blue
    public static final Color PRIMARY_LIGHT = new Color(232, 240, 254); // Light Blue Highlight

    public static final Color ACCENT_CYAN = new Color(14, 165, 233);   // Sky 500
    public static final Color ACCENT_PURPLE = new Color(168, 85, 247); // Electric Purple

    // Text hierarchy for Light Theme
    public static final Color TEXT_PRIMARY = new Color(30, 41, 59);    // #1e293b Slate 800 (Near black)
    public static final Color TEXT_SECONDARY = new Color(71, 85, 105); // #475569 Slate 600
    public static final Color TEXT_MUTED = new Color(148, 163, 184);   // #94a3b8 Slate 400

    // Google Semantic Colors
    public static final Color SUCCESS = new Color(21, 128, 61);        // Green 700
    public static final Color SUCCESS_BG = new Color(220, 252, 231);   // Green 100
    public static final Color DANGER = new Color(185, 28, 28);         // Red 700
    public static final Color DANGER_BG = new Color(254, 226, 226);    // Red 100
    public static final Color WARNING = new Color(180, 83, 9);         // Amber 700
    public static final Color WARNING_BG = new Color(254, 243, 199);   // Amber 100
    public static final Color INFO = new Color(29, 78, 216);           // Blue 700

    // Typography
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 17);
    public static final Font FONT_SUBHEADER = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BODY_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_BADGE = new Font("SansSerif", Font.BOLD, 11);

    /**
     * Creates a rounded border with specified radius and color.
     */
    public static Border createRoundedBorder(int radius, Color color) {
        return new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyQualityRendering(g2);
                g2.setColor(color);
                g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(8, 14, 8, 14);
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        };
    }

    /**
     * Configures antialiasing for text and 2D rendering.
     */
    public static void applyQualityRendering(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
}
