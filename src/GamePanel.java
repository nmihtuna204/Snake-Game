import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;
import java.util.function.Consumer;

public class GamePanel extends JPanel implements ActionListener {
    public static final int SCREEN_WIDTH = 600;
    public static final int SCREEN_HEIGHT = 600;
    public static final int UNIT_SIZE = 25;

    enum Direction { UP, DOWN, LEFT, RIGHT }

    private final Queue<Point> snakeBody = new LinkedList<>();
    private Point food;
    private Point powerUp;
    private int bodyParts = 3;
    private int foodEaten;
    private Direction direction = Direction.RIGHT;
    private Direction pendingDirection = null; // To handle rapid key presses
    private boolean running = false;
    private Timer timer;
    private final int initialDelay;
    private int currentDelay;
    private final Consumer<Void> restartCallback;
    private final ScoreStack scoreStack;
    private long powerUpTimer;
    private boolean speedBoostActive = false;
    private boolean scoreMultiplierActive = false;
    private Clip backgroundMusicClip = null; // For background music

    public GamePanel(int gameDelay, Consumer<Void> restartCallback) {
        this.initialDelay = gameDelay;
        this.currentDelay = gameDelay;
        this.restartCallback = restartCallback;
        this.scoreStack = new ScoreStack();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(new Color(20, 40, 80));
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }

