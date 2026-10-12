package com.example.url_shortner.service;

import com.example.url_shortner.dtos.ShortUrlResponse;
import com.example.url_shortner.dtos.StatsResponse;
import com.example.url_shortner.dtos.UrlCreationRequest;
import com.example.url_shortner.entity.Click;
import com.example.url_shortner.entity.Link;
import com.example.url_shortner.exceptionhandler.FunctionalException;
import com.example.url_shortner.repository.ClickRepository;
import com.example.url_shortner.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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

    public String redirectUrl(String shortCode, String referrer) {
        //add cache later
        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new FunctionalException(HttpStatus.NOT_FOUND, "Link does not exist"));

        // A failed analytics write must never break the redirect itself
        // will be replaced by kafka
        try {
            Click click = new Click();
            click.setLink(link);
            click.setClickedAt(LocalDateTime.now());
            click.setReferrer(truncate(referrer)); // to limit referrer to 255 char only
            clickRepository.save(click);
        } catch (Exception e) {
            log.warn("Could not record click for {}", shortCode, e);
        }
        return link.getOriginalUrl();
    }

    public StatsResponse getStats(String shortCode) {
        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new FunctionalException(HttpStatus.NOT_FOUND, "Link does not exist"));

        Map<String, Long> byReferrer = new HashMap<>();
        for (Object[] row : clickRepository.countByReferrer(link.getId())) {
            byReferrer.put((String) row[0], (Long) row[1]);
        }

        return new StatsResponse(
                link.getShortCode(),
                link.getOriginalUrl(),
                link.getCreatedAt(),
                clickRepository.countByLinkId(link.getId()),
                byReferrer
        );
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

    private String truncate(String value) {
        if (value == null || value.isBlank()) return null;
        return value.length() <= 255 ? value : value.substring(0, 255);
    }
}
