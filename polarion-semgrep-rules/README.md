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
├── rules/*.yaml           # one rule per file, id == filename (.yml also loaded)
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
rule files, so a suite that checked nothing cannot report success. The
vulnerable fixture's `ruleid:` annotations are the expected finding count, so a
rule that regresses from matching five cases to matching one fails rather than
passing on a bare `> 0`. A case the rule is known not to reach carries
`known-miss:` instead, and is not counted.

## Rules

| Rule | Severity | Fires on |
|---|---|---|
| `polarion-rest-no-authz-check` | WARNING | A REST controller method that changes state without `@Secured` or a per-endpoint permission check. Suppressed for classes whose `@Path` starts with `/internal`. |
| `polarion-get-with-write-transaction` | ERROR | A `@GET` method that opens a write transaction. |
| `polarion-transaction-no-permission-check` | INFO | A write transaction opened without an explicit permission check. |
| `polarion-velocity-ssti` | ERROR | `VelocityEngine` constructed without `SecureUberspector`, which exposes Java reflection to template authors. |
| `polarion-xxe-unsafe-parser` | ERROR | `DocumentBuilderFactory` / `SAXParserFactory` / `XMLInputFactory` created without `disallow-doctype-decl` or an emptied `ACCESS_EXTERNAL_DTD`. |
| `polarion-hardcoded-creds-config` | ERROR | A non-placeholder credential in a `.properties` or `.xml` configuration file. |
| `polarion-workflow-function-no-authz` | WARNING | A workflow function `execute(...)` that consults neither the invoking user nor a permission check. Matches whether or not the body mutates anything — see the gaps below. |
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
- **`polarion-velocity-ssti` matches at method scope, in both directions.** A
  `SecureUberspector` configured through class-scope constants is invisible to
  the method-level `pattern-not-regex`, so that shape produces a false positive.
  In the other direction the rule requires `new VelocityEngine(...)` as a bare
  statement inside a method declaration, so three shapes are missed silently: a
  field initializer (`private static final VelocityEngine ENGINE = new
  VelocityEngine();`), a construction inside a constructor, which has no return
  type for the pattern to bind, and `return new VelocityEngine(props);`. The
  last of these is annotated `known-miss:` in the vulnerable fixture. Widening
  the pattern is not a one-line change — adding a `pattern-regex` narrows the
  match region so the `SecureUberspector` suppression stops working — so it is
  tracked separately rather than bundled here.
- **`polarion-xxe-unsafe-parser` accepts exactly two hardening forms.**
  `disallow-doctype-decl`, and `ACCESS_EXTERNAL_DTD` set to an empty string. The
  empty value is part of the check: `ACCESS_EXTERNAL_DTD, "file"` still resolves
  `file://` external entities, so matching the constant name alone would clear
  the rule on code that is still exposed. `ACCESS_EXTERNAL_SCHEMA` on its own
  does not clear it either, because restricting schema resolution addresses
  neither DOCTYPE processing nor external general entities. Nor does
  `FEATURE_SECURE_PROCESSING`: OWASP records that it "may not always mitigate
  entity expansion" and treats it as supplementary. Both negative cases are
  pinned in the vulnerable fixture.
- **`polarion-weasyprint-pre-68` cannot match a specifier split across
  lines.** Two forms are out of reach, both because TOML spreads them over more
  than one line and every alternative is single-line: a `poetry.lock` entry
  (`name = "weasyprint"` then `version = "67.0"`), and the dotted-section form
  (`[tool.poetry.dependencies.weasyprint]` then `version = "^67.0"`).
  `*poetry.lock` stays in `paths.include` so a future cross-line alternative
  needs no change there, but today that entry scans nothing.

  What is covered: the specifier string (`weasyprint==67`, `>=66,<68`, any
  casing), the assignment form (`weasyprint = "==67.0"`) and the inline-table
  form (`weasyprint = {version = "^67.0", extras = [...]}`) — the last two being
  the syntaxes Pipfile and the poetry dependency table use.
- **The `ACCESS_EXTERNAL_DTD` form clears DOM only, not SAX.**
  `SAXParserFactory` has no `setAttribute`, so the SAX route to that hardening is
  `parser.setProperty(...)` after `newSAXParser()`, which falls outside the
  matched region — SAX code hardened that way is reported at ERROR. Widening the
  SAX pattern past the factory call was tried and made it worse: the region
  overlaps itself, producing duplicate findings on unhardened code without
  clearing the hardened case. `disallow-doctype-decl` is unaffected on both
  branches, because `factory.setFeature(...)` precedes `newSAXParser()`.
- **`polarion-workflow-function-no-authz` cannot read `workflow.xml`, and does
  not require a mutation.** Whether the transition itself is role-restricted is
  outside the rule's reach. The precisely-typed
  `execute(IArguments, IActionContext)` signature also matches a read-only body,
  which is intentional — the signature is strong evidence of a workflow function
  and a read-only body is one edit from a mutating one — so the finding is raised
  for human review rather than as a definite defect, and a genuinely read-only
  function is a valid dismissal.

## Consuming the pack from CI

`.github/workflows/reusable-polarion-semgrep.yml` in this repository checks the
pack out alongside the target repository, runs it, and uploads SARIF to Code
Scanning. Consumers call the reusable workflow rather than vendoring the rules.

## Where rules are developed

Rules are authored and tuned against real repositories outside CI, where the
feedback loop is seconds rather than a workflow run, and land here once they
have both fixtures and a measured baseline. Edits made here must be mirrored
back to that development copy; there is no automatic synchronisation.
