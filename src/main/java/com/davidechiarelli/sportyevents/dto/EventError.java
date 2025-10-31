package com.davidechiarelli.sportyevents.dto;

public record EventError(
        String eventId,
        String errorMessage,
        String exceptionClass,
        String stackTrace,
        long timestamp
) {}
