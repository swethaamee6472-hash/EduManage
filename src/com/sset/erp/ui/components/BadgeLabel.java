package com.sset.erp.ui.components;

import com.sset.erp.model.Role;
import com.sset.erp.util.UITheme;
import java.awt.*;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 * Antigravity-styled Pill Badge for roles and status tags.
 */
public class BadgeLabel extends JLabel {
    private Color bgColor;
    private Color fgColor;
    private int cornerRadius = 999;

    public BadgeLabel(String text, Color fgColor, Color bgColor) {
        super(text);
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        initUI();
    }

    public static BadgeLabel forRole(Role role) {
        return new BadgeLabel(role.getDisplayName(), role.getPrimaryColor(), role.getBadgeBgColor());
    }

    public static BadgeLabel forStatus(boolean active) {
        if (active) {
            return new BadgeLabel("Active", UITheme.SUCCESS, UITheme.SUCCESS_BG);
        } else {
            return new BadgeLabel("Deactivated", UITheme.DANGER, UITheme.DANGER_BG);
        }
    }

    private void initUI() {
        setOpaque(false);
        setFont(UITheme.FONT_BADGE);
        setForeground(fgColor);
        setHorizontalAlignment(CENTER);
        setBorder(new EmptyBorder(5, 12, 5, 12));
    }

    public void setColors(Color fgColor, Color bgColor) {
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        setForeground(fgColor);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UITheme.applyQualityRendering(g2);

        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height);

        // Pill translucent background
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, width, height, radius, radius);

        // Subtle glowing border
        g2.setColor(new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), 120));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}
