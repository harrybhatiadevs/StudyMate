package model;

public class Flashcard {
    private int id;              // database id (auto-incremented)
    private String question;     // the question text
    private String answer;       // the answer text

    // Constructor without id (for new flashcards before saving to DB)
    public Flashcard(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    // For debugging — makes it easy to print a flashcard
    @Override
    public String toString() {
        return id + ". Q: " + question + " | A: " + answer;
    }
}