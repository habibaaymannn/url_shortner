package com.example.url_shortner.dtos;

import java.time.LocalDateTime;

public record ShortUrlResponse(
        String shortCode,
        String shortUrl,
        String originalUrl,
        LocalDateTime createdAt
)
{}
