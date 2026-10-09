package com.example.url_shortner.controller;

import com.example.url_shortner.dtos.ShortUrlResponse;
import com.example.url_shortner.dtos.UrlCreationRequest;
import com.example.url_shortner.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/urls")
public class UrlController {
    private final UrlService urlService;

    @PostMapping
    ResponseEntity<ShortUrlResponse> createShortUrl(@Valid @RequestBody UrlCreationRequest urlCreationRequest) {
        ShortUrlResponse shortUrlResponse = urlService.createShortUrl(urlCreationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(shortUrlResponse);
    }
}
