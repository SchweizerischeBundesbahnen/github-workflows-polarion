# github-workflows-polarion

![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)

Reusable GitHub Actions workflows for the SBB Polarion team.

This repository contains **GitHub Actions workflows (reusable and caller/CI)**. It does not contain application code, libraries, or standalone tools. Other repositories in the organization call the reusable workflows via `workflow_call`.

## Overview

- Status: Active
- Primary maintainers: @SchweizerischeBundesbahnen/SBB-CLEW-POLARION-ADMINS
- Support channel: [GitHub Issues](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues)
- License: Apache-2.0

## Available Workflows

| Workflow | Description |
|----------|-------------|
| `reusable-actionlint.yml` | GitHub Actions workflow validation using actionlint |
| `reusable-claude-code-review.yml` | AI-powered code review using Claude Code |
| `reusable-add-issue-to-project.yml` | Automatically add new issues to a GitHub project board |
| `reusable-release-please.yml` | Automated releases and changelogs using release-please (Maven, Python, Docker, etc.) |

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

Each reusable workflow has a corresponding caller workflow (e.g. `claude-code-review.yml`) that demonstrates how this repository itself uses it.

## Security

SBB manages `SECURITY.md` at the organization level. Add repository-specific security notes only if the project needs additional instructions beyond the organization default.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for reporting bugs, proposing changes, and submitting pull requests.
