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

Re-measure the corpus with `POLARION_TARGETS_DIR=<dir> bash polarion-semgrep-rules/tests/measure_corpus.sh`, which needs local clones and so cannot run in CI.

Probe a rule against arbitrary code — a shape a review comment describes, a hardening spelling no
fixture pins yet — with `bash polarion-semgrep-rules/tests/probe_rule.sh <rule|rules-dir>
<target>…`. It answers what a rule does to one file, which is the question a rule change raises
before a fixture exists for it. It refuses to print a finding count when semgrep exited non-zero,
and prints the scanned-file count beside the finding count, because a target semgrep never read —
an extension that does not map to the rule's language, a path on the built-in `.semgrepignore` —
otherwise reports 0 findings and exit 0 just like a rule that matched nothing.

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
| `polarion-transaction-no-permission-check` | WARNING | A call that changes state outside a platform API (JDBC update, `java.io` / `java.nio.file` / commons-io write, `setAccessible(true)`) inside a write transaction, or in a same-file method the transaction calls, with no permission check before it. |
| `polarion-elevated-privileges` | WARNING | `doAsSystemUser(...)`, `getSystemUserSubject()`, `loginUserFromVault(...)`, `login(user, password, context)` or `loginWithToken(...)`: code that runs with, or logs in with, rights other than the request user's. |
| `polarion-velocity-ssti` | ERROR | `VelocityEngine` constructed without `SecureUberspector`, which exposes Java reflection to template authors. |
| `polarion-xxe-unsafe-parser` | ERROR | `DocumentBuilderFactory` / `SAXParserFactory` / `XMLInputFactory` created without `disallow-doctype-decl` or an emptied `ACCESS_EXTERNAL_DTD`. |
| `polarion-hardcoded-creds-config` | ERROR | A non-placeholder credential in a `.properties` or `.xml` configuration file. |
| `polarion-workflow-function-no-authz` | WARNING | A workflow function `execute(...)` that consults neither the invoking user nor a permission check. Matches whether or not the body mutates anything — see the gaps below. |
| `polarion-weasyprint-pre-68` | ERROR | The WeasyPrint sidecar pinned below 68 (CVE-2025-68616). |

## Baseline on the target corpus

The measurements below run the Polarion rules only, with no registry packs.

The rules were tuned to a locked-in baseline of **13 findings** on 2026-05-06,
at the SHAs recorded then: api-extender 1, generic 2, pdf-exporter 0,
docx-exporter 0, diff-tool 10.

Re-measured against current `main` on 2026-08-22, semgrep 1.172.0:

| Target | `main` SHA | Findings |
|---|---|---|
| api-extender | ede37c6 | 1 |
| generic | dbf2f6e | 2 |
| pdf-exporter | ce31ac1 | 0 |
| docx-exporter | f8d956f | 0 |
| diff-tool | 079d9f7 | 15 |

18 in total, at the rule set of that date. Every one was
`polarion-transaction-no-permission-check`, which was INFO then and fired on
every write transaction; diff-tool gained five as new transactions were added
since May. The other rules of that date fired nowhere on the corpus and acted as
regression guards on new code.

That paragraph is historical. The rule was narrowed in 2026-09 and is now
WARNING, `polarion-elevated-privileges` joined the pack, and nine rules ship.
The current numbers are in the 2026-09 table below.

The same five trees were measured with the rule pack as it stood before
`polarion-velocity-ssti` and `polarion-xxe-unsafe-parser` were rewritten to match
hardening structurally, and the result is identical target by target and rule by
rule. Widening those two rules to reach four more shapes introduced no finding on
any real target, which is the only evidence that matters for whether the widening
is safe to ship: their fixtures grew from 1 and 5 asserted cases to 5 and 9.

Re-measured against current `main` on 2026-09-14, semgrep 1.172.0, before and
after `polarion-transaction-no-permission-check` was narrowed to calls that
bypass the platform and `polarion-elevated-privileges` was added:

| Target | `main` SHA | Before | After |
|---|---|---|---|
| api-extender | aba70af | 1 | 0 |
| generic | 53d498c | 2 | 0 |
| pdf-exporter | 48f77f65 | 0 | 0 |
| docx-exporter | 3f1a2fb | 0 | 0 |
| diff-tool | 9df71f0 | 15 | 2 |

