package com.sset.erp;

import com.sset.erp.service.AuthService;
import com.sset.erp.service.impl.AuthServiceImpl;
import com.sset.erp.ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application Entry Point for SCMS College Student Data Management ERP.
 * PBCST304 Object Oriented Programming - S3 BTech CSE.
 */
public class Main {
    public static void main(String[] args) {
        // Configure macOS / system rendering properties
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "EduManage ERP");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            // Apply native operating system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default Swing Look & Feel if system L&F unavailable
        }

        // Initialize Services and Launch Login Frame on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            AuthService authService = new AuthServiceImpl();
            LoginFrame loginFrame = new LoginFrame(authService);
            loginFrame.setVisible(true);
        });
    }
}
