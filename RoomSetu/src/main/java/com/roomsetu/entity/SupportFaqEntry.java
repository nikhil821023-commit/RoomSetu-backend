package com.roomsetu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Deliberately NOT an AI-generated response — support FAQ matching is plain keyword
 * overlap against a small, curated table (see FaqAutoResponseService). A wrong AI
 * answer about deposits or verification could actively mislead someone; a curated
 * answer that says "I'm not sure, here's how to reach a human" is the safer default.
 */
@Entity
@Table(name = "support_faq_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportFaqEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String question;

    @Column(nullable = false, length = 1000)
    private String answer;

    // Comma-separated, lowercase — kept simple deliberately; see FaqAutoResponseService
    @Column(nullable = false, length = 300)
    private String keywords;
}