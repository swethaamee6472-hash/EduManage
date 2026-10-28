package com.sset.erp.ui;

import com.github.sarxos.webcam.Webcam;
import com.sset.erp.util.QRCodeUtil;
import com.sset.erp.util.UITheme;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * High-performance QR Code scanner supporting native Apple Silicon (AVFoundation)
 * on macOS as well as standard webcam capture on other platforms.
 */
public class LiveWebcamQRScanner extends JFrame {
    private Webcam sarxosWebcam;
    private Process nativeMacProcess;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean isScanning = true;
    private volatile BufferedImage currentFrame = null;
    private final Consumer<String> onQrDecoded;

    private JPanel cameraPanel;
    private JPanel viewFinder;
    private JLayeredPane layeredPane;
    private JPanel bottomBar;

    public LiveWebcamQRScanner(Consumer<String> onQrDecoded) {
        this.onQrDecoded = onQrDecoded;
        setTitle("Live QR Scanner - EduManage");
        setSize(680, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        boolean started = false;
        if (isMacOS()) {
            started = startNativeMacCapture();
        }

        if (!started) {
            started = startSarxosCapture();
        }

        if (!started) {
            showFallbackUI();
            return;
        }

        buildLiveUI();
    }

    private static boolean isMacOS() {
        return System.getProperty("os.name", "").toLowerCase().contains("mac");
    }

    private boolean startNativeMacCapture() {
        File bin = new File("bin/mac_camera_stream");
        File swiftSrc = new File("src/native/mac_camera_stream.swift");

        ProcessBuilder pb = null;
        if (bin.exists() && bin.canExecute()) {
            pb = new ProcessBuilder(bin.getAbsolutePath());
        } else if (swiftSrc.exists()) {
            pb = new ProcessBuilder("swift", swiftSrc.getAbsolutePath());
        }

        if (pb == null) {
            return false;
        }

        try {
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            nativeMacProcess = pb.start();

            executor.submit(() -> {
                try (DataInputStream dis = new DataInputStream(new BufferedInputStream(nativeMacProcess.getInputStream()))) {
                    while (isScanning && nativeMacProcess.isAlive()) {
                        int length;
                        try {
                            length = dis.readInt();
                        } catch (Exception e) {
                            break;
                        }
                        if (length <= 0 || length > 15_000_000) {
                            break;
                        }
                        byte[] data = new byte[length];
                        dis.readFully(data);
                        BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
                        if (img != null) {
                            currentFrame = img;
                            if (cameraPanel != null) {
                                cameraPanel.repaint();
                            }
                            String decoded = QRCodeUtil.readQRCode(img);
                            if (decoded != null && !decoded.trim().isEmpty()) {
                                isScanning = false;
                                SwingUtilities.invokeLater(() -> {
                                    closeScanner();
                                    onQrDecoded.accept(decoded.trim());
                                });
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Stream closed
                }
            });
            return true;
        } catch (Exception e) {
            if (nativeMacProcess != null) {
                nativeMacProcess.destroyForcibly();
                nativeMacProcess = null;
            }
            return false;
        }
    }

    private boolean startSarxosCapture() {
        try {
            sarxosWebcam = Webcam.getDefault();
            if (sarxosWebcam == null) {
                return false;
            }
            sarxosWebcam.open();
            executor.submit(() -> {
                while (isScanning && sarxosWebcam.isOpen()) {
                    try {
                        BufferedImage img = sarxosWebcam.getImage();
                        if (img != null) {
                            currentFrame = img;
                            if (cameraPanel != null) {
                                cameraPanel.repaint();
                            }
                            String decoded = QRCodeUtil.readQRCode(img);
                            if (decoded != null && !decoded.trim().isEmpty()) {
                                isScanning = false;
                                SwingUtilities.invokeLater(() -> {
                                    closeScanner();
                                    onQrDecoded.accept(decoded.trim());
                                });
                                break;
                            }
                        }
                        Thread.sleep(60);
                    } catch (Exception ignored) {
                    }
                }
            });
            return true;
        } catch (Throwable t) {
            sarxosWebcam = null;
            return false;
        }
    }

    private void buildLiveUI() {
        cameraPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();
                if (currentFrame != null) {
                    int imgW = currentFrame.getWidth();
                    int imgH = currentFrame.getHeight();
                    double scale = Math.max((double) w / imgW, (double) h / imgH);
                    int drawW = (int) (imgW * scale);
                    int drawH = (int) (imgH * scale);
                    int x = (w - drawW) / 2;
                    int y = (h - drawH) / 2;
                    g2.drawImage(currentFrame, x, y, drawW, drawH, null);
                } else {
                    g2.setColor(new Color(15, 23, 42));
                    g2.fillRect(0, 0, w, h);
                    g2.setColor(Color.WHITE);
                    g2.setFont(UITheme.FONT_BODY_BOLD);
                    String msg = "Initializing camera feed...";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
                }
                g2.dispose();
            }
        };

        viewFinder = new JPanel() {
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
                g2.setColor(new Color(0, 0, 0, 70));
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
                g2.drawLine(x + boxSize, y + boxSize, x + boxSize, y + cornerLen);

                // Hint text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                String hint = "Align QR code inside frame (Hold camera steady)";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, (w - fm.stringWidth(hint)) / 2, y + boxSize + 24);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g2.setColor(new Color(203, 213, 225));
                String hint2 = "Tip: Make sure the QR code is clearly visible and well-lit";
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString(hint2, (w - fm2.stringWidth(hint2)) / 2, y + boxSize + 42);

                g2.dispose();
            }
        };
        viewFinder.setOpaque(false);

        layeredPane = new JLayeredPane();
        cameraPanel.setBounds(0, 0, 680, 460);
        viewFinder.setBounds(0, 0, 680, 460);
        layeredPane.add(cameraPanel, Integer.valueOf(1));
        layeredPane.add(viewFinder, Integer.valueOf(2));

        bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        bottomBar.setBackground(UITheme.CARD_BG);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton btnUploadFile = new JButton("📁 Upload QR Image");
        btnUploadFile.setFont(UITheme.FONT_BODY_BOLD);
        btnUploadFile.setFocusPainted(false);
        btnUploadFile.addActionListener(e -> chooseQRImageFromFile());

        JButton btnManual = new JButton("⌨️ Enter Roll Number");
        btnManual.setFont(UITheme.FONT_BODY_BOLD);
        btnManual.setFocusPainted(false);
        btnManual.addActionListener(e -> {
            String roll = JOptionPane.showInputDialog(this, "Enter Student Roll Number:", "Manual Input", JOptionPane.QUESTION_MESSAGE);
            if (roll != null && !roll.trim().isEmpty()) {
                isScanning = false;
                closeScanner();
                onQrDecoded.accept("STUDENT_ID:" + roll.trim());
            }
        });

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(UITheme.FONT_BODY);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> closeScanner());

        bottomBar.add(btnUploadFile);
        bottomBar.add(btnManual);
        bottomBar.add(btnCancel);

        add(layeredPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int w = getWidth();
                int h = getHeight() - bottomBar.getHeight() - 40;
                if (cameraPanel != null) cameraPanel.setBounds(0, 0, w, h);
                if (viewFinder != null) viewFinder.setBounds(0, 0, w, h);
                layeredPane.revalidate();
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
        JPanel p = new JPanel(new BorderLayout(16, 20));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(new javax.swing.border.EmptyBorder(40, 40, 40, 40));

        JLabel msg = new JLabel("<html><center><h2 style='color:#1a73e8;'>Camera Unavailable</h2>"
                + "<p style='color:#475569;'>Could not initialize live camera feed.<br>"
                + "You can upload a saved QR code image or enter the Student Roll Number directly.</p></center></html>", SwingConstants.CENTER);
        msg.setFont(UITheme.FONT_BODY);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        btnPanel.setOpaque(false);

        JButton btnFile = new JButton("📁 Browse QR Code File");
        btnFile.setFont(UITheme.FONT_BODY_BOLD);
        btnFile.setPreferredSize(new Dimension(210, 40));
        btnFile.addActionListener(e -> chooseQRImageFromFile());

        JButton btnManual = new JButton("⌨️ Enter Roll Number");
        btnManual.setFont(UITheme.FONT_BODY_BOLD);
        btnManual.setPreferredSize(new Dimension(210, 40));
        btnManual.addActionListener(e -> {
            String roll = JOptionPane.showInputDialog(this, "Enter Student Roll Number (e.g. SSET24CS042):", "Manual Verification", JOptionPane.QUESTION_MESSAGE);
            if (roll != null && !roll.trim().isEmpty()) {
                isScanning = false;
                closeScanner();
                onQrDecoded.accept("STUDENT_ID:" + roll.trim());
            }
        });

        btnPanel.add(btnFile);
        btnPanel.add(btnManual);

        p.add(msg, BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
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
        if (nativeMacProcess != null) {
            try {
                nativeMacProcess.destroyForcibly();
            } catch (Exception ignored) {}
            nativeMacProcess = null;
        }
        if (sarxosWebcam != null) {
            try {
                sarxosWebcam.close();
            } catch (Exception ignored) {}
            sarxosWebcam = null;
        }
        executor.shutdownNow();
        dispose();
    }
}
