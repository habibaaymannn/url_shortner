package com.example.url_shortner.repository;

import com.example.url_shortner.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LinkRepository extends JpaRepository<Link, UUID> {
}
