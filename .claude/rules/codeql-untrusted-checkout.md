---
paths:
  - ".github/workflows/**"
---

- CodeQL's `actions/untrusted-checkout/medium` flags any `${{ }}` expression in a checkout's `ref:` or `repository:` under `workflow_call`, `job.workflow_*` included, and renaming an input does not clear it (variants measured in #126). On the `job.workflow_*` own-source checkout it is a false positive: request its dismissal. Where an `inputs.*` value reaches `ref:`, take that control away from the caller instead
