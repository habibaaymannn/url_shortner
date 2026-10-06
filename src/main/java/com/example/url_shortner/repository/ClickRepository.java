package com.example.url_shortner.repository;

import com.example.url_shortner.entity.Click;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClickRepository extends JpaRepository<Click, UUID> {
}
