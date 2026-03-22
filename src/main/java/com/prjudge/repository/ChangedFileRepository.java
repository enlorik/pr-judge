package com.prjudge.repository;

import com.prjudge.domain.entity.ChangedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangedFileRepository extends JpaRepository<ChangedFile, Long> {
    List<ChangedFile> findByPullRequestRecordId(Long pullRequestRecordId);
    void deleteByPullRequestRecordId(Long pullRequestRecordId);
}