    public void startGame() {
        snakeBody.clear();
        int initialX = UNIT_SIZE * 5;
        int initialY = SCREEN_HEIGHT / 2;
        for (int i = 0; i < bodyParts; i++) {
            snakeBody.offer(new Point(initialX - (i * UNIT_SIZE), initialY));
        }
        direction = Direction.RIGHT;
        pendingDirection = null;
        newFood();
        running = true;
        timer = new Timer(currentDelay, this);
        timer.start();
        foodEaten = 0;
        powerUp = null;
        powerUpTimer = 0;
        speedBoostActive = false;
        scoreMultiplierActive = false;
        playBackgroundMusic("sounds/music.wav"); // Start background music
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            g.setColor(new Color(30, 60, 100));
            for (int i = 0; i < SCREEN_WIDTH / UNIT_SIZE; i++) {
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
                g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
            }

            if (food != null) {
                g.setColor(Color.RED);
                g.fillOval(food.x, food.y, UNIT_SIZE, UNIT_SIZE);
            }

            if (powerUp != null) {
                g.setColor(Color.YELLOW);
                g.fillRect(powerUp.x, powerUp.y, UNIT_SIZE, UNIT_SIZE);
            }

            Point head = snakeBody.peek();
            if (head != null) {
                for (Point currentPart : snakeBody) {
                    if (currentPart.equals(head)) {
                        g.setColor(speedBoostActive ? Color.BLUE : Color.GREEN);
                        g.fillRect(currentPart.x, currentPart.y, UNIT_SIZE, UNIT_SIZE);
                    } else {
                        g.setColor(new Color(45, 180, 0));
                        g.fillRect(currentPart.x, currentPart.y, UNIT_SIZE, UNIT_SIZE);
                    }
                }
            }

            g.setColor(Color.CYAN);
            g.setFont(new Font("Ink Free", Font.BOLD, 20));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + foodEaten, 10, 30);
            String highScores = "High: " + scoreStack.getTopThree();
            g.drawString(highScores, SCREEN_WIDTH - metrics.stringWidth(highScores) - 10, 30);
        } else {
            gameOver(g);
        }
    }

    public void newFood() {
        boolean foodOnSnake;
        Random random = new Random();
        do {
            foodOnSnake = false;
            int foodX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            int foodY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            food = new Point(foodX, foodY);
            for (Point snakePart : snakeBody) {
                if (snakePart.equals(food)) {
                    foodOnSnake = true;
                    break;
                }
            }
        } while (foodOnSnake);
    }

    public void newPowerUp() {
        Random random = new Random();
        if (random.nextInt(10) < 3) {
            boolean powerUpOnSnake;
            do {
                powerUpOnSnake = false;
                int powerUpX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
                int powerUpY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
                powerUp = new Point(powerUpX, powerUpY);
                for (Point snakePart : snakeBody) {
                    if (snakePart.equals(powerUp)) {
                        powerUpOnSnake = true;
                        break;
                    }
                }
                if (powerUp != null && food != null && powerUp.equals(food)) {
                    powerUpOnSnake = true;
                }
            } while (powerUpOnSnake);
            powerUpTimer = System.currentTimeMillis();
        }
    }

    public void move() {
        if (snakeBody.isEmpty()) return;

        // Apply pending direction if valid
        if (pendingDirection != null && isValidDirectionChange(direction, pendingDirection)) {
            direction = pendingDirection;
            pendingDirection = null;
        }

        Point head = snakeBody.peek();
        if (head == null) return; // Additional safety check

        Point newHead = new Point(head.x, head.y);

        switch (direction) {
            case UP: newHead.y -= UNIT_SIZE; break;
            case DOWN: newHead.y += UNIT_SIZE; break;
            case LEFT: newHead.x -= UNIT_SIZE; break;
            case RIGHT: newHead.x += UNIT_SIZE; break;
        }

        newHead.x = Math.max(0, Math.min(newHead.x, SCREEN_WIDTH - UNIT_SIZE));
        newHead.y = Math.max(0, Math.min(newHead.y, SCREEN_HEIGHT - UNIT_SIZE));

        Queue<Point> newSnakeBody = new LinkedList<>();
        newSnakeBody.offer(newHead);

        Point previous = head;
        boolean first = true;
        for (Point segment : snakeBody) {
            if (first) { first = false; continue; }
            newSnakeBody.offer(new Point(previous.x, previous.y));
            previous = segment;
        }

        // If we've eaten food, add one more segment at the tail
        if (newSnakeBody.size() < bodyParts) {
            newSnakeBody.offer(new Point(previous.x, previous.y));
        }

        snakeBody.clear();
        while (!newSnakeBody.isEmpty()) {
            snakeBody.offer(newSnakeBody.poll());
        }
    }

    private boolean isValidDirectionChange(Direction current, Direction newDir) {
        // Prevent 180-degree turns (e.g., RIGHT to LEFT)
        return !(current == Direction.RIGHT && newDir == Direction.LEFT) &&
                !(current == Direction.LEFT && newDir == Direction.RIGHT) &&
                !(current == Direction.UP && newDir == Direction.DOWN) &&
                !(current == Direction.DOWN && newDir == Direction.UP);
    }

    public void checkFood() {
        if (food != null && !snakeBody.isEmpty()) {
            Point head = snakeBody.peek();
            if (head != null && head.equals(food)) {
                bodyParts++;
                foodEaten += scoreMultiplierActive ? 2 : 1;
                newFood();
                newPowerUp();
                playSound("sounds/food.wav"); // Play food sound
                if (foodEaten % 5 == 0 && currentDelay > 50) {
                    currentDelay = Math.max(50, currentDelay - 10);
                    timer.setDelay(currentDelay);
                }
            }
        }
    }

    public void checkPowerUp() {
        Random random = new Random();
        if (powerUp != null && !snakeBody.isEmpty()) {
            Point head = snakeBody.peek();
            if (head != null && head.equals(powerUp)) {
                playSound("sounds/food.wav"); // Reuse food sound for power-up
                if (random.nextBoolean()) {
                    speedBoostActive = true;
                    timer.setDelay(currentDelay / 2);
                } else {
                    scoreMultiplierActive = true;
                }
                powerUp = null;
                powerUpTimer = System.currentTimeMillis();
            }
        }
        if (powerUp != null && System.currentTimeMillis() - powerUpTimer > 5000) {
            powerUp = null;
        }
        if (speedBoostActive && System.currentTimeMillis() - powerUpTimer > 5000) {
            speedBoostActive = false;
            timer.setDelay(currentDelay);
        }
        if (scoreMultiplierActive && System.currentTimeMillis() - powerUpTimer > 5000) {
            scoreMultiplierActive = false;
        }
    }

    public void checkCollisions() {
        if (snakeBody.isEmpty()) return;

        Point head = snakeBody.peek();
        if (head == null) return; // Safety check

        int count = 1;
        for (Point segment : snakeBody) {
            if (count > 1 && segment.equals(head)) {
                running = false;
                break;
            }
            count++;
        }

        if (head.x < 0 || head.x >= SCREEN_WIDTH || head.y < 0 || head.y >= SCREEN_HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
            scoreStack.push(foodEaten);
            stopBackgroundMusic(); // Stop music on game over
            playSound("sounds/gameover.wav"); // Play game over sound
        }
    }

    public void gameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + foodEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + foodEaten)) / 2, SCREEN_HEIGHT / 3);

        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);

        JButton restartButton = new JButton("Restart");
        restartButton.setFont(new Font("SansSerif", Font.BOLD, 24));
        restartButton.setForeground(Color.WHITE);
        restartButton.setBackground(new Color(0, 102, 204));
        restartButton.setBounds((SCREEN_WIDTH - 200) / 2, SCREEN_HEIGHT / 2 + 50, 200, 50);
        restartButton.addActionListener(e -> restartCallback.accept(null));
        add(restartButton);

        JButton resetButton = new JButton("Reset High Scores");
        resetButton.setFont(new Font("SansSerif", Font.BOLD, 24));
        resetButton.setForeground(Color.WHITE);
        resetButton.setBackground(new Color(0, 102, 204));
        resetButton.setBounds((SCREEN_WIDTH - 250) / 2, SCREEN_HEIGHT / 2 + 110, 250, 50);
        resetButton.addActionListener(e -> scoreStack.resetHighScores());
        add(resetButton);
    }

    private void playSound(String filePath) {
        try {
            File soundFile = new File(filePath);
            if (!soundFile.exists()) {
                System.err.println("Sound file not found: " + soundFile.getAbsolutePath());
                return;
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }

    private void playBackgroundMusic(String filePath) {
        try {
            if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
                backgroundMusicClip.stop();
                backgroundMusicClip.close();
            }
            File musicFile = new File(filePath);
            if (musicFile.exists()) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicFile);
                backgroundMusicClip = AudioSystem.getClip();
                backgroundMusicClip.open(audioInputStream);
                backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the music
                backgroundMusicClip.start();
            } else {
                System.err.println("Background music file not found: " + musicFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Error playing background music: " + e.getMessage());
        }
    }

    private void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
            backgroundMusicClip.close();
            backgroundMusicClip = null;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkFood();
            checkPowerUp();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!running) {
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    removeAll();
                    bodyParts = 3;
                    startGame();
                }
                return;
            }
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (isValidDirectionChange(direction, Direction.LEFT)) pendingDirection = Direction.LEFT;
                    break;
                case KeyEvent.VK_RIGHT:
                    if (isValidDirectionChange(direction, Direction.RIGHT)) pendingDirection = Direction.RIGHT;
                    break;
                case KeyEvent.VK_UP:
                    if (isValidDirectionChange(direction, Direction.UP)) pendingDirection = Direction.UP;
                    break;
                case KeyEvent.VK_DOWN:
                    if (isValidDirectionChange(direction, Direction.DOWN)) pendingDirection = Direction.DOWN;
                    break;
            }
        }
    }
}
