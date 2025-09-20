package model;

public class Flashcard {
    private final String topic;
    private final String question;
    private final String answer;

    public Flashcard(String topic, String question, String answer) {
        this.topic = topic;
        this.question = question;
        this.answer = answer;
    }

    public String getTopic()    { return topic; }
    public String getQuestion() { return question; }
    public String getAnswer()   { return answer; }
}
