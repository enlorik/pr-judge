package com.prjudge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prjudge.domain.entity.*;
import com.prjudge.domain.enums.*;
import com.prjudge.dto.request.PullRequestIngestRequest;
import com.prjudge.dto.response.PullRequestResponse;
import com.prjudge.dto.response.ScoreResultResponse;
import com.prjudge.security.JwtTokenProvider;
import com.prjudge.security.UserDetailsServiceImpl;
import com.prjudge.service.PullRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PullRequestController.class)
@MockBean(JpaMetamodelMappingContext.class)
class PullRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PullRequestService pullRequestService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private PullRequestResponse buildPrResponse(Long id) {
        ScoreResultResponse score = ScoreResultResponse.builder()
                .id(1L)
                .riskScore(25.0)
                .reviewReadinessScore(85.0)
                .riskCategory(RiskCategory.SAFE)
                .mergeRecommendation(MergeRecommendation.MERGE)
                .overridden(false)
                .reasons(List.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return PullRequestResponse.builder()
                .id(id)
                .prNumber(42)
                .title("Add feature X")
                .description("Implements feature X with full test coverage and documentation.")
                .authorLogin("dev-user")
                .sourceBranch("feature/x")
                .targetBranch("main")
                .totalAdditions(80)
                .totalDeletions(20)
                .changedFilesCount(5)
                .status(PrStatus.OPEN)
                .repositoryId(1L)
                .repositoryName("my-repo")
                .scoreResult(score)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void ingestPullRequest_shouldReturn201WithScore() throws Exception {
        PullRequestIngestRequest request = new PullRequestIngestRequest();
        request.setPrNumber(42);
        request.setTitle("Add feature X");
        request.setDescription("Implements feature X with full test coverage and documentation.");
        request.setRepositoryId(1L);

        given(pullRequestService.ingest(any(), eq("testuser")))
                .willReturn(buildPrResponse(1L));

        mockMvc.perform(post("/api/pull-requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.prNumber").value(42))
                .andExpect(jsonPath("$.scoreResult.riskCategory").value("SAFE"))
                .andExpect(jsonPath("$.scoreResult.mergeRecommendation").value("MERGE"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getPullRequestById_shouldReturn200() throws Exception {
        given(pullRequestService.findById(1L)).willReturn(buildPrResponse(1L));

        mockMvc.perform(get("/api/pull-requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Add feature X"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getScore_shouldReturnScoreResult() throws Exception {
        ScoreResultResponse score = ScoreResultResponse.builder()
                .id(1L)
                .riskScore(25.0)
                .reviewReadinessScore(85.0)
                .riskCategory(RiskCategory.SAFE)
                .mergeRecommendation(MergeRecommendation.MERGE)
                .overridden(false)
                .reasons(List.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        given(pullRequestService.getScore(1L)).willReturn(score);

        mockMvc.perform(get("/api/pull-requests/1/score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskScore").value(25.0))
                .andExpect(jsonPath("$.riskCategory").value("SAFE"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void ingestWithoutTitle_shouldReturn400() throws Exception {
        PullRequestIngestRequest request = new PullRequestIngestRequest();
        request.setPrNumber(1);
        // Missing title and repositoryId

        mockMvc.perform(post("/api/pull-requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void accessWithoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/pull-requests"))
                .andExpect(status().isUnauthorized());
    }
}
