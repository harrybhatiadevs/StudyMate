package ui;

public class TextLibrary {

    // Add sentences or paragraphs here. Multi-line texts can use Java text blocks (Java 15+)
    private static final String[] TEXTS = {
            "The quick brown fox jumps over the lazy dog.",
            "A journey of a thousand miles begins with a single step.",
            "Practice makes perfect. Keep pushing forward, even when it’s tough.",
            """
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
        Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
        """,
            """
        It was the best of times, it was the worst of times, 
        it was the age of wisdom, it was the age of foolishness...
        """
    };

    /**
     * Returns a randomly selected text from the library.
     */
    public static String getRandomText() {
        int index = (int) (Math.random() * TEXTS.length);
        return TEXTS[index];
    }

    /**
     * Optional: add a new text dynamically (if needed at runtime)
     */
    public static String[] addText(String newText) {
        String[] newArray = new String[TEXTS.length + 1];
        System.arraycopy(TEXTS, 0, newArray, 0, TEXTS.length);
        newArray[TEXTS.length] = newText;
        return newArray;
    }
}