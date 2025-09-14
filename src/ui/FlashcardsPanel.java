package ui;

import model.Flashcard;
import model.FlashcardDAO;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FlashcardsPanel extends JPanel {
    private final FlashcardDAO dao = new FlashcardDAO();
    private List<Flashcard> cards;
    private int idx = 0;

    // UI components
    private final JLabel counter = new JLabel("0/0");
    private final JTextArea questionArea = new JTextArea();
    private final JTextField yourAnswer = new JTextField();
    private final JButton checkBtn = new JButton("Check");
    private final JButton nextBtn = new JButton("Next ▶");
    private final JButton prevBtn = new JButton("◀ Prev");
    private final JButton addBtn  = new JButton("Add");
    private final JLabel verdict = new JLabel(" ");        // shows ✅ Correct! / ❌ Not quite.
    private final JTextArea revealArea = new JTextArea();  // holds the real answer (hidden until Check)

    public FlashcardsPanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // ==== Top bar (title, counter, nav, add) ====
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        JLabel title = new JLabel("Flashcards");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        top.add(title);
        top.add(counter);
        top.add(prevBtn);
        top.add(nextBtn);
        top.add(addBtn);
        add(top, BorderLayout.NORTH);

        // ==== Center: question only ====
        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setFont(questionArea.getFont().deriveFont(16f));
        add(new JScrollPane(questionArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
            BorderLayout.CENTER);

        // ==== Bottom: answer entry + check + verdict + (hidden) reveal ====
        JPanel bottom = new JPanel(new BorderLayout(8,8));

        JPanel inputRow = new JPanel(new BorderLayout(8,8));
        inputRow.add(new JLabel("Your answer:"), BorderLayout.WEST);
        inputRow.add(yourAnswer, BorderLayout.CENTER);
        inputRow.add(checkBtn, BorderLayout.EAST);
        bottom.add(inputRow, BorderLayout.NORTH);

        verdict.setForeground(new Color(30,130,30));
        verdict.setFont(verdict.getFont().deriveFont(Font.BOLD, 13f));
        bottom.add(verdict, BorderLayout.CENTER);

        revealArea.setEditable(false);
        revealArea.setLineWrap(true);
        revealArea.setWrapStyleWord(true);
        revealArea.setVisible(false); // hidden until Check is pressed
        revealArea.setBackground(getBackground());
        revealArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(6,0,0,0),
                BorderFactory.createMatteBorder(1,0,0,0, new Color(220,220,220))
        ));
        bottom.add(revealArea, BorderLayout.SOUTH);

        add(bottom, BorderLayout.SOUTH);

        // ==== Actions ====
        prevBtn.addActionListener(e -> { if (idx > 0) { idx--; showCard(); } });
        nextBtn.addActionListener(e -> { if (cards != null && idx < cards.size()-1) { idx++; showCard(); } });
        checkBtn.addActionListener(e -> checkAnswer());
        addBtn.addActionListener(e -> addCardDialog());

        // Enter key submits answer
        yourAnswer.addActionListener(e -> checkAnswer());

        // Load data
        refresh();
    }

    /** Reloads cards from DB. Call this before showing the panel. */
    public void refresh() {
        cards = dao.getAllFlashcards();
        if (cards == null || cards.isEmpty()) {
            questionArea.setText("No flashcards yet.\nClick 'Add' to create one.");
            yourAnswer.setEnabled(false);
            checkBtn.setEnabled(false);
            prevBtn.setEnabled(false);
            nextBtn.setEnabled(false);
            counter.setText("0/0");
            verdict.setText(" ");
            revealArea.setVisible(false);
            revealArea.setText("");
            return;
        }
        // Enable controls and show the current card
        yourAnswer.setEnabled(true);
        checkBtn.setEnabled(true);
        prevBtn.setEnabled(true);
        nextBtn.setEnabled(true);
        idx = Math.max(0, Math.min(idx, cards.size()-1));
        showCard();
    }

    /** Shows the current question; hides the reveal until checked */
    private void showCard() {
        Flashcard c = cards.get(idx);
        questionArea.setText("Q: " + c.getQuestion());
        counter.setText((idx+1) + "/" + cards.size());

        // reset answer UI
        verdict.setText(" ");
        verdict.setForeground(new Color(30,130,30));
        revealArea.setVisible(false);
        revealArea.setText("");
        yourAnswer.setText("");
        yourAnswer.requestFocusInWindow();

        prevBtn.setEnabled(idx > 0);
        nextBtn.setEnabled(idx < cards.size()-1);
    }

    /** Compares user's answer (case/space-insensitive), shows verdict & reveals real answer */
    private void checkAnswer() {
        if (cards == null || cards.isEmpty()) return;
        Flashcard c = cards.get(idx);

        String typed = normalize(yourAnswer.getText());
        String correct = normalize(c.getAnswer());

        boolean ok = !typed.isEmpty() && typed.equalsIgnoreCase(correct);
        verdict.setText(ok ? "✅ Correct!" : "❌ Not quite.");
        verdict.setForeground(ok ? new Color(30,130,30) : new Color(170,30,30));

        // reveal truth
        revealArea.setText("Answer: " + c.getAnswer());
        revealArea.setVisible(true);
    }

    /** Quick add dialog (handy while you’re testing) */
    private void addCardDialog() {
        String q = JOptionPane.showInputDialog(this, "Question:", "Add Flashcard", JOptionPane.QUESTION_MESSAGE);
        if (q == null || q.isBlank()) return;
        String a = JOptionPane.showInputDialog(this, "Answer:", "Add Flashcard", JOptionPane.QUESTION_MESSAGE);
        if (a == null || a.isBlank()) return;

        dao.addFlashcard(new Flashcard(q.trim(), a.trim()));
        // Move to the last card so the user sees what they just added
        idx = (cards == null ? 0 : cards.size());
        refresh();
    }

    // Simple normalization to be forgiving about spaces/case
    private static String normalize(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }
}