import java.io.*;

public class HighScoreManager {
    private static final String HIGH_SCORE_FILE = "highscore.txt";
    private int highScore;

    public HighScoreManager() {
        loadHighScore();
    }

    public int getHighScore() {
        return highScore;
    }

    public void updateHighScore(int score) {
        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }
    }

    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
        } catch (Exception e) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            System.err.println("Error saving high score: " + e.getMessage());
        }
    }
}


