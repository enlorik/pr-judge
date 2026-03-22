package com.prjudge.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.prjudge.domain.enums.PrStatus;
import com.prjudge.dto.request.ChangedFileDto;
import com.prjudge.dto.request.PullRequestIngestRequest;
import com.prjudge.dto.response.PullRequestResponse;
import com.prjudge.service.PullRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "GitHub webhook integration")
public class WebhookController {

    private final PullRequestService pullRequestService;

    @PostMapping("/github")
    @Operation(summary = "Receive GitHub PR webhook payload")
    public ResponseEntity<?> handleGithubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String event,
            @RequestBody JsonNode payload) {

        log.info("Received GitHub webhook event: {}", event);

        if (!"pull_request".equals(event)) {
            return ResponseEntity.ok("Event ignored: " + event);
        }

        try {
            JsonNode pr = payload.path("pull_request");
            JsonNode repoNode = payload.path("repository");
            String action = payload.path("action").asText();

            log.info("PR action: {}", action);

            if (!List.of("opened", "synchronize", "reopened").contains(action)) {
                return ResponseEntity.ok("Action ignored: " + action);
            }

            PullRequestIngestRequest request = new PullRequestIngestRequest();
            request.setPrNumber(pr.path("number").asInt());
            request.setTitle(pr.path("title").asText(""));
            request.setDescription(pr.path("body").asText(""));
            request.setAuthorLogin(pr.path("user").path("login").asText(""));
            request.setSourceBranch(pr.path("head").path("ref").asText(""));
            request.setTargetBranch(pr.path("base").path("ref").asText(""));
            request.setTotalAdditions(pr.path("additions").asInt(0));
            request.setTotalDeletions(pr.path("deletions").asInt(0));
            request.setChangedFilesCount(pr.path("changed_files").asInt(0));
            request.setStatus(mapState(pr.path("state").asText("open")));

            // Try to find repository by GitHub URL or name
            // For webhook ingestion, repositoryId must be resolvable
            // Fall back to null if not configured — will fail validation cleanly
            request.setChangedFiles(new ArrayList<>());

            // Attempt to resolve repository ID from the webhook payload's full_name
            // This requires a registered repository matching the GitHub full_name
            String fullName = repoNode.path("full_name").asText("");
            Long repoId = resolveRepositoryId(fullName);
            if (repoId == null) {
                log.warn("No repository registered for GitHub repo: {}", fullName);
                return ResponseEntity.ok("Repository not registered: " + fullName);
            }
            request.setRepositoryId(repoId);

            PullRequestResponse response = pullRequestService.ingest(request, "github-webhook");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    private PrStatus mapState(String state) {
        return switch (state.toLowerCase()) {
            case "closed" -> PrStatus.CLOSED;
            case "merged" -> PrStatus.MERGED;
            case "draft" -> PrStatus.DRAFT;
            default -> PrStatus.OPEN;
        };
    }

    /**
     * Placeholder: resolve repository ID from GitHub full_name.
     * In production, query the repository table by githubUrl or owner/name.
     */
    private Long resolveRepositoryId(String fullName) {
        // Return null to signal "not found"; real implementation would query the DB
        return null;
    }
}
