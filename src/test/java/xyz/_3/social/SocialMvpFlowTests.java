package xyz._3.social;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
class SocialMvpFlowTests {

    @Autowired
    ApplicationContext applicationContext;

    @Test
    void donationMarkPaidAppearsInOverlayAndReplayWorks() throws Exception {
        WebTestClient webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
        byte[] createResponse = webTestClient.post()
                .uri("/api/v1/donations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "streamerId": "streamer-demo",
                          "senderName": "Alice",
                          "amount": 12.50,
                          "currency": "usd",
                          "messageText": "great stream"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("PENDING_PAYMENT")
                .returnResult()
                .getResponseBodyContent();
        String createdJson = new String(createResponse);
        long donationId = Long.parseLong(createdJson.replaceAll(".*\"id\":(\\d+).*", "$1"));

        webTestClient.post()
                .uri("/api/v1/donations/{id}/mark-paid", donationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("PAID");

        webTestClient.get()
                .uri("/api/v1/overlay/events?streamerId=streamer-demo&cursor=0")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.events.length()").isEqualTo(1)
                .jsonPath("$.events[0].donationId").isEqualTo((int) donationId);

        webTestClient.get()
                .uri("/api/v1/streamer/donations")
                .header("X-Streamer-Key", "change-me")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").exists();

        webTestClient.post()
                .uri("/api/v1/streamer/donations/{id}/replay", donationId)
                .header("X-Streamer-Key", "change-me")
                .exchange()
                .expectStatus().isNoContent();
    }
}
