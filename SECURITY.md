# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in this repository, **do not** open a public GitHub issue.

Please report it directly to the Core Cloud Platform team via the UK Home Office internal security reporting process.

## Supported Versions

Only the latest release tag is actively maintained and patched.

## Security Scanning

This repository runs the following automated security checks on every pull request and push to `main`:

- **Gitleaks** – scans the full git history for accidentally committed secrets or credentials
- **Checkov** – static analysis of Dockerfiles and GitHub Actions workflows (SARIF uploaded to GitHub Advanced Security)
- **Trivy** – vulnerability scanning of built Docker images (via `core-cloud-workflow-docker-actions`)
- **SonarQube** – SAST code analysis
