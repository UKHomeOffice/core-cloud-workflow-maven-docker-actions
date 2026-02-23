# core-cloud-workflow-maven-docker-actions

> **Jira:** CCL-6306 | **Team:** Core Cloud Platform – Sauron | **Owner:** corecloud@homeoffice.gov.uk

Reusable GitHub Actions pipeline for building, scanning and publishing Maven-based Docker workloads across all Core Cloud Product teams and tenants.

---

## Overview

This repo provides a composable `workflow_call` pipeline that any consumer repo can reference with a single `uses:` declaration. It eliminates pipeline duplication across the platform and ensures consistent security scanning on every build.

```
Consumer Repo
    └── calls ──► .github/workflows/maven-docker-pipeline.yml   (this repo)
                        │
                        ├── .github/workflows/checkov-sonar-scan.yml  (this repo)
                        │       ├── Checkov → GitHub Advanced Security
                        │       └── SonarQube → core-cloud-workflow-sonarqube-scan
                        │
                        ├── actions/maven-setup/   (this repo)
                        ├── actions/maven-build/   (this repo)
                        │
                        └── calls ──► core-cloud-workflow-docker-actions (CCL-6431)
                                        ├── docker-setup
                                        ├── docker-build
                                        ├── docker-scan   (Trivy)
                                        └── docker-push   (AWS ECR + OIDC)
```

---

## Pipeline Stages

```
┌───────────────────────────────────────────────────────────────────────┐
│                    maven-docker-pipeline.yml                          │
├──────────────────┬────────────┬──────────────┬───────────────────────┤
│  1. Secret       │  2. SAST   │  3. Maven    │  4. Docker       5.   │
│     Detection    │            │     Build    │     Build &    Publish │
│                  │            │              │     Scan       (ECR)   │
│  Gitleaks        │  Checkov   │  validate    │  docker-setup          │
│  (full history)  │  SonarQube │  compile     │  docker-build          │
│                  │            │  test        │  docker-scan           │
│                  │            │  package     │  (Trivy)       push    │
└──────────────────┴────────────┴──────────────┴───────────────────────┘
                                                            ▲
                                                only when publish: true
```

| # | Job | Description |
|---|---|---|
| 1 | `secret-detection` | Gitleaks scans full git history; blocks pipeline on findings |
| 2 | `sast` | Checkov (SARIF → GitHub Security) + SonarQube analysis |
| 3 | `maven-build` | `validate` → `compile` → `test` → `package`; uploads JAR artifact |
| 4 | `docker-build-scan` | Docker build + Trivy image scan; no push |
| 5 | `docker-publish` | Docker build + scan + push to AWS ECR; only when `publish: true` |

---

## Repository Structure

```
core-cloud-workflow-maven-docker-actions/
│
├── .github/
│   └── workflows/
│       ├── maven-docker-pipeline.yml   ← Reusable pipeline (workflow_call)
│       └── checkov-sonar-scan.yml      ← SAST: Checkov + SonarQube
│
├── actions/
│   ├── maven-setup/
│   │   └── action.yml                  ← Composite: JDK + cache + settings.xml
│   └── maven-build/
│       └── action.yml                  ← Composite: validate→compile→test→package
│
├── scripts/
│   └── filter-sarif.py                 ← Removes test fixture paths from SARIF
│
├── tests/
│   └── dummy-service/                  ← Throw-away Spring Boot service (CCL-6306 testing)
│       ├── Dockerfile
│       ├── pom.xml
│       └── src/
│           ├── main/java/com/homeoffice/dummyservice/
│           │   ├── DummyServiceApplication.java
│           │   └── StatusController.java
│           ├── main/resources/
│           │   └── application.properties
│           └── test/java/com/homeoffice/dummyservice/
│               └── StatusControllerTest.java
│
├── CODEOWNERS
├── CODE_OF_CONDUCT.md
├── CONTRIBUTING.md
├── SECURITY.md
├── sonar-project.properties
└── README.md
```

---

## Quick Start – Consuming the Pipeline

Create `.github/workflows/ci.yml` in your service repository:

```yaml
name: CI

on:
  push:
    branches: [main, "feature/**"]
  pull_request:
    branches: [main]

jobs:
  pipeline:
    uses: UKHomeOffice/core-cloud-workflow-maven-docker-actions/.github/workflows/maven-docker-pipeline.yml@main
    with:
      image_name:        "my-service"
      java_version:      "17"
      publish:           ${{ github.ref == 'refs/heads/main' }}
    secrets:
      sonar_token:        ${{ secrets.SONAR_TOKEN }}
      sonar_host_url:     ${{ secrets.SONAR_HOST_URL }}
      AWS_ECR_ACCOUNT_ID: ${{ secrets.AWS_ECR_ACCOUNT_ID }}
      ROLE_TO_ASSUME:     ${{ secrets.ROLE_TO_ASSUME }}
```

---

## Inputs Reference

| Input | Type | Default | Description |
|---|---|---|---|
| `image_name` | string | **required** | Docker image name (without registry prefix) |
| `java_version` | string | `"17"` | JDK version |
| `java_distribution` | string | `"temurin"` | JDK distribution: `temurin` \| `corretto` \| `zulu` |
| `working_directory` | string | `"."` | Path to Maven project root |
| `maven_args` | string | `""` | Extra arguments appended to every `mvn` command |
| `dockerfile` | string | `"Dockerfile"` | Relative path to Dockerfile |
| `docker_context` | string | `"."` | Docker build context |
| `tag_latest` | boolean | `true` | Also tag the image as `:latest` on publish |
| `publish` | boolean | `false` | Push image to ECR (set `true` on main branch only) |

---

## Secrets Reference

| Secret | Required | Description |
|---|---|---|
| `sonar_token` | ✅ | SonarQube authentication token |
| `sonar_host_url` | ✅ | SonarQube server URL |
| `AWS_ECR_ACCOUNT_ID` | When `publish: true` | AWS account ID hosting the ECR registry |
| `ROLE_TO_ASSUME` | When `publish: true` | IAM role ARN for OIDC-based ECR authentication |
| `MAVEN_SETTINGS_XML` | ⬜ | Base64-encoded `settings.xml` for private Nexus/Artifactory |

---

## Outputs

| Output | Description |
|---|---|
| `image_tag` | The image tag pushed to ECR (only set when `publish: true`) |

---

## Dependencies

| Dependency | Purpose |
|---|---|
| [`core-cloud-workflow-docker-actions`](https://github.com/UKHomeOffice/core-cloud-workflow-docker-actions) (CCL-6431) | Docker setup, build, Trivy scan and ECR push |
| [`core-cloud-workflow-sonarqube-scan`](https://github.com/UKHomeOffice/core-cloud-workflow-sonarqube-scan) `@1.1.3` | SonarQube SAST analysis |

---

## Versioning

Pin consumers to a release tag in production:

```yaml
uses: UKHomeOffice/core-cloud-workflow-maven-docker-actions/.github/workflows/maven-docker-pipeline.yml@v1.0.0
```

Using `@main` is acceptable for development but may include breaking changes between releases.

---

*Confluence documentation: to be linked post-demo per Definition of Done*
*Jira: CCL-6306*

