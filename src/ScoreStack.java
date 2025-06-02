import java.util.Stack;
import java.io.*;

public class ScoreStack {
    private static final String HIGH_SCORE_FILE = "highscores.txt";
    private Stack<Integer> scoreStack;

    public ScoreStack() {
        scoreStack = new Stack<>();
        loadHighScores();
    }

    public void push(int score) {
        scoreStack.push(score);
        sortAndTrim();
        saveHighScores();
    }

    public String getTopThree() {
        if (scoreStack.isEmpty()) {
            return "0";
        }

        Integer[] scores = scoreStack.toArray(new Integer[0]);
        java.util.Arrays.sort(scores, (a, b) -> b - a); // Sort in descending order
        int count = Math.min(3, scores.length);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(", ");
            sb.append(scores[i]);
        }
        return sb.length() > 0 ? sb.toString() : "0";
    }

    public void resetHighScores() {
        try {
            File file = new File(HIGH_SCORE_FILE);
            if (file.exists()) {
                file.delete();
            }
            scoreStack.clear();
            saveHighScores();
        } catch (Exception e) {
            System.err.println("Error resetting high scores: " + e.getMessage());
        }
    }

    private void sortAndTrim() {
        Integer[] scores = scoreStack.toArray(new Integer[0]);
        java.util.Arrays.sort(scores, (a, b) -> b - a); // Sort in descending order
        scoreStack.clear();
        for (int i = 0; i < Math.min(3, scores.length); i++) {
            scoreStack.push(scores[i]);
        }
    }

    private void loadHighScores() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    int score = Integer.parseInt(line.trim());
                    scoreStack.push(score);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
            sortAndTrim();
        } catch (IOException e) {
            scoreStack.clear(); // Start fresh if file doesn't exist
        }
    }

    private void saveHighScores() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            for (Integer score : scoreStack) {
                writer.write(score + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving high scores: " + e.getMessage());
        }
    }
}
