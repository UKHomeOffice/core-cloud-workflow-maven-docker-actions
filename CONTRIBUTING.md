# Contributing Guide

Contributing to `core-cloud-workflow-maven-docker-actions`

This repository is part of the UK Home Office Core Cloud Platform shared tooling ecosystem.
To maintain consistent security and operational standards, all contributions must follow the guidelines below.

## Branching

Use the naming convention `feature/CCL-XXXX-short-description` for all feature branches.

## Making Changes

1. Branch off `main`
2. Make your changes
3. Raise a Pull Request — all PRs require at least one review from `@UKHomeOffice/core-cloud-platform`
4. Ensure all pipeline stages pass on your branch before requesting review

## Testing

All changes must be validated against the dummy service in `tests/dummy-service/` before merging. Evidence of a passing pipeline run must be linked in the PR description.

## Versioning

This repo uses semver tags (`v1.0.0`). Consumers should pin to a tag rather than `@main` in production workloads. When introducing breaking input/output changes, bump the major version and update the README migration notes.

## Documentation

Update `README.md` if any inputs, outputs, or pipeline behaviour changes. Link the relevant Jira ticket in your commit messages.
