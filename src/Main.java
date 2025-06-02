import javax.swing.*;
import java.awt.*;

public class Main implements SpeedSelectionListener {
    private JFrame frame;
    private SpeedSelectionPanel speedSelectionPanel;
    private GamePanel gamePanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().createAndShowGUI());
    }

    public void createAndShowGUI() {
        frame = new JFrame("Snake Game - Java DSA");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        showSpeedSelectionScreen();
        frame.setLocationRelativeTo(null);
    }

    public void showSpeedSelectionScreen() {
        if (gamePanel != null) {
            frame.remove(gamePanel);
            gamePanel = null;
        }
        speedSelectionPanel = new SpeedSelectionPanel(this);
        frame.add(speedSelectionPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        speedSelectionPanel.requestFocusInWindow();
    }

    @Override
    public void onSpeedSelected(int delay) {
        if (speedSelectionPanel != null) {
            frame.remove(speedSelectionPanel);
            speedSelectionPanel = null;
        }
        gamePanel = new GamePanel(delay, v -> showSpeedSelectionScreen()); // Lambda expression
        frame.add(gamePanel, BorderLayout.CENTER);
        frame.pack();
        frame.revalidate();
        frame.repaint();
        gamePanel.requestFocusInWindow();
    }
}