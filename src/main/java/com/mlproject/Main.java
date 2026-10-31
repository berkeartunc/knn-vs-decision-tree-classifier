package com.mlproject;

import com.mlproject.gui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        // Start the UI on the Swing event thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Use the native look and feel of the operating system
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {

                // If it fails, keep the default look and feel instead of crashing
                System.err.println("Could not load the system look and feel: " + e.getMessage());
            }
            // Show the main window
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
