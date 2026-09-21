# github-workflows-polarion

![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)

Reusable GitHub Actions workflows for the SBB Polarion team.

This repository contains **GitHub Actions workflows (reusable and caller/CI)** and the Polarion Semgrep rule pack that one of them runs. It does not contain application code or libraries. Other repositories in the organization call the reusable workflows via `workflow_call`.

## Overview

- Status: Active
- Primary maintainers: @SchweizerischeBundesbahnen/SBB-CLEW-POLARION-ADMINS
- Support channel: [GitHub Issues](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues)
- License: Apache-2.0

## Available Workflows

| Workflow | Description |
|----------|-------------|
| `reusable-actionlint.yml` | GitHub Actions workflow validation using actionlint |
| `reusable-claude.yml` | On-demand Claude Code assistant triggered by `@claude` mentions in issues, PRs, and reviews |
| `reusable-claude-code-review.yml` | AI-powered code review using Claude Code |
| `reusable-add-issue-to-project.yml` | Automatically add new issues to a GitHub project board |
| `reusable-pr.yml` | PR checks with conventional commit validation |
| `reusable-openapi-validation.yml` | OpenAPI spec validation using Redocly CLI |
| `reusable-release-please.yml` | Automated releases and changelogs using release-please (Maven, Python, Docker, etc.) |
| `reusable-release-please-guard.yml` | Blocks PR merges when the base branch `pom.xml` version is not a SNAPSHOT (prevents post-release drift) |
| `reusable-codeql-java.yml` | CodeQL analysis for Java repositories, using `build-mode: manual` |
| `reusable-codeql-javascript.yml` | CodeQL analysis for JavaScript/TypeScript and GitHub Actions workflows, using `build-mode: none` |
| `reusable-polarion-semgrep.yml` | Polarion-specific Semgrep rules, uploaded to Code Scanning ([rule pack](polarion-semgrep-rules/README.md)) |

## Usage

Call a reusable workflow from your repository:

```yaml
jobs:
  claude-review:
    uses: SchweizerischeBundesbahnen/github-workflows-polarion/.github/workflows/reusable-claude-code-review.yml@main
    secrets:
      CLAUDE_CODE_OAUTH_TOKEN: ${{ secrets.CLAUDE_CODE_OAUTH_TOKEN }}
```

```yaml
# @claude mention handler — triggered from issues, PR comments, and reviews
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

```yaml
jobs:
  add-to-project:
    uses: SchweizerischeBundesbahnen/github-workflows-polarion/.github/workflows/reusable-add-issue-to-project.yml@main
    secrets:
      POLARION_GITHUB_APP_ID: ${{ secrets.POLARION_GITHUB_APP_ID }}
      POLARION_GITHUB_APP_PRIVATE_KEY: ${{ secrets.POLARION_GITHUB_APP_PRIVATE_KEY }}
    with:
      project-number: ${{ vars.POLARION_GITHUB_PROJECT_NUMBER }}
```

```yaml
# Maven project
jobs:
  release-please:
    uses: SchweizerischeBundesbahnen/github-workflows-polarion/.github/workflows/reusable-release-please.yml@main
    permissions:
      contents: write
      pull-requests: write
    with:
      release-type: maven
```

```yaml
# Python project
jobs:
  release-please:
    uses: SchweizerischeBundesbahnen/github-workflows-polarion/.github/workflows/reusable-release-please.yml@main
    permissions:
      contents: write
      pull-requests: write
    with:
      release-type: python
      include-v-in-tag: false
```

```yaml
# Docker / simple project
jobs:
  release-please:
    uses: SchweizerischeBundesbahnen/github-workflows-polarion/.github/workflows/reusable-release-please.yml@main
    permissions:
      contents: write
      pull-requests: write
    with:
      release-type: simple
      include-v-in-tag: false
```

```yaml
# CodeQL — one caller workflow, one job per analysed language. A repository with
# both Java and a JavaScript UI needs both jobs: each reusable workflow covers
# only the languages named in its file, and a language nobody analyses is simply
# never scanned.
#
# Disable CodeQL default setup in the repository before adding this workflow.
# The two cannot coexist: while default setup is enabled every advanced analysis
# is rejected at upload with "CodeQL analyses from advanced configurations
# cannot be processed when the default setup is enabled", after the scan has
# already run. Conversely, enabling advanced setup disables default setup, and
# any language the advanced workflow does not name then stops being scanned
# without further notice — which is the gap these two workflows together close.
on:
  push:
    branches: [main, release-v*]
  pull_request:
    branches: [main, release-v*]
    types: [opened, synchronize, reopened, ready_for_review]
  schedule:
    - cron: 17 5 * * 0
permissions: {}
jobs:
  analyze:
    uses: SchweizerischeBundesbahnen/github-workflows-polarion/.github/workflows/reusable-codeql-java.yml@main
    permissions:
      contents: read
      security-events: write
      actions: read
      packages: read
    secrets:
      IO_JFROG_SBB_POLARION_TOKEN: ${{ secrets.IO_JFROG_SBB_POLARION_TOKEN }}
  analyze-javascript:
    uses: SchweizerischeBundesbahnen/github-workflows-polarion/.github/workflows/reusable-codeql-javascript.yml@main
    permissions:
      contents: read
      security-events: write
      actions: read
    # `languages` defaults to '["javascript-typescript", "actions"]'. A
    # repository with no JavaScript or TypeScript source must narrow it, because
    # CodeQL fails an analysis that finds no source file of a language it was
    # asked for:
    #   with:
    #     languages: '["actions"]'
```

```yaml
# Maven release-please guard — blocks merges when base branch pom.xml is not a SNAPSHOT
# Requires branch protection: add "release-please-guard" as required status check
# and enable "Require branches to be up to date before merging"
on:
  pull_request:
    types: [opened, synchronize, reopened]
permissions: {}
jobs:
  release-please-guard:
    uses: SchweizerischeBundesbahnen/github-workflows-polarion/.github/workflows/reusable-release-please-guard.yml@main
    permissions:
      contents: read
```

Each reusable workflow has a corresponding caller workflow (e.g. `claude-code-review.yml`) that demonstrates how this repository itself uses it.

## Security

SBB manages `SECURITY.md` at the organization level. Add repository-specific security notes only if the project needs additional instructions beyond the organization default.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for reporting bugs, proposing changes, and submitting pull requests.
