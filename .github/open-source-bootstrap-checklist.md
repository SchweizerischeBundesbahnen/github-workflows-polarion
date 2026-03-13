> **Not every item applies to every repository.** Required items must be completed before publishing. Recommended items should be completed unless there is a clear reason to skip them. Optional items can be removed or ignored if they do not fit the project. Delete checklist lines that do not apply so the issue reflects only the actual work remaining.

### Required

These must be completed before making the repository public.

- [ ] **This repository fulfils all requirements from the SBB Open Source Guidelines**
- [ ] **Replace all template tokens in the form `{{TOKEN_NAME}}`**
- [ ] **Add a README.md file containing the project overview, setup instructions, and support information**
- [ ] **Add a LICENSE.md file using an approved SBB open source license**
- [ ] **Add a CONTRIBUTING.md file**
- [ ] **Add a CODE_OF_CONDUCT.md file with a private maintainer contact**
- [x] **SECURITY.md is provided at the organisation level**
- [ ] **Delete the license_suggestions/ directory after choosing the license**

### Recommended

These are expected for most repositories. Skip only with a documented reason.

- [ ] **Add a CODING_STANDARDS.md file** — remove if the project has no code contributions
- [ ] **Set up CI for the basic structure and review the security of GitHub Actions: https://docs.github.com/en/actions/security-for-github-actions**
- [ ] **Set up Renovate or Dependabot for the repository**
- [ ] **Review /.github/CODEOWNERS and replace the owning team or remove the file if not used**

### Optional

Keep, customize, or remove based on your project's needs.

- [ ] Review CHANGELOG.md and keep it if the repository should maintain structured release history; otherwise delete it
- [ ] Add or customize RELEASE.md for the release procedure; remove if the project has no formal release process
- [ ] Review and customize SUPPORT.md; remove if support information in the README is sufficient
- [ ] If using Apache 2.0, update the NOTICE file with project name and year; otherwise remove it
- [ ] Review /.github/ISSUE_TEMPLATE/ and keep or adjust the issue forms; remove if not needed
- [ ] Review /.github/pull_request_template.md; remove if not needed
- [ ] Review the stale workflow and labeler workflow; adjust timeouts and labels or remove if not needed

***--- Tasks after the repository is set to PUBLIC ---***

### Required after publishing

- [ ] **Code Security and analysis must be configured in your repository under Settings > Code security and analysis**
- [ ] **Set "Require approval for all outside collaborators" for PR from forks in your repository under Settings > Actions > General**

### Optional after publishing

- [ ] Add the project to SonarCloud under https://sonarcloud.io/organizations/schweizerischebundesbahnen (note that public repository vulnerabilities will be visible to everyone)
