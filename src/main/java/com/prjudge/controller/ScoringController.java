package com.prjudge.controller;

import com.prjudge.domain.entity.ScoringRule;
import com.prjudge.dto.request.ScoringRuleRequest;
import com.prjudge.service.ScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scoring-rules")
@RequiredArgsConstructor
@Tag(name = "Scoring Rules", description = "Manage scoring rule definitions")
public class ScoringController {

    private final ScoringService scoringService;

    @GetMapping
    @Operation(summary = "List all scoring rules")
    public ResponseEntity<List<ScoringRule>> findAll() {
        return ResponseEntity.ok(scoringService.findAll());
    }

    @PostMapping
    @Operation(summary = "Create a new scoring rule (ADMIN only)")
    public ResponseEntity<ScoringRule> create(@Valid @RequestBody ScoringRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scoringService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a scoring rule (ADMIN only)")
    public ResponseEntity<ScoringRule> update(
            @PathVariable Long id,
            @Valid @RequestBody ScoringRuleRequest request) {
        return ResponseEntity.ok(scoringService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a scoring rule (ADMIN only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scoringService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
