package xyz._3.social.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "donations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String streamerId;
    private String senderName;
    private BigDecimal amount;
    private String currency;
    private String messageText;
    private String voiceProfile;
    @Enumerated(EnumType.STRING)
    private TtsStatus ttsStatus;
    @Enumerated(EnumType.STRING)
    private DonationStatus status;
    private Instant createdAt;
}
