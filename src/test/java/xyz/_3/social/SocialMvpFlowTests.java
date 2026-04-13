package xyz._3.social;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SocialMvpFlowTests {

    @LocalServerPort
    int port;

    @Test
    void donationMarkPaidAppearsInOverlayAndReplayWorks() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
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
        String createdJson = createResponse.body();
        long donationId = Long.parseLong(createdJson.replaceAll(".*\"id\":(\\d+).*", "$1"));

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
                        .uri(URI.create(baseUrl("/api/v1/streamer/donations")))
                        .header("X-Streamer-Key", "change-me")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, streamerListResponse.statusCode());
        assertTrue(streamerListResponse.body().contains("\"id\""));

        HttpResponse<String> replayResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/v1/streamer/donations/" + donationId + "/replay")))
                        .header("X-Streamer-Key", "change-me")
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(204, replayResponse.statusCode());
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }
}
