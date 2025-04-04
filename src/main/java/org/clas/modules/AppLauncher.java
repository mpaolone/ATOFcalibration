package org.clas.modules;

import javax.swing.*;

public class AppLauncher {
    public static void main(String[] args) {
        // Launch the main GUI on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            ALERTCalibrationEngine.runGUI();
        });
    }
}
