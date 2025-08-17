package client.UI;

import client.Client;

import javax.swing.*;

public class Launcher {
    public static void UILauncher() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set Nimbus Look and Feel
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Nimbus is not available, falling back to default");

            }

            Client.mainWindow = new MainWindow();
        });
    }
}
