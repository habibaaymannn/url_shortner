package com.example.url_shortner.mapper;

import com.example.url_shortner.dtos.ShortUrlResponse;
import com.example.url_shortner.dtos.UrlCreationRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LinkMapper {

    ShortUrlResponse toShortUrlResponse(UrlCreationRequest urlCreationRequest);
}
