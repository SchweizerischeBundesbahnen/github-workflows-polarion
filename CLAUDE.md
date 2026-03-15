# CLAUDE.md

## Project Purpose

This repository contains **only reusable GitHub Actions workflows**. There is no application code, no build system, and no tests. All files of interest live under `.github/workflows/`.

## Repository Structure

- `.github/workflows/reusable-*.yml` — Reusable workflows called by other repositories via `workflow_call`
- `.github/workflows/*.yml` (non-reusable) — Caller workflows that this repo uses itself, plus CI workflows (actionlint, PR checks, labeler, stale)

## Conventions

- All GitHub Actions must be pinned to full commit SHAs with a version comment (e.g. `uses: actions/checkout@<sha> # v6.0.2`)
- Workflows must set `permissions: {}` at the top level and grant only required permissions at the job level
- Checkout steps must include `persist-credentials: false`
- Reusable workflows accept secrets via `workflow_call` — never hardcode secrets or tokens
- Use `${{ github.repository_owner }}` instead of hardcoding the org name to keep workflows portable
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/)

## Workflow Naming

- Reusable workflows: `reusable-<name>.yml` with trigger `workflow_call`
- Caller workflows: `<name>.yml` that reference the reusable version via `uses:`

## Pre-commit

This repo uses pre-commit hooks including `zizmor` (GitHub Actions security linter) and `actionlint` (workflow syntax checker). Commits that fail these checks should not be pushed.
