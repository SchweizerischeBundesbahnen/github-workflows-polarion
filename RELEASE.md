# Release Process

This file describes how this repository should publish releases.

## Goals

- produce reproducible releases
- keep release notes understandable for users
- ensure tags and published artifacts match the reviewed source

## Recommended baseline

1. Merge changes through pull requests with passing checks.
2. Use Conventional Commits or an equivalent convention for release notes.
3. Update or review the changelog before each release.
4. Create a version tag from the default branch.
5. Publish release notes that summarize user-visible changes, breaking changes, and migration steps.
6. If artifacts are published, ensure the build is reproducible from the tagged commit.

## Repository-specific details to define

- release cadence
- versioning strategy
- artifact publication target
- rollback procedure
- approval requirements

Replace this section with repository-specific release instructions once the project is created.
