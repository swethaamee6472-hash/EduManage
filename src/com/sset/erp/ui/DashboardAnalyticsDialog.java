package com.sset.erp.ui;

import com.sset.erp.model.AssessmentRecord;
import com.sset.erp.model.AttendanceRecord;
import com.sset.erp.model.DashboardAnalytics;
import com.sset.erp.model.User;
import com.sset.erp.repository.FileAssessmentRepository;
import com.sset.erp.repository.FileAttendanceRepository;
import com.sset.erp.service.AuthService;
import com.sset.erp.ui.components.ModernButton;
import com.sset.erp.ui.components.ModernCard;
import com.sset.erp.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Administrator analytics view summarizing users, attendance activity, and assessment results.
 */
public class DashboardAnalyticsDialog extends JDialog {
    private final AuthService authService;
    private final JPanel metricsPanel = new JPanel(new java.awt.GridLayout(1, 4, 12, 0));
    private final JPanel chartsPanel = new JPanel(new java.awt.GridLayout(0, 1, 0, 12));

    public DashboardAnalyticsDialog(JFrame owner, AuthService authService) {
        super(owner, "Dashboard Analytics", true);
        this.authService = authService;
        initUI();
        refreshAnalytics();
    }

    private void initUI() {
        setSize(1080, 760);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(12, 12));

        JLabel heading = new JLabel("Institutional Dashboard Analytics");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.TEXT_PRIMARY);
        heading.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        add(heading, BorderLayout.NORTH);

        metricsPanel.setOpaque(false);
        chartsPanel.setOpaque(false);
        JPanel content = new JPanel(new BorderLayout(12, 16));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        content.add(metricsPanel, BorderLayout.NORTH);
        content.add(chartsPanel, BorderLayout.CENTER);
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        ModernButton refresh = new ModernButton("Refresh Analytics", ModernButton.Variant.OUTLINE);
        ModernButton close = new ModernButton("Close", ModernButton.Variant.OUTLINE);
        refresh.addActionListener(e -> refreshAnalytics());
        close.addActionListener(e -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        actions.add(refresh);
        actions.add(close);
        add(actions, BorderLayout.SOUTH);
    }

    private void refreshAnalytics() {
        try {
            List<User> users = authService.getAllUsers();
            List<AttendanceRecord> attendance = new FileAttendanceRepository().findAll();
            List<AssessmentRecord> assessments = new FileAssessmentRepository().findAll();
            DashboardAnalytics analytics = DashboardAnalytics.from(users, attendance, assessments);
            showMetrics(analytics);
            showCharts(analytics);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to load dashboard analytics.\n" + e.getMessage(),
                    "Analytics Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showMetrics(DashboardAnalytics analytics) {
        metricsPanel.removeAll();
        metricsPanel.add(createMetric("Total Accounts", Integer.toString(analytics.getTotalUsers()), UITheme.PRIMARY));
        metricsPanel.add(createMetric("Active Accounts", Integer.toString(analytics.getActiveUsers()), UITheme.SUCCESS));
        metricsPanel.add(createMetric("Attendance Records",
                Integer.toString(analytics.getAttendanceRecordCount()), UITheme.INFO));
        String average = analytics.getAssessmentAverageBySubject().isEmpty()
                ? "N/A" : String.format("%.1f%%", analytics.getWeightedAssessmentAverage());
        metricsPanel.add(createMetric("Weighted Marks Average", average, UITheme.ACCENT_PURPLE));
        metricsPanel.revalidate();
        metricsPanel.repaint();
    }

    private JPanel createMetric(String title, String value, Color accent) {
        ModernCard card = new ModernCard(14);
        card.setLayout(new BorderLayout(6, 8));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(UITheme.FONT_TITLE);
        valueLabel.setForeground(accent);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void showCharts(DashboardAnalytics analytics) {
        chartsPanel.removeAll();
        chartsPanel.add(new BarChartPanel("User Accounts by Role",
                toDoubleMap(analytics.getUsersByRole()), "accounts", UITheme.PRIMARY));
        chartsPanel.add(new BarChartPanel("Attendance Records by Subject",
                toDoubleMap(analytics.getAttendanceBySubject()), "records", UITheme.SUCCESS));
        chartsPanel.add(new BarChartPanel("Weighted Assessment Average by Subject",
                analytics.getAssessmentAverageBySubject(), "percent", UITheme.ACCENT_PURPLE));
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }

    private static Map<String, Double> toDoubleMap(Map<String, Integer> source) {
        java.util.Map<String, Double> converted = new java.util.LinkedHashMap<>();
        source.forEach((key, value) -> converted.put(key, value.doubleValue()));
        return converted;
    }

    private static final class BarChartPanel extends JPanel {
        private final String title;
        private final Map<String, Double> values;
        private final String unit;
        private final Color accent;

        private BarChartPanel(String title, Map<String, Double> values, String unit, Color accent) {
            this.title = title;
            this.values = values;
            this.unit = unit;
            this.accent = accent;
            setOpaque(true);
            setBackground(UITheme.CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER),
                    BorderFactory.createEmptyBorder(14, 16, 14, 16)));
            setPreferredSize(new Dimension(900, Math.max(190, 70 + values.size() * 30)));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(UITheme.FONT_SUBHEADER);
            g.setColor(UITheme.TEXT_PRIMARY);
            g.drawString(title, 16, 24);

            if (values.isEmpty()) {
                g.setFont(UITheme.FONT_BODY);
                g.setColor(UITheme.TEXT_MUTED);
                g.drawString("No data recorded yet.", 16, 58);
                g.dispose();
                return;
            }

            int labelWidth = 190;
            int valueWidth = 100;
            int chartLeft = 16 + labelWidth;
            int chartWidth = Math.max(80, getWidth() - chartLeft - valueWidth - 28);
            int rowHeight = 30;
            int top = 48;
            double max = values.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
            if (max <= 0) {
                max = 1;
            }

            FontMetrics metrics = g.getFontMetrics(UITheme.FONT_BODY);
            for (Map.Entry<String, Double> entry : values.entrySet()) {
                int y = top + 18;
                g.setFont(UITheme.FONT_BODY);
                g.setColor(UITheme.TEXT_SECONDARY);
                String label = fitLabel(entry.getKey(), metrics, labelWidth - 12);
                g.drawString(label, 16, y);

                g.setColor(UITheme.BORDER);
                g.fillRoundRect(chartLeft, top + 4, chartWidth, 14, 8, 8);
                int barWidth = (int) Math.round(chartWidth * entry.getValue() / max);
                if (entry.getValue() > 0) {
                    g.setColor(accent);
                    g.fillRoundRect(chartLeft, top + 4, Math.max(4, barWidth), 14, 8, 8);
                }
                g.setColor(UITheme.TEXT_PRIMARY);
                g.drawString(formatValue(entry.getValue()) + ("percent".equals(unit) ? "%" : " " + unit),
                        chartLeft + chartWidth + 10, y);
                top += rowHeight;
            }
            g.dispose();
        }

        private static String fitLabel(String text, FontMetrics metrics, int availableWidth) {
            if (metrics.stringWidth(text) <= availableWidth) {
                return text;
            }
            String shortened = text;
            while (!shortened.isEmpty() && metrics.stringWidth(shortened + "...") > availableWidth) {
                shortened = shortened.substring(0, shortened.length() - 1);
            }
            return shortened + "...";
        }

        private static String formatValue(double value) {
            return value == Math.rint(value) ? Long.toString((long) value) : String.format("%.1f", value);
        }
    }
}
