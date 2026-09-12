package com.sset.erp.ui.components;

import com.sset.erp.util.UITheme;
import java.awt.*;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Antigravity-styled Dark Surface Card with rounded corners and subtle border outline.
 */
public class ModernCard extends JPanel {
    private int cornerRadius = 16;
    private Color cardBackground = UITheme.CARD_BG;
    private boolean drawShadow = true;

    public ModernCard() {
        this(18);
    }

    public ModernCard(int padding) {
        setOpaque(false);
        setBorder(new EmptyBorder(padding, padding, padding, padding));
    }

    public void setCardBackground(Color color) {
        this.cardBackground = color;
        repaint();
    }

    public void setDrawShadow(boolean drawShadow) {
        this.drawShadow = drawShadow;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UITheme.applyQualityRendering(g2);

        int width = getWidth();
        int height = getHeight();

        // Dark ambient shadow
        if (drawShadow) {
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillRoundRect(2, 4, width - 4, height - 4, cornerRadius + 2, cornerRadius + 2);
        }

        // Card body (Deep dark obsidian surface)
        g2.setColor(cardBackground);
        g2.fillRoundRect(0, 0, width - 1, height - (drawShadow ? 3 : 1), cornerRadius, cornerRadius);

        // Thin futuristic border
        g2.setColor(UITheme.BORDER);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, width - 1, height - (drawShadow ? 3 : 1), cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
