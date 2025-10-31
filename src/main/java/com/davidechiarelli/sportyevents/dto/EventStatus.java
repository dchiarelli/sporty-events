package com.davidechiarelli.sportyevents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventStatus(
        @NotBlank
        String eventId,
        @NotNull
        Boolean status
) {
}
