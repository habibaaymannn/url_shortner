package com.example.url_shortner.service;

import com.example.url_shortner.dtos.ShortUrlResponse;
import com.example.url_shortner.dtos.UrlCreationRequest;
import com.example.url_shortner.entity.Link;
import com.example.url_shortner.exceptionhandler.FunctionalException;
import com.example.url_shortner.repository.ClickRepository;
import com.example.url_shortner.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {
    private final LinkRepository linkRepository;
    private final ClickRepository clickRepository;

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_CODE_LENGTH = 7;
    private static final int MAX_GENERATION_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();
    @Value("${app.base-url}")
    private String baseUrl;


    public ShortUrlResponse createShortUrl(UrlCreationRequest request) {
        //code
        if (request == null || request.originalUrl() == null || request.originalUrl().isBlank()) {
            throw new FunctionalException(HttpStatus.BAD_REQUEST, "originalUrl is required");
        }

        String shortCode = request.alias() == null || request.alias().isBlank()
                ? generateUniqueShortCode()
                : validateAndUseAlias(request.alias().trim());

        Link link = new Link();
        link.setShortCode(shortCode);
        link.setOriginalUrl(request.originalUrl().trim());
        link.setCreatedAt(LocalDateTime.now());

        Link savedLink;
        try {
            savedLink = linkRepository.saveAndFlush(link);
        } catch (org.springframework.dao.DataIntegrityViolationException exception) {
            // Handles a race where another request reserves the same alias/code after our check.
            throw new FunctionalException(HttpStatus.CONFLICT, "The requested short code is already in use");
        }

        String normalizedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        return new ShortUrlResponse(
                savedLink.getShortCode(),
                normalizedBaseUrl + "/" + savedLink.getShortCode(),
                savedLink.getOriginalUrl(),
                savedLink.getCreatedAt()
        );
    }

    public String getOriginalUrl(String shortUrl) {
        //add cache later
        Link link = linkRepository.findByShortCode(shortUrl).orElseThrow(() -> new FunctionalException(HttpStatus.NOT_FOUND, "Link does not exist"));
        return link.getOriginalUrl();
    }

    private String validateAndUseAlias(String alias) {
        if (!alias.matches("[a-zA-Z0-9_-]{3,20}")) {
            throw new FunctionalException(HttpStatus.BAD_REQUEST,
                    "Alias must be 3-20 characters and contain only letters, numbers, '_' or '-'");
        }
        if (linkRepository.existsByShortCode(alias)) {
            throw new FunctionalException(HttpStatus.CONFLICT, "The requested alias is already in use");
        }
        return alias;
    }

    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            StringBuilder code = new StringBuilder(SHORT_CODE_LENGTH);
            for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
                code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
            }
            String candidate = code.toString();
            if (!linkRepository.existsByShortCode(candidate)) {
                return candidate;
            }
        }
        throw new FunctionalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not generate a unique short code. Please try again.");
    }
}