Before the change all 18 findings were `polarion-transaction-no-permission-check`
at INFO, one per write transaction, and none needed a code change: each one wrote
through a platform API that checks permissions itself. After it the narrowed rule
fires nowhere on the corpus. The 2 remaining findings are `polarion-elevated-privileges`
in diff-tool: `DocumentCopyService` sets a comment author as the system user, and
`ExecutionQueueSettings` reads global settings as the system user. Both are the
review the rule exists to prompt. The two changed rules were also run over the 17
other local `ch.sbb.polarion.extension.*` repositories with Java sources, with 0
findings. Both numbers held through the review rounds that followed: one-level
helpers, chained JDBC statements, guard polarity, helper arity, the widened write
sinks, the compound guard shapes and the login entry points.

Those later rounds were re-measured differently, on 2026-09-16 and semgrep
1.172.0, over all 43 local target repositories at once rather than the five
above. The number held at every step, before the round and after each change to
the guard clauses: after they were restricted to the disjunctive shape, and
after the two-sided form was added. Each run gave 12 findings with an identical
composition: diff-tool 2 `polarion-elevated-privileges`, fake-services 9
`polarion-rest-no-authz-check`, mailworkflow 1
`polarion-workflow-function-no-authz`. `polarion-transaction-no-permission-check`
reports nowhere on any of them.

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
  `<img src=…>`. This is why the rule is ERROR, while
  `polarion-transaction-no-permission-check` needs a bypass of the platform to
  fire and is WARNING.
- **`polarion-transaction-no-permission-check` reports the bypass, not the
  transaction.** Polarion platform APIs (`IDataService`, `IRepositoryConnection`,
  IPObject mutators) self-check the active Subject and throw
  `PermissionDeniedException`. `PolarionService.callPrivileged` is misleadingly
  named: it runs its lambda as the request's actual user, not as an elevated
  subject. So a write transaction that changes state through a platform API is
  already authorized, and the rule fires only on a call inside it that checks
  nothing: a JDBC update, a `java.io` / `java.nio.file` / commons-io write, or
  `setAccessible(true)`. Until 2026-09 it fired on every write transaction
  without an explicit check, at INFO, which on the corpus was 18 findings and no
  defect.

  Calls are followed one level into a method of the same file, spelled
  `helper(...)` or `this.helper(...)`, matched on name and arity for 0 to 3
  parameters, and the finding lands in the helper. A helper two levels down, a
  method on another object, a method reference and a helper with four or more
  parameters are not followed, because semgrep reads one file at a time and
  cannot wildcard an arity. A chained
  `connection.prepareStatement(sql).execute()` is reached through the
  `Connection` call that creates the statement. A bypass in a read-only
  transaction or outside any transaction is not reported: file IO outside a
  write transaction is routine on the corpus (export logs, temporary files).

  The permission check is a `checkPermission(...)` statement before the change,
  a positive `if (hasPermission(...))` around it, or a negated
  `if (!hasPermission(...))` before it that returns or throws
  `PermissionDeniedException`, `ForbiddenException`, `NotAuthorizedException` or
  `SecurityException`, spelled by simple name. The thrown type is named because a
  bare `throw` matched a rethrow in a `catch` nested in the guard body, which
  cleared a write made precisely when permission is denied. The check needs a
  receiver: `securityService.checkPermission(...)`, `this.`-qualified and
  statically imported spellings are recognized, because semgrep resolves all
  three. A negated condition may be a disjunction of any length, with the check
  in any position of the chain; a conjunction does not clear it, because on the
  other branch the check never runs. A positive condition is reached through
  one level of `&&`. Neither side uses a deep expression: inside a positive
  clause it matches the negated call, inside a negated one it accepts a
  conjunction, and both mistakes clear a write that nothing checked. What
  remains out of reach, each pinned in the vulnerable fixture: a check in the
  caller does not clear a finding in its helper; an extension helper with
  another name (`checkPermissions()`, `isModificationAllowed()`), a
  receiverless delegate and a guard throwing another type do not clear the
  rule; a helper of the same name and arity in a nested class is reported
  although the transaction cannot reach it; a write in the `else` of a positive
  guard, a write inside a terminating guard, and a guard that throws on one
  path only are cleared. Semgrep matches statements, not paths.
