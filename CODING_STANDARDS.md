# Repository Coding Standards

This document defines the minimum engineering baseline for this repository. Add repository-specific rules when needed, but do not weaken the expectations defined here.

## General Principles

- Prefer simple, maintainable solutions over clever ones.
- Optimize for readability and safe change review.
- Keep public behavior explicit and documented.
- Avoid introducing dependencies without a clear maintenance benefit.

## Required for Every Change

- Tests must cover bug fixes and non-trivial features where automated testing is practical.
- User-visible behavior changes must be documented in the relevant README, docs, or changelog entry.
- Breaking changes must be called out explicitly in pull requests and release notes.
- Security-sensitive changes must include a short explanation of the threat model or risk being addressed.

## Code Style

- Follow the formatter, linter, and editor settings configured in the repository.
- Use descriptive names for modules, functions, variables, tests, and commits.
- Keep functions focused. If a function is doing multiple unrelated things, split it.
- Remove dead code rather than commenting it out.
- Keep comments rare and useful. Explain intent or non-obvious tradeoffs, not obvious syntax.

## Dependencies

- Prefer standard library or existing repository dependencies before adding new packages.
- New dependencies must be actively maintained, appropriately licensed, and justified in the pull request.
- Pin or constrain dependencies according to the ecosystem norms used by the repository.

## API and Compatibility

- Treat public interfaces as contracts.
- When changing a public API, document the migration path.
- Maintain backward compatibility unless there is an explicit decision to break it.

## Documentation

- Keep setup instructions accurate.
- Document required environment variables, configuration, and external services.
- Update architecture or design references when the implementation materially changes.

## Security

- Do not hardcode secrets, credentials, or tokens.
- Apply the principle of least privilege to workflows, bots, and integrations.
- Review third-party GitHub Actions and pin them to trusted versions or SHAs where organizational policy requires it.

## Repository-specific Additions

Add language-specific or tool-specific rules below this section when the repository is created.
