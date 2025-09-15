package ui;

import okhttp3.*;
import com.google.gson.*;

public class GeminiClient {
    private static final String MODEL = "gemini-2.0-flash-001";
    private final OkHttpClient client = new OkHttpClient();
    private final String apiKey;

    public GeminiClient(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("GOOGLE_API_KEY is missing");
        }
        this.apiKey = apiKey;
    }

    public String askGemini(String prompt) {
        try {
            // Build JSON body
            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", prompt);

            JsonObject content = new JsonObject();
            content.add("parts", new JsonArray());
            content.getAsJsonArray("parts").add(textPart);

            JsonObject body = new JsonObject();
            body.add("contents", new JsonArray());
            body.getAsJsonArray("contents").add(content);

            RequestBody requestBody = RequestBody.create(
                    body.toString(), MediaType.parse("application/json"));

            // Build HTTP request
            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/"
                            + MODEL + ":generateContent?key=" + apiKey)
                    .post(requestBody)
                    .build();

            // Send request
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "[Error: " + response.code() + " " + response.message() + "]";
                }

                // Parse response JSON
                assert response.body() != null;
                String resp = response.body().string();
                JsonObject json = JsonParser.parseString(resp).getAsJsonObject();

                return json.getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
            }
        } catch (Exception e) {
            return "[Error: " + e.getMessage() + "]";
        }
    }
}
