package com.sset.erp.ui.components;

import com.sset.erp.util.UITheme;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Antigravity-styled Dark Text Input with glowing focus borders and dark canvas background.
 */
public class ModernTextField extends JTextField {
    private String placeholder;
    private boolean isFocused = false;
    private int cornerRadius = 10;

    public ModernTextField(String placeholder) {
        this.placeholder = placeholder;
        initUI();
    }

    private void initUI() {
        setOpaque(false);
        setFont(UITheme.FONT_BODY);
        setForeground(UITheme.TEXT_PRIMARY);
        setCaretColor(UITheme.PRIMARY);
        setBorder(new EmptyBorder(10, 16, 10, 16));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UITheme.applyQualityRendering(g2);

        int width = getWidth();
        int height = getHeight();

        // Dark Input Background
        g2.setColor(UITheme.INPUT_BG);
        g2.fillRoundRect(1, 1, width - 2, height - 2, cornerRadius, cornerRadius);

        // Border / Glow state
        if (isFocused) {
            g2.setColor(UITheme.BORDER_FOCUS);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(1, 1, width - 3, height - 3, cornerRadius, cornerRadius);
        } else {
            g2.setColor(UITheme.BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(1, 1, width - 2, height - 2, cornerRadius, cornerRadius);
        }

        // Placeholder text
        if (getText().isEmpty() && !isFocused && placeholder != null) {
            g2.setColor(UITheme.TEXT_MUTED);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (height - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, 16, y);
        }

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 42));
    }
}
