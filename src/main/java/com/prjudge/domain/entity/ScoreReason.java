package com.prjudge.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "score_reasons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ScoreReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reason_code", nullable = false, length = 100)
    private String reasonCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "impact_score", nullable = false)
    private Double impactScore;

    @Column(nullable = false)
    private boolean positive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_result_id", nullable = false)
    private ScoreResult scoreResult;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
