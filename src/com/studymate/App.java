package com.studymate;

import java.sql.*;

public class App {
    private static final String DB_URL = "jdbc:sqlite:studymate.db";

    public static void main(String[] args) {
        // 1. Create table if it doesn't exist,
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String createTable = "CREATE TABLE IF NOT EXISTS flashcards (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "question TEXT NOT NULL," +
                    "answer TEXT NOT NULL" +
                    ")";
            stmt.execute(createTable);

            System.out.println("✅ Table ready.");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 2. Insert one flashcard
        String insertSql = "INSERT INTO flashcards(question, answer) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            pstmt.setString(1, "What is Java?");
            pstmt.setString(2, "A programming language.");
            pstmt.executeUpdate();

            System.out.println("✅ Flashcard inserted.");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3. Read all flashcards
        String selectSql = "SELECT * FROM flashcards";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {

            System.out.println("📖 Flashcards in database:");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + ". " +
                        rs.getString("question") + " → " +
                        rs.getString("answer"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
