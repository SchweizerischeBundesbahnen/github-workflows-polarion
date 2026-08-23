#!/usr/bin/env bash
# tests/probe_rule.sh — run one rule (or the whole pack) against arbitrary files
# and print the finding count and line numbers per file.
#
# test_rules.sh answers "do the rules still match their fixtures". This answers
# "what does this rule do to THIS code", which is the question every rule change
# raises before a fixture exists for it: the shape a review comment describes, a
# hardening spelling nobody has pinned, a suspected false positive.
#
# It exists because semgrep's own failure mode is silence, and the silence has
# two doors. An invalid rule file makes semgrep load no rule at all from a
# --config directory and exit 7 with both streams empty under --quiet. A target
# semgrep never scans — an extension that does not map to the rule's language, a
# path on the built-in .semgrepignore — reports no finding and exits 0. Either
# way a hand-rolled probe prints 0 for code no rule was run against, which is
# indistinguishable from a rule that ran and matched nothing. This checks the
# status, surfaces .errors[], reports how many files were actually scanned, and
# pins the semgrep version the rule baseline was measured on.
#
# --no-rewrite-rule-ids keeps the printed check_id equal to the rule file's own
# id: semgrep otherwise prefixes it with the --config path, so the ids printed
# here would match neither the rule files, nor the ruleid: annotations in
# tests/fixtures/, nor measure_corpus.sh's per-rule breakdown.
#
# Usage:
#   tests/probe_rule.sh <rule.yaml|rules-dir> <target>...
#
# Override the semgrep invocation via SEMGREP, as the sibling scripts do.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACK_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
VERSION="$(tr -d '[:space:]' < "${PACK_DIR}/SEMGREP_VERSION")"

read -r -a SEMGREP_CMD <<< "${SEMGREP:-uvx semgrep==${VERSION}}"

if ! command -v jq >/dev/null 2>&1; then
    echo "ERROR: jq not installed." >&2
    exit 1
fi

if [[ $# -lt 2 ]]; then
    echo "usage: $(basename "$0") <rule.yaml|rules-dir> <target>..." >&2
    exit 2
fi

rule="$1"
shift

if [[ ! -e "${rule}" ]]; then
    echo "ERROR: ${rule} does not exist." >&2
    exit 1
fi

status=0
for target in "$@"; do
    if [[ ! -e "${target}" ]]; then
        echo "FAIL: ${target} does not exist" >&2
        status=1
        continue
    fi

    printf '=== %s ===\n' "${target}"

    set +e
    json=$("${SEMGREP_CMD[@]}" --metrics off --json --quiet --no-rewrite-rule-ids \
        --config "${rule}" "${target}")
    rc=$?
    set -e

    # Exit 7 means no rule was loaded. Reporting the count first would print a
    # reassuring 0, which is the whole trap this script exists to close.
    if [[ ${rc} -ne 0 ]]; then
        printf 'SEMGREP EXIT %s — no finding count below is meaningful\n' "${rc}"
        printf '%s' "${json}" | jq -r '.errors[]? | "  ERROR \(.level): \((.message // "") | split("\n")[0])"' 2>/dev/null || true
        status=1
        continue
    fi

    printf '%s' "${json}" | jq -r '.errors[]? | "  ERROR \(.level): \((.message // "") | split("\n")[0])"'
    # A count is only an answer about code semgrep actually read, so print the
    # scanned-file count beside it and say so outright when it is zero.
    scanned=$(printf '%s' "${json}" | jq '.paths.scanned | length')
    printf 'count=%s (scanned %s file(s))\n' \
        "$(printf '%s' "${json}" | jq '.results | length')" "${scanned}"
    if [[ "${scanned}" -eq 0 ]]; then
        printf '  NOTE: semgrep scanned nothing here — the extension does not map to the\n'
        printf '        rule language, or the path is skipped by .semgrepignore. The count\n'
        printf '        above says nothing about this code.\n'
    fi
    printf '%s' "${json}" | jq -r '.results[] | "  \(.path):\(.start.line)  \(.check_id)"'
done

exit "${status}"
