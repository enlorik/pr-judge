# PR Judge

**PR Judge** is a production-ready Spring Boot service that automatically scores GitHub Pull Requests for risk and review readiness. It ingests PR metadata, evaluates it against configurable scoring rules, and produces a structured risk score — helping teams prioritize reviews and enforce merge gates.

---

## Why PR Scoring Matters

Large, unreviewed, or risky pull requests are one of the most common sources of production incidents. PR Judge helps teams:

- **Catch risky PRs early** — security-sensitive paths, missing tests, massive diffs
- **Standardize review effort** — focus reviewer energy on high-risk changes
- **Accelerate safe PRs** — small, well-tested PRs get a MERGE recommendation instantly
- **Audit all decisions** — full audit log of scores, overrides, and ingestion events

---

## Scoring Model

| Rule | Risk Points | Readiness Impact |
|------|-------------|-----------------|
| Diff > 500 lines | +20 | -10 |
| Diff > 1000 lines | +30 | -10 |
| Files changed > 10 | +15 | — |
| Files changed > 20 | +20 | — |
| Security-sensitive paths | +25 | -10 |
| Database migration files | +20 | — |
| No test files | +15 | -20 |
| Has test files | — | +10 |
| Empty/short description | +10 | -15 |
| Good description | — | +10 |
| Poor title (hotfix/wip/hack) | +10 | -5 |
| Mixed concerns (4+ dirs) | +10 | — |
| Small PR (< 100 changes) | — | +10 |

**Risk Categories:**
- `SAFE` (0–30) → `MERGE`
- `REVIEW_NEEDED` (31–60) → `REVIEW_FIRST`
- `HIGH_RISK` (61+) → `DO_NOT_MERGE`

---

## Example PR Input

```json
{
  "prNumber": 42,
  "title": "Add user profile page",
  "description": "Implements user profile page with avatar upload and bio field. Includes unit and integration tests.",
  "authorLogin": "dev-user",
  "sourceBranch": "feature/user-profile",
  "targetBranch": "main",
  "totalAdditions": 180,
  "totalDeletions": 40,
  "changedFilesCount": 8,
  "repositoryId": 1,
  "status": "OPEN",
  "changedFiles": [
    { "filePath": "src/main/UserController.java", "additions": 60, "deletions": 10 },
    { "filePath": "src/main/UserService.java", "additions": 80, "deletions": 20 },
    { "filePath": "src/test/UserControllerTest.java", "additions": 40, "deletions": 10 }
  ]
}
```

## Example Score Output

```json
{
  "id": 1,
  "riskScore": 0.0,
  "reviewReadinessScore": 100.0,
  "riskCategory": "SAFE",
  "mergeRecommendation": "MERGE",
  "overridden": false,
  "reasons": [
    {
      "reasonCode": "HAS_TESTS",
      "description": "PR includes test files",
      "impactScore": 10.0,
      "positive": true
    },
    {
      "reasonCode": "GOOD_DESCRIPTION",
      "description": "PR has a meaningful description",
      "impactScore": 10.0,
      "positive": true
    }
  ]
}
```

---

## Local Setup

### Prerequisites
- Java 17
- Maven 3.9+
- Docker (optional, for PostgreSQL)

### Run with H2 (default dev mode)

```bash
git clone https://github.com/your-org/pr-judge.git
cd pr-judge
mvn spring-boot:run
```

App starts at `http://localhost:8080`. H2 console at `http://localhost:8080/h2-console`.

### Run with Docker Compose (PostgreSQL)

```bash
mvn clean package -DskipTests
docker-compose up --build
```

### API Documentation

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login, get JWT token |
| GET | `/api/repositories` | List repositories |
| POST | `/api/repositories` | Create a repository |
| PUT | `/api/repositories/{id}` | Update a repository |
| DELETE | `/api/repositories/{id}` | Soft-delete repository |
| POST | `/api/pull-requests` | Ingest & auto-score a PR |
| GET | `/api/pull-requests` | List PRs (filter by repo, status, risk) |
| GET | `/api/pull-requests/{id}` | Get PR details |
| GET | `/api/pull-requests/{id}/score` | Get score for PR |
| POST | `/api/pull-requests/{id}/score` | Re-score a PR |
| POST | `/api/pull-requests/{id}/override` | Override a score |
| GET | `/api/scoring-rules` | List scoring rules |
| POST | `/api/scoring-rules` | Create rule (ADMIN) |
| PUT | `/api/scoring-rules/{id}` | Update rule (ADMIN) |
| DELETE | `/api/scoring-rules/{id}` | Delete rule (ADMIN) |
| POST | `/api/webhooks/github` | GitHub PR webhook |

---

## Future Improvements

- **GitHub App integration** — automatically register repos via GitHub App installation
- **PR diff fetching** — pull actual file diffs from GitHub API for more accurate scoring
- **Custom rule DSL** — allow admins to define rules via a configuration DSL without code changes
- **Team dashboards** — aggregate risk metrics per repository and team
- **Notification hooks** — send Slack/email alerts for HIGH_RISK PRs
- **ML-based scoring** — train models on historical merge outcomes to improve predictions
- **RBAC** — more granular role-based access control beyond USER/ADMIN
An automated pull request scoring engine that evaluates PR risk, review readiness, and merge quality using diff signals, repository rules, and explainable scoring.
