# Polarion Semgrep rule pack

Custom Semgrep rules for Polarion Java extensions. They cover the gap no
upstream rule author fills: there is no `p/polarion` registry pack, and no
CodeQL pack models Polarion's permission API (`ISecurityService.checkPermission`,
`TransactionalExecutor`, `IRepositoryConnection`).

Generic Java and OWASP coverage is **not** this pack's job. CodeQL
(`security-extended`), SonarCloud, Renovate, and gitleaks already run on the
consumer repositories, and duplicating them only adds alerts.

## Layout

```
polarion-semgrep-rules/
├── SEMGREP_VERSION        # pinned semgrep version, single source of truth
├── rules/*.yaml           # one rule per file, id == filename
└── tests/
    ├── fixtures/          # <rule>.vuln.<ext> and <rule>.fixed.<ext> per rule
    └── test_rules.sh      # asserts each rule fires on vuln only
```

## Running the tests

```bash
SEMGREP="uvx semgrep==$(cat polarion-semgrep-rules/SEMGREP_VERSION)" \
  bash polarion-semgrep-rules/tests/test_rules.sh
```

`jq` is required. A rule missing either fixture fails the suite rather than
being skipped, and the number of rules tested is asserted against the number of
rule files, so a suite that checked nothing cannot report success.

## Rules

| Rule | Severity | Fires on |
|---|---|---|
| `polarion-rest-no-authz-check` | WARNING | A REST controller method that changes state without `@Secured` or a per-endpoint permission check. Suppressed for classes whose `@Path` starts with `/internal`. |
| `polarion-get-with-write-transaction` | ERROR | A `@GET` method that opens a write transaction. |
| `polarion-transaction-no-permission-check` | INFO | A write transaction opened without an explicit permission check. |
| `polarion-velocity-ssti` | ERROR | `VelocityEngine` constructed without `SecureUberspector`, which exposes Java reflection to template authors. |
| `polarion-xxe-unsafe-parser` | ERROR | `DocumentBuilderFactory` / `SAXParserFactory` / `XMLInputFactory` created without disabling external entities. |
| `polarion-hardcoded-creds-config` | ERROR | A non-placeholder credential in a `.properties` or `.xml` configuration file. |
| `polarion-workflow-function-no-authz` | WARNING | A workflow function `execute(...)` body that mutates state without consulting the invoking user. |
| `polarion-weasyprint-pre-68` | ERROR | The WeasyPrint sidecar pinned below 68 (CVE-2025-68616). |

## Baseline on the target corpus

Both measurements below run the Polarion rules only, with no registry packs.

The rules were tuned to a locked-in baseline of **13 findings** on 2026-05-06,
at the SHAs recorded then: api-extender 1, generic 2, pdf-exporter 0,
docx-exporter 0, diff-tool 10.

Re-measured against current `main` on 2026-08-19, semgrep 1.172.0:

| Target | `main` SHA | Findings |
|---|---|---|
| api-extender | 6d7f5a7 | 1 |
| generic | 2d48baa | 2 |
| pdf-exporter | 28efc27 | 0 |
| docx-exporter | 147532a | 0 |
| diff-tool | 77ae24c | 15 |

18 in total. Every one is `polarion-transaction-no-permission-check` at INFO;
diff-tool gained five as new transactions were added since May. The other seven
rules fire nowhere on the corpus and act as regression guards on new code. Treat
that as the point of the pack in CI today: it is not a backlog of findings to
clear.

## Known rule gaps

Each limitation below is a deliberate trade against false positives, and is
repeated in the header of the rule it applies to.

- **`polarion-rest-no-authz-check` — `/api/*` wrapper subclasses.** Semgrep is
  file-scoped, so a controller under `/api` that omits `@Secured` while
  inheriting its methods from an `/internal/*` parent class is not detected.
- **`polarion-rest-no-authz-check` — the `/internal` suppression is
  architectural.** Per the upstream `generic` README, `/internal/*` controllers
  are intentionally not `@Secured`: the container's
  `<security-constraint role-name="user">` plus Polarion FORM login provides
  authentication, and `SameSite=Lax` plus the POST/PUT/DELETE convention
  provides CSRF defense. Adding `@Secured` there breaks the UI flow.
- **`polarion-get-with-write-transaction` is the residual CSRF case.**
  `SameSite=Lax` does not block a cookie-bearing cross-origin `GET`, so a
  side-effecting `GET` on `/internal/*` remains reachable through an
  `<img src=…>`. This is why the rule is ERROR while its sibling is INFO.
- **`polarion-transaction-no-permission-check` is informational by design.**
  Polarion platform APIs (`IDataService`, `IRepositoryConnection`, IPObject
  mutators) self-check the active Subject and throw `PermissionDeniedException`.
  `PolarionService.callPrivileged` is misleadingly named: it runs its lambda as
  the request's actual user, not as an elevated subject. Extension-level
  `checkPermission` is therefore defense-in-depth. The rule still catches
  mutations made outside a platform API — raw JDBC, direct file IO, reflection.
- **`polarion-velocity-ssti` matches at method scope.** A `SecureUberspector`
  configured through class-scope constants is invisible to the method-level
  `pattern-not-regex`, so that shape produces a false positive.
- **`polarion-workflow-function-no-authz` cannot read `workflow.xml`.** Whether
  the transition itself is role-restricted is outside the rule's reach, so the
  finding is raised for human review rather than as a definite defect.

## Consuming the pack from CI

`.github/workflows/reusable-polarion-semgrep.yml` in this repository checks the
pack out alongside the target repository, runs it, and uploads SARIF to Code
Scanning. Consumers call the reusable workflow rather than vendoring the rules.

## Where rules are developed

Rules are authored and tuned against real repositories outside CI, where the
feedback loop is seconds rather than a workflow run, and land here once they
have both fixtures and a measured baseline. Edits made here must be mirrored
back to that development copy; there is no automatic synchronisation.
