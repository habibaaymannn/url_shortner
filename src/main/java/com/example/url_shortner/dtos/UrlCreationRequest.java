package com.example.url_shortner.dtos;


import jakarta.validation.constraints.Pattern;

public record UrlCreationRequest (

    @Pattern(
            regexp = "^(https?)://[\\w.-]+(:[0-9]+)?([/?#].*)?$",
            message = "originalUrl is not a valid URL"
    )
    String originalUrl,

    //optional
    String alias

)    {}
