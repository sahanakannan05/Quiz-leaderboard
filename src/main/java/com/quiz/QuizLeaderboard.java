package com.quiz;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class QuizLeaderboard {

    // ── CONFIG ──────────────────────────────────────────────────────────────
    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final String REG_NO   = "RA2311053010140"; // <-- replace with your reg number
    private static final int    TOTAL_POLLS   = 10;
    private static final int    POLL_DELAY_MS = 5000; // 5 seconds between polls
    // ────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // Key: "roundId::participant"  →  score
        // Using a Set to track which (roundId, participant) pairs we have already seen
        Set<String> seen        = new HashSet<>();
        Map<String, Integer> scores = new LinkedHashMap<>();

        // ── STEP 1: Poll 10 times ────────────────────────────────────────────
        for (int poll = 0; poll < TOTAL_POLLS; poll++) {
            String url = BASE_URL + "/quiz/messages?regNo=" + REG_NO + "&poll=" + poll;
            System.out.println("[Poll " + poll + "] GET " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            System.out.println("[Poll " + poll + "] Response: " + body);

            // ── STEP 2: Parse and deduplicate ────────────────────────────────
            JSONObject json   = new JSONObject(body);
            JSONArray  events = json.getJSONArray("events");

            for (int i = 0; i < events.length(); i++) {
                JSONObject event       = events.getJSONObject(i);
                String     roundId     = event.getString("roundId");
                String     participant = event.getString("participant");
                int        score       = event.getInt("score");

                // Deduplication key: roundId + participant
                String dedupKey = roundId + "::" + participant;

                if (seen.contains(dedupKey)) {
                    System.out.println("  [DUPLICATE] Skipping " + dedupKey);
                    continue;
                }

                seen.add(dedupKey);
                scores.merge(participant, score, Integer::sum);
                System.out.println("  [ADDED] " + participant + " +" + score + " (key: " + dedupKey + ")");
            }

            // ── STEP 3: Wait 5 seconds before next poll ──────────────────────
            if (poll < TOTAL_POLLS - 1) {
                System.out.println("  Waiting 5 seconds...\n");
                Thread.sleep(POLL_DELAY_MS);
            }
        }

        // ── STEP 4: Build leaderboard sorted by totalScore descending ────────
        List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>(scores.entrySet());
        leaderboard.sort((a, b) -> b.getValue() - a.getValue());

        System.out.println("\n── LEADERBOARD ──────────────────────────────");
        int totalScore = 0;
        JSONArray leaderboardJson = new JSONArray();

        for (Map.Entry<String, Integer> entry : leaderboard) {
            System.out.println(entry.getKey() + " → " + entry.getValue());
            totalScore += entry.getValue();

            JSONObject item = new JSONObject();
            item.put("participant", entry.getKey());
            item.put("totalScore", entry.getValue());
            leaderboardJson.put(item);
        }
        System.out.println("Total Score (all users): " + totalScore);
        System.out.println("──────────────────────────────────────────────\n");

        // ── STEP 5: Submit leaderboard once ──────────────────────────────────
        JSONObject submitBody = new JSONObject();
        submitBody.put("regNo", REG_NO);
        submitBody.put("leaderboard", leaderboardJson);

        String submitPayload = submitBody.toString();
        System.out.println("[SUBMIT] POST " + BASE_URL + "/quiz/submit");
        System.out.println("[SUBMIT] Payload: " + submitPayload);

        HttpRequest submitRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/quiz/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(submitPayload))
                .build();

        HttpResponse<String> submitResponse = client.send(submitRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("[SUBMIT] Response: " + submitResponse.body());
    }
}
