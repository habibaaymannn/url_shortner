package com.example.url_shortner.repository;

import com.example.url_shortner.entity.Click;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClickRepository extends JpaRepository<Click, UUID> {
    long countByLinkId(UUID linkId);

    // each row: [referrer or "direct", count]
    @Query("""
           select coalesce(c.referrer, 'direct'), count(c)
           from Click c
           where c.link.id = :linkId
           group by c.referrer
           """)
    List<Object[]> countByReferrer(@Param("linkId") UUID linkId);
}
