package com.sset.erp.ui.components;

import com.sset.erp.util.UITheme;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

/**
 * Antigravity-styled Dark Password Input with show/hide toggle and focus glow.
 */
public class ModernPasswordField extends JPasswordField {
    private String placeholder;
    private boolean isFocused = false;
    private boolean isPasswordVisible = false;
    private final char defaultEchoChar;
    private int cornerRadius = 10;
    private final Rectangle toggleBounds = new Rectangle();

    public ModernPasswordField(String placeholder) {
        this.placeholder = placeholder;
        this.defaultEchoChar = getEchoChar();
        initUI();
    }

    private void initUI() {
        setOpaque(false);
        setFont(UITheme.FONT_BODY);
        setForeground(UITheme.TEXT_PRIMARY);
        setCaretColor(UITheme.PRIMARY);
        setBorder(new EmptyBorder(10, 16, 10, 52));

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

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (toggleBounds.contains(e.getPoint())) {
                    togglePasswordVisibility();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (toggleBounds.contains(e.getPoint())) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(new Cursor(Cursor.TEXT_CURSOR));
                }
            }
        });
    }

    public void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        setEchoChar(isPasswordVisible ? (char) 0 : defaultEchoChar);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UITheme.applyQualityRendering(g2);

        int width = getWidth();
        int height = getHeight();

        // Dark Background
        g2.setColor(UITheme.INPUT_BG);
        g2.fillRoundRect(1, 1, width - 2, height - 2, cornerRadius, cornerRadius);

        // Border
        if (isFocused) {
            g2.setColor(UITheme.BORDER_FOCUS);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(1, 1, width - 3, height - 3, cornerRadius, cornerRadius);
        } else {
            g2.setColor(UITheme.BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(1, 1, width - 2, height - 2, cornerRadius, cornerRadius);
        }

        // Placeholder
        if (getPassword().length == 0 && !isFocused && placeholder != null) {
            g2.setColor(UITheme.TEXT_MUTED);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (height - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, 16, y);
        }

        // Toggle label on right side
        int iconSize = 28;
        int iconX = width - iconSize - 16;
        int iconY = (height - iconSize) / 2;
        toggleBounds.setBounds(iconX - 6, iconY - 4, iconSize + 12, iconSize + 8);

        g2.setColor(isPasswordVisible ? UITheme.ACCENT_CYAN : UITheme.TEXT_MUTED);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString(isPasswordVisible ? "HIDE" : "SHOW", iconX - 8, iconY + 18);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 42));
    }
}
