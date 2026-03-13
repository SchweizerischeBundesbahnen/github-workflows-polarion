# {{PROJECT_NAME}}

> Replace every `{{TOKEN_NAME}}` placeholder before making a repository public. Run the validation workflow manually from the Actions tab to verify customization is complete.

<!-- Uncomment and adjust the badges below after setting up CI and choosing a license.
![CI](https://github.com/SchweizerischeBundesbahnen/{{REPO_NAME}}/actions/workflows/ci.yml/badge.svg)
![License](https://img.shields.io/badge/license-{{LICENSE_NAME}}-blue.svg)
-->

{{ONE_LINE_DESCRIPTION}}

## Maintainer Quickstart

Start with [docs/MAINTAINER_QUICKSTART.md](docs/MAINTAINER_QUICKSTART.md). It covers the first actions after creating a repository from this template, including license selection, token replacement, GitHub setup review, manual checklist creation, and publication readiness.

## Overview

- Status: {{PROJECT_STATUS}}
- Primary maintainers: {{MAINTAINER_TEAM_OR_NAME}}
- Support channel: {{SUPPORT_CHANNEL}}
- License: {{LICENSE_NAME}}

## Getting Started

### Prerequisites

Document the required tools, runtimes, and accounts needed to work with the project.

### Installation

Replace this section with the commands or steps needed to install dependencies and prepare a local environment.

### Usage

Describe the most common workflows for users and contributors. Include links to additional documentation if the project is larger than a single README.

## Repository Bootstrap

> **Remove this section and "Included Files" below after the repository is public and stable.** See [Post-release cleanup](docs/MAINTAINER_QUICKSTART.md#8-post-release-cleanup) for the full list of bootstrapping artifacts to delete.

1. Choose the approved license text from [license_suggestions](license_suggestions) and copy it to LICENSE.md.
2. Replace all template tokens in the repository.
3. Review [CONTRIBUTING.md](CONTRIBUTING.md), [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md), [CODING_STANDARDS.md](CODING_STANDARDS.md), and [RELEASE.md](RELEASE.md).
4. Review the GitHub scaffolding in [.github](.github), especially CODEOWNERS, issue forms, the PR template, Dependabot, and workflows.
5. The readiness checklist issue is created automatically on the first push to `main`. If not, run [create-initial-open-source-checklist.yml](.github/workflows/create-initial-open-source-checklist.yml) manually from the Actions tab.

## Included Files

Not every file is needed in every repository. Required files must be present before publishing. Optional files can be deleted if they do not apply to the project.

- [CONTRIBUTING.md](CONTRIBUTING.md): contribution expectations and review process *(required)*
- [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md): community behavior and enforcement path *(required)*
- [CODING_STANDARDS.md](CODING_STANDARDS.md): engineering baseline for contributions *(recommended)*
- [CHANGELOG.md](CHANGELOG.md): optional structured release history
- [RELEASE.md](RELEASE.md): release procedure template *(optional)*
- [SUPPORT.md](SUPPORT.md): support channels and how to get help *(optional)*
- [NOTICE](NOTICE): attribution notice *(required for Apache 2.0; remove if using a different license)*
- [.github/open-source-bootstrap-checklist.md](.github/open-source-bootstrap-checklist.md): canonical readiness checklist used by the workflow

## Security

SBB manages `SECURITY.md` at the organization level. Add repository-specific security notes only if the project needs additional instructions beyond the organization default.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for reporting bugs, proposing changes, and submitting pull requests.
