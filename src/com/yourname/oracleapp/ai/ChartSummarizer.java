package com.yourname.oracleapp.ai;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import org.json.JSONObject;
import org.json.JSONArray;

public class ChartSummarizer {

    private static final String API_KEY = "AIzaSyBbiWsavDfWOUJMFZaDq9-H-nhMaYxA9Cw";
    private static final String MODEL = "gemini-2.5-flash";
    private static final String API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";

    public String summarizeChart(BufferedImage chartImage, String chartType, String xAxis, String yAxis) throws Exception {

        if (API_KEY == null || API_KEY.isEmpty()) {
            return "Error: Gemini API key is missing.";
        }

        if (chartImage == null) {
            throw new IllegalArgumentException("Chart image cannot be null");
        }
        if (chartType == null) {
            chartType = "Unknown";
        }
        if (xAxis == null) {
            xAxis = "Unknown";
        }
        if (yAxis == null) {
            yAxis = "Unknown";
        }

        String base64Image = imageToBase64(chartImage);
        String prompt = createPrompt(chartType, xAxis, yAxis);

        return callGeminiAPI(base64Image, prompt);
    }

    private String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private String createPrompt(String chartType, String xAxis, String yAxis) {
        return "Analyze this " + chartType + " chart and summarize the key insights in 3-5 sentences.\n"
                + "X-axis: " + xAxis + "\n"
                + "Y-axis: " + yAxis + "\n\n"
                + "Provide trends, patterns, highs/lows, correlations, and important conclusions.";
    }

    private String callGeminiAPI(String base64Image, String prompt) throws Exception {

        URL url = new URL(API_ENDPOINT + "?key=" + API_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        JSONObject request = new JSONObject();

        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();


        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);
        parts.put(textPart);

        JSONObject imagePart = new JSONObject();
        JSONObject inlineData = new JSONObject();
        inlineData.put("mime_type", "image/png");
        inlineData.put("data", base64Image);
        imagePart.put("inline_data", inlineData);
        parts.put(imagePart);

        content.put("parts", parts);
        contents.put(content);

        request.put("contents", contents);

        JSONObject generationConfig = new JSONObject();
        generationConfig.put("temperature", 0.4);
        generationConfig.put("topK", 32);
        generationConfig.put("topP", 1);
        generationConfig.put("maxOutputTokens", 2048);
        request.put("generationConfig", generationConfig);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = request.toString().getBytes("UTF-8");
            os.write(input, 0, input.length);
        }

        int code = conn.getResponseCode();
        InputStream stream = (code == 200 || code == 201)
                ? conn.getInputStream()
                : conn.getErrorStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        String responseBody = sb.toString();

        if (code != 200 && code != 201) {
            throw new Exception("Gemini API Error (" + code + "): " + responseBody);
        }

        try {
            JSONObject json = new JSONObject(responseBody);

            if (json.has("error")) {
                JSONObject error = json.getJSONObject("error");
                String errorMessage = error.optString("message", "Unknown error");
                throw new Exception("Gemini API Error: " + errorMessage);
            }

            JSONArray candidates = json.optJSONArray("candidates");

            if (candidates != null && candidates.length() > 0) {
                JSONObject first = candidates.getJSONObject(0);

                if (first.has("content")) {
                    JSONObject respContent = first.getJSONObject("content");
                    JSONArray respParts = respContent.optJSONArray("parts");

                    if (respParts != null && respParts.length() > 0) {
                        JSONObject firstPart = respParts.getJSONObject(0);
                        if (firstPart.has("text")) {
                            return firstPart.getString("text");
                        }
                    }
                }

                String finishReason = first.optString("finishReason", "");
                if (!finishReason.isEmpty() && !finishReason.equals("STOP")) {
                    throw new Exception("API returned finish reason: " + finishReason);
                }
            }

            return "Error: Could not parse AI response. Response structure unexpected.";

        } catch (org.json.JSONException je) {
            throw new Exception("Failed to parse Gemini response: " + je.getMessage() + "\nResponse: " + responseBody);
        }
    }
}