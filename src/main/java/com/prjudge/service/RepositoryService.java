package com.prjudge.service;

import com.prjudge.domain.entity.Repository;
import com.prjudge.domain.entity.User;
import com.prjudge.domain.enums.AuditAction;
import com.prjudge.dto.request.RepositoryRequest;
import com.prjudge.dto.response.RepositoryResponse;
import com.prjudge.exception.ResourceNotFoundException;
import com.prjudge.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final RepositoryRepository repositoryRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;

    @Transactional
    public RepositoryResponse create(RepositoryRequest request, String username) {
        User user = userService.findByUsername(username);
        Repository repo = Repository.builder()
                .name(request.getName())
                .owner(request.getOwner())
                .githubUrl(request.getGithubUrl())
                .description(request.getDescription())
                .active(request.isActive())
                .createdBy(user)
                .build();
        Repository saved = repositoryRepository.save(repo);
        auditLogService.log(AuditAction.REPOSITORY_REGISTERED, "Repository", saved.getId(), username,
                "Repository created: " + saved.getName());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RepositoryResponse> findAll() {
        return repositoryRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RepositoryResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public RepositoryResponse update(Long id, RepositoryRequest request, String username) {
        Repository repo = getOrThrow(id);
        repo.setName(request.getName());
        repo.setOwner(request.getOwner());
        repo.setGithubUrl(request.getGithubUrl());
        repo.setDescription(request.getDescription());
        repo.setActive(request.isActive());
        return toResponse(repositoryRepository.save(repo));
    }

    @Transactional
    public void delete(Long id, String username) {
        Repository repo = getOrThrow(id);
        repo.setActive(false);
        repositoryRepository.save(repo);
        log.info("Repository {} soft-deleted by {}", id, username);
    }

    public Repository getOrThrow(Long id) {
        return repositoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", id));
    }

    private RepositoryResponse toResponse(Repository r) {
        return RepositoryResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .owner(r.getOwner())
                .githubUrl(r.getGithubUrl())
                .description(r.getDescription())
                .active(r.isActive())
                .createdByUsername(r.getCreatedBy().getUsername())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
