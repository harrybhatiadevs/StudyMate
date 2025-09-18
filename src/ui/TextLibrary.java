package ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * TextLibrary provides:
 * 1) A small built-in sample text pool (legacy API).
 * 2) A simple persistent file library under ~/.studymate/library.
 *
 * The new instance APIs:
 *  - addFile(File): copies a selected file into the library directory and returns the saved Path.
 *  - listFiles(): lists all files currently stored in the library directory.
 *  - readText(Path): reads text content from simple text-like files (txt/md) for preview.
 */
public class TextLibrary {

    // ===== Legacy sample texts (kept for backward compatibility) =====
    private static final String[] TEXTS = {
            "The quick brown fox jumps over the lazy dog.",
            "A journey of a thousand miles begins with a single step.",
            "Practice makes perfect. Keep pushing forward, even when it's tough.",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            "It was the best of times, it was the worst of times, it was the age of wisdom, it was the age of foolishness...",
            "Life is a storm, my young friend. You will bask in the sunlight one moment, be shattered on the rocks the next. What makes you a man is what you do when that storm comes. You must look into that storm and shout as you did in Rome. Do your worst, for I will do mine! Then the fates will know you as we know you."
    };

    public static String getRandomText() {
        int index = (int) (Math.random() * TEXTS.length);
        return TEXTS[index];
    }

    public static String[] addText(String newText) {
        String[] newArray = new String[TEXTS.length + 1];
        System.arraycopy(TEXTS, 0, newArray, 0, TEXTS.length);
        newArray[TEXTS.length] = newText;
        return newArray;
    }

    // ===== New persistent file library =====
    private final Path baseDir;
    private final List<Path> storedFiles = new ArrayList<>();

    public TextLibrary() {
        // Default storage: ~/.studymate/library
        this.baseDir = Paths.get(System.getProperty("user.home"), ".studymate", "library");
        try {
            Files.createDirectories(baseDir);
            // Load existing files into memory so listFiles() returns them immediately.
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir)) {
                for (Path p : stream) {
                    if (Files.isRegularFile(p)) storedFiles.add(p);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize TextLibrary storage: " + e.getMessage(), e);
        }
    }

    /**
     * Copy the given file into the library directory with a unique name.
     * @param source the file chosen by the user
     * @return the saved path inside the library directory
     * @throws IOException if copy fails
     */
    public Path addFile(File source) throws IOException {
        if (source == null || !source.exists()) {
            throw new IllegalArgumentException("Source file does not exist");
        }
        String uniqueName = System.currentTimeMillis() + "_" + sanitize(source.getName());
        Path target = baseDir.resolve(uniqueName);
        Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        storedFiles.add(target);
        return target;
    }

    /**
     * @return an unmodifiable view of all files currently stored in the library.
     */
    public List<Path> listFiles() {
        return Collections.unmodifiableList(storedFiles);
    }

    /**
     * Read text content for lightweight preview. Supports txt/md.
     * @param file a path returned by addFile/listFiles
     * @return file content as String
     * @throws IOException if not supported or reading fails
     */
    public String readText(Path file) throws IOException {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".txt") || name.endsWith(".md")) {
            return Files.readString(file);
        }
        throw new IOException("Unsupported text format for preview: " + name);
    }

    private static String sanitize(String name) {
        // Replace illegal filename characters on Windows/macOS/Linux
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
