package com.prjudge.controller;

import com.prjudge.dto.request.RepositoryRequest;
import com.prjudge.dto.response.RepositoryResponse;
import com.prjudge.service.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
@Tag(name = "Repositories", description = "Repository management")
public class RepositoryController {

    private final RepositoryService repositoryService;

    @PostMapping
    @Operation(summary = "Register a new repository")
    public ResponseEntity<RepositoryResponse> create(
            @Valid @RequestBody RepositoryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repositoryService.create(request, userDetails.getUsername()));
    }

    @GetMapping
    @Operation(summary = "List all active repositories")
    public ResponseEntity<List<RepositoryResponse>> findAll() {
        return ResponseEntity.ok(repositoryService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get repository by ID")
    public ResponseEntity<RepositoryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(repositoryService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a repository")
    public ResponseEntity<RepositoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RepositoryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(repositoryService.update(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a repository")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        repositoryService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
