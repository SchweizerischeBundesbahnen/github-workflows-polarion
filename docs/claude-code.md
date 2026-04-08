# Claude Code Workflow

On-demand Claude Code assistant triggered by `@claude` mentions, implemented as a reusable workflow.

## Architecture

`reusable-claude.yml` wraps [`anthropics/claude-code-action`](https://github.com/anthropics/claude-code-action). Any repo in the org can call it via a thin caller workflow. This repo dogfoods it via `claude.yml`.

```
calling-repo/.github/workflows/claude.yml
  └─ uses: SchweizerischeBundesbahnen/github-workflows-polarion/.github/workflows/reusable-claude.yml@main
```

## Trigger Conditions

The caller subscribes to four events and the reusable job filters them:

| Event | When it fires |
|-------|---------------|
| `issue_comment` (created) | Someone comments on an issue or PR |
| `pull_request_review_comment` (created) | Someone comments on a PR diff line |
| `pull_request_review` (submitted) | Someone submits a PR review |
| `issues` (opened, assigned) | An issue is opened or assigned |

A run is only dispatched when **all** of the following hold:

- The triggering actor is not a bot (`sender.type != 'Bot'`)
- The event payload contains the string `@claude` in the relevant field (comment body, review body, issue body, or issue title)

## Permissions

The caller must grant the following at the job level:

| Permission | Level | Purpose |
|------------|-------|---------|
| `contents` | read | Read repository files |
| `pull-requests` | read | Read PR metadata |
| `issues` | read | Read issue metadata |
| `id-token` | write | OIDC authentication for `claude-code-action` |
| `actions` | read | Let Claude read CI results on PRs |

The reusable workflow itself declares the same permissions at the job level. Top-level permissions are `{}`.

## Usage

```yaml
name: Claude Code
on:
  issue_comment:
    types: [created]
  pull_request_review_comment:
    types: [created]
  issues:
    types: [opened, assigned]
  pull_request_review:
    types: [submitted]
permissions: {}
jobs:
  claude:
    uses: SchweizerischeBundesbahnen/github-workflows-polarion/.github/workflows/reusable-claude.yml@main
    permissions:
      contents: read
      pull-requests: read
      issues: read
      id-token: write
      actions: read
    secrets:
      CLAUDE_CODE_OAUTH_TOKEN: ${{ secrets.CLAUDE_CODE_OAUTH_TOKEN }}
```

## Configuration

- **Reusable workflow**: `reusable-claude.yml`
- **Secret required**: `CLAUDE_CODE_OAUTH_TOKEN`
- **Timeout**: 30 minutes per run
- **Checkout depth**: `fetch-depth: 1` (shallow; the action fetches more if it needs to)

## Relationship to `reusable-claude-code-review.yml`

The two workflows serve different purposes and should not be confused:

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| `reusable-claude-code-review.yml` | `pull_request` events | Automated code review on every PR push |
| `reusable-claude.yml` | `@claude` mentions in issues, comments, and reviews | On-demand assistant for ad-hoc tasks |

Both use `anthropics/claude-code-action` and the same `CLAUDE_CODE_OAUTH_TOKEN` secret, but they run independently.
