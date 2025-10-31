package com.davidechiarelli.sportyevents.dto;

import jakarta.validation.constraints.NotBlank;

public record EventScore(
        @NotBlank
        String eventId,
        @NotBlank
        String currentScore
) {
}
