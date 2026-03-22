package com.prjudge.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.prjudge.domain.enums.PrStatus;
import com.prjudge.dto.request.PullRequestIngestRequest;
import com.prjudge.dto.response.PullRequestResponse;
import com.prjudge.repository.RepositoryRepository;
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
    private final RepositoryRepository repositoryRepository;

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

            // Resolve repository by owner/name from the webhook payload
            String fullName = repoNode.path("full_name").asText("");
            String[] parts = fullName.split("/", 2);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                log.warn("Invalid repository full_name in webhook payload: {}", fullName);
                return ResponseEntity.badRequest().body("Invalid repository full_name: " + fullName);
            }
            String owner = parts[0];
            String name = parts[1];

            Long repoId = repositoryRepository.findByOwnerAndNameAndActiveTrue(owner, name)
                    .map(r -> r.getId())
                    .orElse(null);

            if (repoId == null) {
                log.warn("No registered repository found for GitHub repo: {}", fullName);
                return ResponseEntity.ok("Repository not registered: " + fullName);
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
            request.setRepositoryId(repoId);
            request.setChangedFiles(new ArrayList<>());

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
}
