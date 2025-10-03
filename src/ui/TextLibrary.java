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
            // Existing Bible passage
            "\"We are not stoning you for any good work,\" they replied, " +
                    "\"but for blasphemy, because you, a mere man, claim to be God.\" " +
                    "Jesus answered them, \"Is it not written in your Law, 'I have said you are gods'? " +
                    "If he called them 'gods,' to whom the word of God came--and Scripture cannot be set aside-- " +
                    "what about the one whom the Father set apart as his very own and sent into the world? " +
                    "Why then do you accuse me of blasphemy because I said, 'I am God's Son'? " +
                    "Do not believe me unless I do the works of my Father. But if I do them, " +
                    "even though you do not believe me, believe the works, that you may know and understand " +
                    "that the Father is in me, and I in the Father.\"," ,

            // New text addition
            "Life is a storm, my young friend. You will bask in the sunlight one moment, " +
                    "be shattered on the rocks the next. What makes you a man is what you do when that storm comes.",

            // Existing sample texts
            "The quick brown fox jumps over the lazy dog. " +
                    "This sentence contains every letter of the English alphabet and is often used to test typing skills and fonts.",

            "In a hole in the ground there lived a hobbit. " +
                    "Not a nasty, dirty, wet hole, filled with the ends of worms and an oozy smell, " +
                    "nor yet a dry, bare, sandy hole with nothing in it to sit down on or to eat: it was a hobbit-hole, and that means comfort.",

            "It was the best of times, it was the worst of times, " +
                    "it was the age of wisdom, it was the age of foolishness, " +
                    "it was the epoch of belief, it was the epoch of incredulity, " +
                    "it was the season of Light, it was the season of Darkness, " +
                    "it was the spring of hope, it was the winter of despair.",

            "To be, or not to be, that is the question: " +
                    "Whether 'tis nobler in the mind to suffer the slings and arrows of outrageous fortune, " +
                    "or to take arms against a sea of troubles, and by opposing end them."
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