- **`polarion-elevated-privileges` reports the elevation, not its effect.**
  `doAsSystemUser(...)`, `getSystemUserSubject()`, `loginUserFromVault(...)`,
  `login(user, password, context)` and `loginWithToken(...)` are matched wherever
  they appear, whether the block reads or writes, because a read as the system
  user can expose data the caller may not see. `login(...)` is matched at its
  three-argument arity only: the no-argument `securityService.login()`
  re-authenticates the request user and elevates nothing, and it is the spelling
  on the corpus. A read of
  global configuration that every user may cause is a valid dismissal. The
  receiver is not typed, so the chained
  `lookupService(ISecurityService.class).doAsSystemUser(...)` is reached; a
  method with the same name on another type would be reported as well, and none
  exists on the corpus.
- **`polarion-velocity-ssti` suppression is class-scoped wherever the
  construction has no enclosing method.** The match is `new VelocityEngine(...)`
  itself, so all four shapes are reached — a bare statement, `return new
  VelocityEngine(...)`, a field initializer, and a construction inside a
  constructor — and the finding lands on the construction line rather than on the
  enclosing declaration. Where the construction sits inside a method the
  suppression is scoped to that method, so a hardened sibling does not clear an
  unhardened one; where it does not, the only scope available is the class, and
  any hardening anywhere in the class clears it. Accepted spellings of the
  hardening: the fully qualified class name as a string literal, directly or
  through a `static final String` constant that semgrep propagates, and
  `SecureUberspector.class`. Both spellings are accepted from a method, a
  constructor and a static initializer alike, and `class $CLASS { ... }` matches
  an `enum` declaration, so an enum singleton holding an engine is covered.
  Hardening expressed any other way is a false positive, and so is hardening
  placed outside the scope the branch applies to — which for a method-local
  construction means anywhere but that method. `new
  VelocityEngine(secureProperties())` in a builder method is therefore reported
  although the helper hardens, while the identical delegation in a field
  initializer is cleared by the class-scoped branch. That is the price of not
  letting a hardened method clear an unhardened sibling, and it predates this
  wording rather than being introduced by it. Both branches share the
  path-insensitivity recorded for `polarion-xxe-unsafe-parser` below: hardening
  behind an `if`, or written in a lambda that is never invoked, clears the rule
  although nothing is applied on the path that reaches the constructor.
- **`polarion-xxe-unsafe-parser` accepts exactly two hardening forms, on the
  receiver the finding is about.** `disallow-doctype-decl`, and
  `ACCESS_EXTERNAL_DTD` set to an empty string — on the factory through
  `setAttribute` before the parser is created, or on the parser through
  `setProperty` after it, which is the only SAX route to that property because
  `SAXParserFactory` has neither method. The two routes carry opposite order
  disciplines and both are enforced: FACTORY-level hardening applied once the
  builder or parser already exists does not clear the rule, because it cannot
  affect it, and neither does hardening a sibling factory in the same method.
  The parser-level route is ordered against the creation call only, so a
  `setProperty` placed after the `parse` call — where it is equally useless — is
  not reported. The positive pattern ends at `newSAXParser()` rather than at the
  parse, and that shape is contrived enough not to be worth extending it. The
  empty
  value is part of the check: `ACCESS_EXTERNAL_DTD, "file"` still resolves
  `file://` external entities, so matching the constant name alone would clear
  the rule on code that is still exposed. `ACCESS_EXTERNAL_SCHEMA` on its own
  does not clear it either, because restricting schema resolution addresses
  neither DOCTYPE processing nor external general entities. Nor does
  `FEATURE_SECURE_PROCESSING`: OWASP records that it "may not always mitigate
  entity expansion" and treats it as supplementary. Both negative cases are
  pinned in the vulnerable fixture, as are the sibling-factory and
  hardened-too-late shapes. On the StAX branch the boxed `Boolean.FALSE` is
  accepted alongside the literal `false`, because `setProperty` takes an
  `Object`; the string `"false"` is not, since the specification types these
  properties as Boolean and a value only some implementations coerce is not proof
  of hardening. Rejected earlier and worth not retrying: widening the SAX
  positive pattern past the factory call, which makes the region overlap itself
  and produces duplicate findings on unhardened code without clearing the
  hardened one — the parser-level clause exists because of that.

  The limitation is the scope: every clause requires the configuring call in the
  same METHOD as the creation, so hardening delegated to a helper method or
  performed in a constructor does not clear the rule. Block nesting inside that
  method is fine at any depth — the statement ellipsis descends into a `try`,
  `if` or loop body, which matters because the OWASP cheat sheet's own DOM
  example wraps `setFeature` in `try`/`catch`, and both depths are pinned in the
  fixed fixture. It descends into a lambda body as well. The one boundary it does
  not cross is an anonymous class's method body, so hardening performed there
  still reports, which is a false positive.

  That descent is unconditional, and it cuts the other way. Hardening behind an
  `if` clears the rule although the branch not taken reaches the parser
  unhardened; hardening in a lambda body clears it without the lambda ever being
  invoked, because no clause matches the call that would run it, so there the
  hardening need not be on any executed path at all. Semgrep matches statements,
  not paths, so both are limitations of the analysis rather than of these
  clauses. Both are real exploitable configurations the rule stays silent on, and
  the lambda is the worse of the two.
