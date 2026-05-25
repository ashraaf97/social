package com.beam.social;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SocialMvpFlowTests {

    private static final String STREAMER_USERNAME = "streamer1";
    private static final String STREAMER_PASSWORD = "streamer123";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin-test-pass";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    @LocalServerPort
    int port;

    @Test
    void donationMarkPaidAppearsInOverlayAndReplayWorks() throws Exception {
        final JsonNode streamerLogin = login(STREAMER_USERNAME, STREAMER_PASSWORD);
        final String streamerId = streamerLogin.get("streamerId").asText();
        final String donationToken = streamerLogin.get("donationToken").asText();
        final String overlayToken = streamerLogin.get("overlayToken").asText();

        final JsonNode adminLogin = login(ADMIN_USERNAME, ADMIN_PASSWORD);
        final String adminJwt = adminLogin.get("token").asText();

        final String createBody = """
                {
                  "donationToken": "%s",
                  "senderName": "Alice",
                  "amount": 12.50,
                  "currency": "myr",
                  "messageText": "great stream"
                }
                """.formatted(donationToken);
        final HttpResponse<String> createResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/donations")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(createBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, createResponse.statusCode());
        assertTrue(createResponse.body().contains("\"status\":\"PENDING_PAYMENT\""));
        final long donationId = objectMapper.readTree(createResponse.body()).get("id").asLong();

        final HttpResponse<String> markPaidResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/donations/" + donationId + "/mark-paid")))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, markPaidResponse.statusCode());
        assertTrue(markPaidResponse.body().contains("\"status\":\"PAID\""));

        final HttpResponse<String> pollResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/overlay/events?token=" + overlayToken + "&cursor=0")))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, pollResponse.statusCode());
        assertTrue(pollResponse.body().contains("\"donationId\":" + donationId));

        final HttpResponse<String> streamerListResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/streamer/donations?streamerId=" + streamerId)))
                        .header("Authorization", "Bearer " + adminJwt)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, streamerListResponse.statusCode());
        assertTrue(streamerListResponse.body().contains("\"id\""));

        final HttpResponse<String> summaryResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/streamer/donations/summary?streamerId=" + streamerId)))
                        .header("Authorization", "Bearer " + adminJwt)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, summaryResponse.statusCode());
        assertTrue(summaryResponse.body().contains("\"streamerId\":\"" + streamerId + "\""));
        assertTrue(summaryResponse.body().contains("\"totalDonations\":1"));

        final HttpResponse<String> replayResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/streamer/donations/" + donationId + "/replay?streamerId=" + streamerId)))
                        .header("Authorization", "Bearer " + adminJwt)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(204, replayResponse.statusCode());

        final HttpResponse<String> adminStreamersResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/admin/streamers")))
                        .header("Authorization", "Bearer " + adminJwt)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, adminStreamersResponse.statusCode());
    }

    private JsonNode login(String username, String password) throws Exception {
        final String loginBody = """
                {"username":"%s","password":"%s"}
                """.formatted(username, password);
        final HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/auth/login")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, response.statusCode());
        return objectMapper.readTree(response.body());
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
