package ui;

public class Profile {
    private final String username;
    private int totalSessions;
    private double totalWpm;
    private double totalAccuracy;

    public Profile(String username) {
        this.username = username;
    }

    // Updated method name to match TypingExercisePanel call
    public void recordResult(int wpm, double accuracy) {
        totalSessions++;
        totalWpm += wpm;
        totalAccuracy += accuracy;
    }

    // Optional: keep the old method for doubles if you want
    public void recordSession(double wpm, double accuracy) {
        totalSessions++;
        totalWpm += wpm;
        totalAccuracy += accuracy;
    }

    public double getAverageWpm() {
        return totalSessions == 0 ? 0 : totalWpm / totalSessions;
    }

    public double getAverageAccuracy() {
        return totalSessions == 0 ? 0 : totalAccuracy / totalSessions;
    }

    public String getUsername() {
        return username;
    }
}