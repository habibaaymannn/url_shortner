package com.example.url_shortner.service;

import com.example.url_shortner.dtos.ShortUrlResponse;
import com.example.url_shortner.dtos.UrlCreationRequest;
import com.example.url_shortner.entity.Link;
import com.example.url_shortner.exceptionhandler.FunctionalException;
import com.example.url_shortner.mapper.LinkMapper;
import com.example.url_shortner.repository.ClickRepository;
import com.example.url_shortner.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class urlService {
    private final LinkRepository linkRepository;
    private final ClickRepository clickRepository;
    private final LinkMapper linkMapper;

    public ShortUrlResponse createShortUrl(UrlCreationRequest urlCreationRequest) {
        //code
        return linkMapper.toShortUrlResponse(urlCreationRequest);
    }

    public String getOriginalUrl(String shortUrl) {
        //add cache
        Link link = linkRepository.findByShortCode(shortUrl).orElseThrow(() -> new FunctionalException(HttpStatus.NOT_FOUND, "Link does not exist"));
        return link.getOriginalUrl();
    }
}
