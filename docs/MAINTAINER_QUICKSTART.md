# Maintainer Quickstart

Use this checklist in the first 10 minutes after creating a new repository from this template.

## 1. Choose the license

1. Review the approved license texts in [license_suggestions](../license_suggestions).
2. Copy the chosen text to `LICENSE.md`.
3. If using Apache 2.0, update [NOTICE](../NOTICE) with the project name and year. If not using Apache 2.0, delete the `NOTICE` file.
4. Delete the `license_suggestions/` directory after making your choice.

## 2. Replace template tokens

Replace every placeholder in the form `{{TOKEN_NAME}}`. Run `grep -rn '{{' .` to find all occurrences.

| Token | Where used | Description |
|---|---|---|
| `{{PROJECT_NAME}}` | README, CONTRIBUTING | Project name |
| `{{ONE_LINE_DESCRIPTION}}` | README | Short project description |
| `{{PROJECT_STATUS}}` | README | e.g., "Active", "Experimental" |
| `{{MAINTAINER_TEAM_OR_NAME}}` | README | Primary maintainers |
| `{{SUPPORT_CHANNEL}}` | README, issue template config | Support channel — use a URL or `mailto:` URI for the issue template |
| `{{LICENSE_NAME}}` | README | SPDX license identifier or short name |
| `{{MAINTAINER_PRIVATE_CONTACT}}` | CODE_OF_CONDUCT | Enforcement contact (email or private channel) |
| `{{REPO_NAME}}` | README (badge, commented out) | GitHub repository name (without org prefix) |
| `{{YEAR}}` | NOTICE | Copyright year (only needed for Apache 2.0) |
| `{{OWNING_TEAM_SLUG}}` | .github/CODEOWNERS | GitHub team slug for code ownership |

## 3. Review the baseline documents

Confirm that these files match the project you are publishing:

- [README.md](../README.md) — required
- [CONTRIBUTING.md](../CONTRIBUTING.md) — required
- [CODE_OF_CONDUCT.md](../CODE_OF_CONDUCT.md) — required
- [CODING_STANDARDS.md](../CODING_STANDARDS.md) — recommended; remove if the project has no code contributions
- [RELEASE.md](../RELEASE.md) — optional; remove if there is no formal release process
- [SUPPORT.md](../SUPPORT.md) — optional; remove if the README covers support information

Keep them short, accurate, and project-specific. Delete any optional file that does not apply rather than leaving template boilerplate in the repository.

## 4. Review GitHub scaffolding

The template includes several GitHub configuration files. Keep what fits your project and delete the rest — unused scaffolding adds confusion for contributors.

- [CODEOWNERS](../.github/CODEOWNERS) — recommended
  > **Note:** If `{{OWNING_TEAM_SLUG}}` is not replaced or the team slug is invalid, CODEOWNERS will silently fail to assign reviewers. GitHub does not report this error.
- [bug report form](../.github/ISSUE_TEMPLATE/bug_report.yml) — optional
- [feature request form](../.github/ISSUE_TEMPLATE/feature_request.yml) — optional
- [pull request template](../.github/pull_request_template.md) — optional
- [Dependabot config](../.github/dependabot.yml) — recommended
- [labeler config](../.github/labeler.yml) — optional
- [stale workflow](../.github/workflows/stale.yml) — optional

## 5. Open the repository readiness checklist

Manually run [create-initial-open-source-checklist.yml](../.github/workflows/create-initial-open-source-checklist.yml) from the Actions tab.

That creates the issue based on [open-source-bootstrap-checklist.md](../.github/open-source-bootstrap-checklist.md), which should track the remaining repository setup work.

## 6. Enable the required repo settings

Before making the repository public, review:

- branch protection and required checks
- code security and analysis settings
- fork pull request approval settings
- dependency update tooling
- whether SonarCloud is needed

Use the generated checklist issue to track these actions.

## 7. Validate before publishing

The workflow [validate-template-customization.yml](../.github/workflows/validate-template-customization.yml) is intended to fail until placeholders are removed and required files exist.

Make the repository green before announcing or publishing it.

## 8. Post-release cleanup

After the repository is public and stable, remove the bootstrapping artifacts that are no longer needed:

- `.github/workflows/validate-template-customization.yml` — template validation workflow
- `.github/workflows/create-initial-open-source-checklist.yml` — checklist issue creation workflow
- `.github/open-source-bootstrap-checklist.md` — checklist source file
- `docs/MAINTAINER_QUICKSTART.md` — this file
- The "Repository Bootstrap" and "Included Files" sections from `README.md`
- Check if NOTICE file is required: If this repository does not use the Apache 2.0 license, this file can be removed.

These files exist only to guide initial setup. Keeping them after publication adds confusion for contributors.
