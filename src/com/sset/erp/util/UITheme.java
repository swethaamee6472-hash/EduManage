package com.sset.erp.util;

import java.awt.*;
import javax.swing.border.Border;

/**
 * Centralized Design System & Theme configuration inspired by Google Antigravity.
 * Features a sleek dark theme, futuristic cyan & electric blue accents, and pill styling.
 */
public final class UITheme {
    private UITheme() {}

    // Google Antigravity Dark Palette
    public static final Color BG_DARK = new Color(11, 15, 23);         // #0b0f17 Deep Space Obsidian
    public static final Color BG_LIGHT = new Color(15, 20, 31);        // #0f141f Dark Canvas
    public static final Color CARD_BG = new Color(22, 28, 42);         // #161c2a Card Surface
    public static final Color CARD_BG_HOVER = new Color(28, 36, 54);   // Card Hover
    public static final Color INPUT_BG = new Color(13, 18, 28);        // Input Background

    public static final Color BORDER = new Color(38, 48, 68);          // Slate Outline
    public static final Color BORDER_FOCUS = new Color(49, 134, 255);  // Antigravity Google Blue (#3186ff)

    // Primary Accents
    public static final Color PRIMARY = new Color(49, 134, 255);       // #3186ff Antigravity Blue
    public static final Color PRIMARY_HOVER = new Color(26, 115, 232); // #1a73e8 Deep Blue
    public static final Color PRIMARY_LIGHT = new Color(20, 38, 70);   // Translucent Blue Highlight

    public static final Color ACCENT_CYAN = new Color(0, 229, 255);    // Neon Cyan
    public static final Color ACCENT_PURPLE = new Color(168, 85, 247); // Electric Purple

    // Text hierarchy for Dark Theme
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);  // High contrast white
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);// Slate 400
    public static final Color TEXT_MUTED = new Color(100, 116, 139);    // Slate 500

    // Google Semantic Colors
    public static final Color SUCCESS = new Color(0, 185, 92);         // Google Green (#00b95c)
    public static final Color SUCCESS_BG = new Color(16, 48, 32);
    public static final Color DANGER = new Color(252, 65, 61);         // Google Red (#fc413d)
    public static final Color DANGER_BG = new Color(48, 20, 22);
    public static final Color WARNING = new Color(251, 188, 4);        // Google Yellow (#fbbc04)
    public static final Color WARNING_BG = new Color(48, 40, 16);
    public static final Color INFO = new Color(49, 134, 255);          // Google Blue

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
