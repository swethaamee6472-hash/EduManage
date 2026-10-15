package com.sset.erp.ui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.sset.erp.util.QRCodeUtil;
import com.sset.erp.util.UITheme;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.swing.*;

public class LiveWebcamQRScanner extends JFrame {
    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean isScanning = true;
    private final Consumer<String> onQrDecoded;

    public LiveWebcamQRScanner(Consumer<String> onQrDecoded) {
        this.onQrDecoded = onQrDecoded;
        setTitle("Live QR Scanner - EduManage");
        setSize(680, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        webcam = Webcam.getDefault();
        if (webcam == null) {
            JOptionPane.showMessageDialog(this, "No webcam detected on this system!\nYou can still upload a QR image directly.", "Webcam Unavailable", JOptionPane.WARNING_MESSAGE);
            showFallbackUI();
            return;
        }

        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setFPSDisplayed(false);
        webcamPanel.setImageSizeDisplayed(false);
        webcamPanel.setMirrored(false); // Do not mirror: mirroring flips the QR code horizontally which fails standard decoding!

        // Overlay panel for visual guidance (crosshair / target rectangle)
        JPanel viewFinder = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.applyQualityRendering(g2);
                int w = getWidth();
                int h = getHeight();
                int boxSize = Math.min(w, h) * 3 / 5;
                int x = (w - boxSize) / 2;
                int y = (h - boxSize) / 2;

                // Translucent dark vignette around target
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRect(0, 0, w, y);
                g2.fillRect(0, y + boxSize, w, h - (y + boxSize));
                g2.fillRect(0, y, x, boxSize);
                g2.fillRect(x + boxSize, y, w - (x + boxSize), boxSize);

                // Bright cyan target box corners
                g2.setColor(new Color(14, 165, 233));
                g2.setStroke(new BasicStroke(3.0f));
                int cornerLen = 28;
                // Top-Left
                g2.drawLine(x, y, x + cornerLen, y);
                g2.drawLine(x, y, x, y + cornerLen);
                // Top-Right
                g2.drawLine(x + boxSize, y, x + boxSize - cornerLen, y);
                g2.drawLine(x + boxSize, y, x + boxSize, y + cornerLen);
                // Bottom-Left
                g2.drawLine(x, y + boxSize, x + cornerLen, y + boxSize);
                g2.drawLine(x, y + boxSize, x, y + boxSize - cornerLen);
                // Bottom-Right
                g2.drawLine(x + boxSize, y + boxSize, x + boxSize - cornerLen, y + boxSize);
                g2.drawLine(x + boxSize, y + boxSize, x + boxSize, y + boxSize - cornerLen);

                // Hint text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                String hint = "Align QR code inside frame (Hold phone ~15-20cm away)";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, (w - fm.stringWidth(hint)) / 2, y + boxSize + 22);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.setColor(new Color(203, 213, 225));
                String hint2 = "Tip: Lower screen brightness slightly if glare prevents focus";
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString(hint2, (w - fm2.stringWidth(hint2)) / 2, y + boxSize + 40);

                g2.dispose();
            }
        };
        viewFinder.setOpaque(false);

        JLayeredPane layeredPane = new JLayeredPane();
        webcamPanel.setBounds(0, 0, 680, 460);
        viewFinder.setBounds(0, 0, 680, 460);
        layeredPane.add(webcamPanel, Integer.valueOf(1));
        layeredPane.add(viewFinder, Integer.valueOf(2));

        // Bottom control toolbar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        bottomBar.setBackground(UITheme.CARD_BG);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton btnUploadFile = new JButton("📁 Upload / Choose QR Image");
        btnUploadFile.setFont(UITheme.FONT_BODY_BOLD);
        btnUploadFile.setFocusPainted(false);
        btnUploadFile.setBackground(new Color(241, 245, 249));
        btnUploadFile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUploadFile.addActionListener(e -> chooseQRImageFromFile());

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(UITheme.FONT_BODY);
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> closeScanner());

        bottomBar.add(btnUploadFile);
        bottomBar.add(btnCancel);

        add(layeredPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        // Resize overlay when window changes
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int w = getWidth();
                int h = getHeight() - bottomBar.getHeight() - 40;
                webcamPanel.setBounds(0, 0, w, h);
                viewFinder.setBounds(0, 0, w, h);
                layeredPane.revalidate();
            }
        });

        // Start scanner loop
        executor.submit(() -> {
            // Wait for webcam to be open
            int attempts = 0;
            while (!webcam.isOpen() && attempts < 20) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
                attempts++;
            }

            while (isScanning && (webcam.isOpen() || (webcamPanel != null && webcamPanel.isStarting()))) {
                try {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        String result = QRCodeUtil.readQRCode(image);
                        if (result != null && !result.trim().isEmpty()) {
                            isScanning = false;
                            SwingUtilities.invokeLater(() -> {
                                onQrDecoded.accept(result.trim());
                                closeScanner();
                            });
                            break;
                        }
                    }
                    Thread.sleep(100);
                } catch (Exception e) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeScanner();
            }
        });
    }

    private void showFallbackUI() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(new javax.swing.border.EmptyBorder(40, 40, 40, 40));

        JLabel msg = new JLabel("<html><center><h3>No Webcam Available</h3><p>You can browse and select any saved student/faculty QR Code image to scan directly.</p></center></html>", SwingConstants.CENTER);
        msg.setFont(UITheme.FONT_BODY);

        JButton btn = new JButton("📁 Select QR Code File");
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.addActionListener(e -> chooseQRImageFromFile());

        p.add(msg, BorderLayout.CENTER);
        p.add(btn, BorderLayout.SOUTH);
        add(p, BorderLayout.CENTER);
    }

    private void chooseQRImageFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select QR Code Image to Scan");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                String decoded = QRCodeUtil.readQRCode(f);
                if (decoded != null && !decoded.trim().isEmpty()) {
                    isScanning = false;
                    closeScanner();
                    onQrDecoded.accept(decoded.trim());
                } else {
                    JOptionPane.showMessageDialog(this, "Could not detect a valid QR code in the selected file.", "Decode Failed", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to read image file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void closeScanner() {
        isScanning = false;
        try {
            if (webcamPanel != null) {
                webcamPanel.stop();
            }
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
            }
        } catch (Exception ignored) {}
        executor.shutdownNow();
        dispose();
    }
}
