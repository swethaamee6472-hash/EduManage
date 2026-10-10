package com.sset.erp.ui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.sset.erp.util.QRCodeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class LiveWebcamQRScanner extends JFrame {
    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isScanning = true;

    public LiveWebcamQRScanner(Consumer<String> onQrDecoded) {
        setTitle("Live QR Scanner");
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        webcam = Webcam.getDefault();
        if (webcam == null) {
            JOptionPane.showMessageDialog(this, "No webcam detected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        webcam.setViewSize(new Dimension(640, 480));
        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setFPSDisplayed(true);
        webcamPanel.setImageSizeDisplayed(true);
        webcamPanel.setMirrored(true);

        add(webcamPanel, BorderLayout.CENTER);

        // Run background scanner
        executor.submit(() -> {
            while (isScanning && webcam.isOpen()) {
                try {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        String result = QRCodeUtil.readQRCode(image);
                        if (result != null) {
                            isScanning = false; // Stop scanning once found
                            SwingUtilities.invokeLater(() -> {
                                onQrDecoded.accept(result);
                                closeScanner();
                            });
                            break;
                        }
                    }
                    Thread.sleep(200); // 5 scans per second
                } catch (Exception e) {
                    // Ignore exception if decode fails, just keep scanning
                    try {
                        Thread.sleep(200);
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

    private void closeScanner() {
        isScanning = false;
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
        executor.shutdownNow();
        dispose();
    }
}