- **Both rules above match the configuring call structurally, not as text.** A
  `pattern-not-regex` is applied to the matched region as TEXT, which cost a
  false positive and a false negative at once and is worth stating explicitly
  because the failure is silent in both directions. Measured on semgrep 1.172.0:
  hardened SAX code was reported at ERROR because the matched region ended at
  `newSAXParser()` and the `parser.setProperty(...)` call sat past it; and a
  comment naming `SecureUberspector` or `disallow-doctype-decl` inside the region
  cleared the rule outright, which is how a genuinely vulnerable case in this
  pack's own fixture came to be recorded as a shape the pattern could not reach.
  Widening a region-scoped regex is not the fix — it moves the region — so both
  rules express hardening as a `pattern-not-inside` over the enclosing scope.
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
- **`polarion-workflow-function-no-authz` cannot read `workflow.xml`, and does
  not require a mutation.** Whether the transition itself is role-restricted is
  outside the rule's reach. The precisely-typed
  `execute(IArguments, IActionContext)` signature also matches a read-only body,
  which is intentional — the signature is strong evidence of a workflow function
  and a read-only body is one edit from a mutating one — so the finding is raised
  for human review rather than as a definite defect, and a genuinely read-only
  function is a valid dismissal.

## What semgrep skips before a rule runs

Semgrep applies a built-in `.semgrepignore` whose "common test paths" section covers `test/` and
`tests/`. Two consequences, both measured on 2026-08-19 with semgrep 1.172.0:

- **The baseline covers `src/main` only.** A consumer's `src/test/java` matches `test/` and is
  never scanned, so no finding in this pack's corpus numbers comes from test code.
- **A consumer's own `.semgrepignore` replaces that list rather than extending it.** Against a
  repository whose `.semgrepignore` held only `node_modules/`, the rule pack checkout produced
  **42 findings from these fixtures**, and 0 with the `--exclude` the reusable workflow passes. So
  that exclusion is load-bearing for exactly the consumers whose ignore file looks harmless, and
  it is not made redundant by the fixtures living under `tests/`.

## Consuming the pack from CI

`.github/workflows/reusable-polarion-semgrep.yml` in this repository checks the
pack out alongside the target repository, runs it, and uploads SARIF to Code
Scanning. Consumers call the reusable workflow rather than vendoring the rules.

The pack is checked out from the workflow's own commit (`job.workflow_repository`
and `job.workflow_sha`), so the ref in the caller's `uses:` line decides the rule
version as well and the two cannot drift apart. There is no input for it: a
caller-supplied ref would both allow that drift and let a caller aim the checkout
at an arbitrary ref.

## Where rules are developed

Rules are authored and tuned against real repositories outside CI, where the
feedback loop is seconds rather than a workflow run, and land here once they
have both fixtures and a measured baseline. Edits made here must be mirrored
back to that development copy; there is no automatic synchronisation.
