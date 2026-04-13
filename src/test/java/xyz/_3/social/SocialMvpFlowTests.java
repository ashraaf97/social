package xyz._3.social;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SocialMvpFlowTests {

    @LocalServerPort
    int port;

    @Test
    void donationMarkPaidAppearsInOverlayAndReplayWorks() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String loginBody = """
                {"username":"admin","password":"admin-test-pass"}
                """;
        HttpResponse<String> loginResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/auth/login")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, loginResponse.statusCode());
        String token = loginResponse.body().replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        String createBody = """
                {
                  "streamerId": "streamer-demo",
                  "senderName": "Alice",
                  "amount": 12.50,
                  "currency": "usd",
                  "messageText": "great stream"
                }
                """;
        HttpResponse<String> createResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/donations")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(createBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, createResponse.statusCode());
        assertTrue(createResponse.body().contains("\"status\":\"PENDING_PAYMENT\""));
        long donationId = Long.parseLong(createResponse.body().replaceAll(".*\"id\":(\\d+).*", "$1"));

        HttpResponse<String> markPaidResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/donations/" + donationId + "/mark-paid")))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, markPaidResponse.statusCode());
        assertTrue(markPaidResponse.body().contains("\"status\":\"PAID\""));

        HttpResponse<String> pollResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/overlay/events?streamerId=streamer-demo&cursor=0")))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, pollResponse.statusCode());
        assertTrue(pollResponse.body().contains("\"donationId\":" + donationId));

        HttpResponse<String> streamerListResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/streamer/donations?streamerId=streamer-demo")))
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, streamerListResponse.statusCode());
        assertTrue(streamerListResponse.body().contains("\"id\""));

        HttpResponse<String> summaryResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/streamer/donations/summary?streamerId=streamer-demo")))
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, summaryResponse.statusCode());
        assertTrue(summaryResponse.body().contains("\"streamerId\":\"streamer-demo\""));
        assertTrue(summaryResponse.body().contains("\"totalDonations\":1"));

        HttpResponse<String> replayResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/streamer/donations/" + donationId + "/replay?streamerId=streamer-demo")))
                        .header("Authorization", "Bearer " + token)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(204, replayResponse.statusCode());

        HttpResponse<String> adminStreamersResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/admin/streamers")))
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, adminStreamersResponse.statusCode());
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
