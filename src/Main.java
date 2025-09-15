import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.ui.StudyMateFrame;

public class Main {
    public static void main(String[] args) {
        // Use system look and feel for a cleaner appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> {
            StudyMateFrame frame = new StudyMateFrame();
            frame.setVisible(true);
        });
    }
}
