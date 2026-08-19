# CLAUDE.md

## Project Purpose

This repository contains **GitHub Actions workflows (reusable and caller/CI)** and the Polarion Semgrep rule pack that one of them runs. There is no application code and no build system.

## Repository Structure

- `.github/workflows/reusable-*.yml` — Reusable workflows called by other repositories via `workflow_call`
- `.github/workflows/*.yml` (non-reusable) — Caller workflows that this repo uses itself, plus CI workflows (actionlint, PR checks, labeler, stale)

## Conventions

- All GitHub Actions must be pinned to full commit SHAs with a version comment (e.g. `uses: actions/checkout@<sha> # v6.0.2`)
- Workflows must set `permissions: {}` at the top level and grant only required permissions at the job level
- Checkout steps must include `persist-credentials: false` — exceptions:
  - `reusable-claude-code-review.yml` — `claude-code-action` needs git credentials for PR branch fetch
  - `reusable-claude.yml` — `claude-code-action` needs git credentials to operate on the PR branch
  - `reusable-actionlint.yml` — reviewdog falls back to `git fetch` for diff fetching on private repos
- Reusable workflows accept secrets via `workflow_call` — never hardcode secrets or tokens
- Use `${{ github.repository_owner }}` instead of hardcoding the org name to keep workflows portable
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/)
- `polarion-semgrep-rules/tests/fixtures/` holds deliberately vulnerable Java and configuration files. A workflow that scans a consumer repository must exclude the rule pack checkout path, or the fixtures are reported as findings against that repository
- Verify a rule-pack change with `SEMGREP="uvx semgrep==$(cat polarion-semgrep-rules/SEMGREP_VERSION)" bash polarion-semgrep-rules/tests/test_rules.sh` — without the `SEMGREP` override the suite runs whatever semgrep is on PATH, which is not the version the rule baseline was measured on

## Workflow Naming

- Reusable workflows: `reusable-<name>.yml` with trigger `workflow_call`
- Caller workflows: `<name>.yml` that reference the reusable version via `uses:`

## Review Architecture

- Claude Code Review (`reusable-claude-code-review.yml`) runs on `pull_request` events — reviews code and triages previous review threads (from Claude, bot reviewers, and human reviewers)
- Claude Code (`reusable-claude.yml`) runs on `issue_comment`, `pull_request_review_comment`, `pull_request_review`, and `issues` events — invokes `claude-code-action` when a human mentions `@claude` (all bot actors excluded via `sender.type != 'Bot'`)

### Known Limitation: `pull_request_review` trigger and OAuth

`claude-code-action` with `claude_code_oauth_token` (OIDC) requires the triggering actor to have write access to the repo. Bot-triggered `pull_request_review` events (e.g. from `copilot-pull-request-reviewer[bot]`) fail with 401 because the bot actor lacks write access. This means a separate triage workflow triggered by `pull_request_review` is **not viable** with OAuth auth. Bot reviewer triage must stay within the main review prompt (triggered by `pull_request` where the actor is the PR author).

### Known Limitation: a PR editing a caller workflow gets no Claude review

`claude-code-action` requires the **triggering caller** workflow file to be identical to its version on the default branch. Where it differs, the OIDC token exchange returns a workflow-validation error, the action logs `Workflow validation failed`, throws `WorkflowValidationSkipError` — which its retry wrapper explicitly excludes from retry — and the step still exits 0. The job is green, no review is posted, and nothing on the pull request surface says so.

The guard covers the caller only, not the reusable workflow it calls: PR #88 changed `reusable-claude-code-review.yml` alone and received a full review, while PR #91 also changed `claude-code-review.yml` and received none (measured 2026-08-19 on run `32273783450`).

So a change to a caller workflow cannot be reviewed by Claude on its own branch and only takes effect once merged. Never read a green `claude-review` check on such a pull request as a clean review.

## Pre-commit

This repo uses pre-commit hooks including `zizmor` (GitHub Actions security linter) and `actionlint` (workflow syntax checker). Commits that fail these checks should not be pushed.
