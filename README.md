# github-workflows-polarion

![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)

Reusable GitHub Actions workflows for the SBB Polarion team.

This repository contains **only reusable workflow definitions**. It does not contain application code, libraries, or standalone tools. Other repositories in the organization call these workflows via `workflow_call`.

## Overview

- Status: Active
- Primary maintainers: @SchweizerischeBundesbahnen/SBB-CLEW-POLARION-ADMINS
- Support channel: [GitHub Issues](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues)
- License: Apache-2.0

## Available Workflows

| Workflow | Description |
|----------|-------------|
| `reusable-claude-code-review.yml` | AI-powered code review using Claude Code |
| `reusable-add-issue-to-project.yml` | Automatically add new issues to a GitHub project board |

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

Each reusable workflow has a corresponding caller workflow (e.g. `claude-code-review.yml`) that demonstrates how this repository itself uses it.

## Security

SBB manages `SECURITY.md` at the organization level. Add repository-specific security notes only if the project needs additional instructions beyond the organization default.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for reporting bugs, proposing changes, and submitting pull requests.
