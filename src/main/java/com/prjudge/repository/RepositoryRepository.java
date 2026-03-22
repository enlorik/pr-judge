package com.prjudge.repository;

import com.prjudge.domain.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepositoryRepository extends JpaRepository<Repository, Long> {
    List<Repository> findByCreatedByIdAndActiveTrue(Long userId);
    List<Repository> findByActiveTrue();
    Optional<Repository> findByOwnerAndNameAndActiveTrue(String owner, String name);
}
