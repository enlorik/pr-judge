package com.prjudge.controller;

import com.prjudge.domain.enums.PrStatus;
import com.prjudge.domain.enums.RiskCategory;
import com.prjudge.dto.request.PullRequestIngestRequest;
import com.prjudge.dto.request.ScoreOverrideRequest;
import com.prjudge.dto.response.PullRequestResponse;
import com.prjudge.dto.response.ScoreResultResponse;
import com.prjudge.service.PullRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pull-requests")
@RequiredArgsConstructor
@Tag(name = "Pull Requests", description = "PR ingestion, listing, and scoring")
public class PullRequestController {

    private final PullRequestService pullRequestService;

    @PostMapping
    @Operation(summary = "Ingest a pull request and auto-score it")
    public ResponseEntity<PullRequestResponse> ingest(
            @Valid @RequestBody PullRequestIngestRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pullRequestService.ingest(request, userDetails.getUsername()));
    }

    @GetMapping
    @Operation(summary = "List pull requests with optional filters")
    public ResponseEntity<Page<PullRequestResponse>> list(
            @RequestParam(required = false) Long repositoryId,
            @RequestParam(required = false) PrStatus status,
            @RequestParam(required = false) RiskCategory riskCategory,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(pullRequestService.findAll(repositoryId, status, riskCategory, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a pull request by ID")
    public ResponseEntity<PullRequestResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(pullRequestService.findById(id));
    }

    @GetMapping("/{id}/score")
    @Operation(summary = "Get the score for a pull request")
    public ResponseEntity<ScoreResultResponse> getScore(@PathVariable Long id) {
        return ResponseEntity.ok(pullRequestService.getScore(id));
    }

    @PostMapping("/{id}/score")
    @Operation(summary = "Trigger rescoring of a pull request")
    public ResponseEntity<ScoreResultResponse> rescore(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pullRequestService.rescore(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/override")
    @Operation(summary = "Manually override a score")
    public ResponseEntity<ScoreResultResponse> override(
            @PathVariable Long id,
            @Valid @RequestBody ScoreOverrideRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pullRequestService.override(id, request, userDetails.getUsername()));
    }
}
