import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SpeedSelectionPanel extends JPanel {
    private SpeedSelectionListener listener;
    private JLabel welcomeLabel;
    private JLabel instructionLabel;
    private Timer colorTimer;

    public SpeedSelectionPanel(SpeedSelectionListener listener) {
        this.listener = listener;
        initComponents();
        startTitleAnimation();
    }

    private void initComponents() {
        setBackground(new Color(20, 40, 80));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 0, 10, 0);

        welcomeLabel = new JLabel("WELCOME TO SNAKE GAME");
        welcomeLabel.setFont(new Font("Ink Free", Font.BOLD, 40));
        welcomeLabel.setForeground(Color.CYAN);
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        add(welcomeLabel, gbc);

        gbc.insets = new Insets(10, 0, 30, 0);
        instructionLabel = new JLabel("Choose speed of the snake");
        instructionLabel.setFont(new Font("Ink Free", Font.PLAIN, 30));
        instructionLabel.setForeground(Color.CYAN);
        instructionLabel.setHorizontalAlignment(JLabel.CENTER);
        add(instructionLabel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(createStyledButton("Easy", 150));
        buttonPanel.add(createStyledButton("Normal", 100));
        buttonPanel.add(createStyledButton("Hard", 65));
        gbc.insets = new Insets(20, 0, 20, 0);
        add(buttonPanel, gbc);
    }

    private JButton createStyledButton(String text, int delay) {
        JButton button = new JButton(text);
        button.setFont(new Font("Ink Free", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 102, 204));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 60));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(0, 153, 255));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0, 102, 204));
            }
        });

        button.addActionListener(e -> listener.onSpeedSelected(delay));
        return button;
    }

    private void startTitleAnimation() {
        colorTimer = new Timer(1000, e -> {
            welcomeLabel.setForeground(welcomeLabel.getForeground().equals(Color.CYAN) ? new Color(0, 255, 255) : Color.CYAN);
        });
        colorTimer.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (colorTimer != null) {
            colorTimer.stop();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
    }
}
