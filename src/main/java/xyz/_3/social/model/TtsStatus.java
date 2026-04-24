package xyz._3.social.model;

public enum TtsStatus {
    /** Donation created; TTS not yet requested. */
    PENDING,
    /** Handed off to the TTS queue, waiting for a worker. */
    QUEUED,
    /** TTS audio is currently being synthesised. */
    PROCESSING,
    /** Audio was successfully synthesised and delivered to the overlay. */
    COMPLETED,
    /** Synthesis or delivery failed; eligible for retry. */
    FAILED,
    /** Intentionally bypassed (e.g. empty message, muted streamer, manual skip). */
    SKIPPED
}
