package com.sset.erp.ui.components;

import com.sset.erp.util.UITheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * Antigravity-styled Pill Button with rounded capsule corners, smooth hover effects,
 * and high-contrast dark aesthetic styling.
 */
public class ModernButton extends JButton {
    public enum Variant { PRIMARY, SECONDARY, DANGER, OUTLINE, GHOST }

    private final Variant variant;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private int cornerRadius = 999; // Antigravity pill style

    public ModernButton(String text) {
        this(text, Variant.PRIMARY);
    }

    public ModernButton(String text, Variant variant) {
        super(text);
        this.variant = variant;
        initUI();
    }

    private void initUI() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(UITheme.FONT_BODY_BOLD);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(10, 24, 10, 24));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UITheme.applyQualityRendering(g2);

        int width = getWidth();
        int height = getHeight();
        int pillRadius = (cornerRadius >= 999) ? height : cornerRadius;

        Color bgColor;
        Color textColor;
        Color borderColor = null;

        switch (variant) {
            case PRIMARY:
                bgColor = isPressed ? UITheme.PRIMARY_HOVER.darker() : (isHovered ? UITheme.PRIMARY_HOVER : UITheme.PRIMARY);
                textColor = Color.WHITE;
                break;
            case SECONDARY:
                bgColor = isPressed ? new Color(24, 32, 48) : (isHovered ? new Color(34, 44, 66) : new Color(28, 36, 54));
                textColor = UITheme.TEXT_PRIMARY;
                borderColor = UITheme.BORDER;
                break;
            case DANGER:
                bgColor = isPressed ? new Color(185, 28, 28) : (isHovered ? new Color(220, 38, 38) : UITheme.DANGER);
                textColor = Color.WHITE;
                break;
            case OUTLINE:
                bgColor = isHovered ? new Color(49, 134, 255, 35) : new Color(255, 255, 255, 8);
                textColor = isHovered ? UITheme.ACCENT_CYAN : UITheme.TEXT_PRIMARY;
                borderColor = isHovered ? UITheme.PRIMARY : UITheme.BORDER;
                break;
            case GHOST:
                bgColor = isHovered ? new Color(255, 255, 255, 15) : new Color(0, 0, 0, 0);
                textColor = isHovered ? Color.WHITE : UITheme.TEXT_SECONDARY;
                break;
            default:
                bgColor = UITheme.PRIMARY;
                textColor = Color.WHITE;
        }

        // Background
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, width, height, pillRadius, pillRadius);

        // Border if specified
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, width - 2, height - 2, pillRadius, pillRadius);
        }

        // Text
        g2.setColor(textColor);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (width - fm.stringWidth(getText())) / 2;
        int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width + 28, 110), Math.max(d.height + 12, 42));
    }
}
