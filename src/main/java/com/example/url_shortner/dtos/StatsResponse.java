package com.example.url_shortner.dtos;

import java.time.LocalDateTime;
import java.util.Map;

public record StatsResponse(
        String shortCode,
        String originalUrl,
        LocalDateTime createdAt,
        long totalClicks,
        Map<String, Long> clicksByReferrer
) {}