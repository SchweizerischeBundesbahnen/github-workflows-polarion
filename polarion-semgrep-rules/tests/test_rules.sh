#!/usr/bin/env bash
# tests/test_rules.sh — verify each rule fires on its .vuln.* fixture and stays
# silent on its .fixed.* fixture.
#
# Raw semgrep invocations rather than `semgrep --test`, because --test crashes
# on this split rules/fixtures layout (Semgrep IndexError in path pairing,
# observed on 1.161.0).
#
# A rule without both fixtures is a FAILURE, not a skip: a skip would let an
# untested rule ship while the suite still reported success. The rule count is
# asserted against the number tested for the same reason.
#
# Override the semgrep invocation via SEMGREP (default: `semgrep`), e.g.
#   SEMGREP="uvx semgrep==1.172.0" bash tests/test_rules.sh

set -euo pipefail
shopt -s nullglob

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACK_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
RULES_DIR="${PACK_DIR}/rules"
FIXTURES_DIR="${SCRIPT_DIR}/fixtures"

read -r -a SEMGREP_CMD <<< "${SEMGREP:-semgrep}"

if ! command -v "${SEMGREP_CMD[0]}" >/dev/null 2>&1; then
    echo "ERROR: ${SEMGREP_CMD[0]} not found. Install semgrep (uv tool install semgrep) or set SEMGREP." >&2
    exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
    echo "ERROR: jq not installed." >&2
    exit 1
fi

count_findings() {
    local rule="$1" fixture="$2" json
    if ! json=$("${SEMGREP_CMD[@]}" --metrics off --json --quiet --config "${rule}" "${fixture}"); then
        echo "ERROR: semgrep failed on ${rule} against ${fixture}" >&2
        return 1
    fi
    printf '%s' "${json}" | jq '.results | length'
}

rules=("${RULES_DIR}"/*.yaml)
if [[ ${#rules[@]} -eq 0 ]]; then
    echo "ERROR: no rules found in ${RULES_DIR}" >&2
    exit 1
fi

failed=0
tested=0

for rule in "${rules[@]}"; do
    name=$(basename "${rule}" .yaml)
    # Fixture extension varies by rule language (.java, .properties, .toml).
    vuln=("${FIXTURES_DIR}/${name}".vuln.*)
    fixed=("${FIXTURES_DIR}/${name}".fixed.*)

    if [[ ${#vuln[@]} -ne 1 || ${#fixed[@]} -ne 1 ]]; then
        printf 'FAIL: %-40s expected exactly one .vuln.* and one .fixed.* fixture (found %d / %d)\n' \
            "${name}" "${#vuln[@]}" "${#fixed[@]}"
        failed=$((failed + 1))
        continue
    fi

    n_vuln=$(count_findings "${rule}" "${vuln[0]}")
    n_fixed=$(count_findings "${rule}" "${fixed[0]}")
    tested=$((tested + 1))

    if [[ "${n_vuln}" -gt 0 && "${n_fixed}" -eq 0 ]]; then
        printf 'PASS: %-40s vuln=%s fixed=%s\n' "${name}" "${n_vuln}" "${n_fixed}"
    else
        printf 'FAIL: %-40s vuln=%s (expected >0) fixed=%s (expected 0)\n' \
            "${name}" "${n_vuln}" "${n_fixed}"
        failed=$((failed + 1))
    fi
done

echo ""
echo "Tested ${tested} of ${#rules[@]} rule(s)."

if [[ ${failed} -gt 0 || ${tested} -ne ${#rules[@]} ]]; then
    echo "FAILED: ${failed} rule(s) did not match their fixtures." >&2
    exit 1
fi

echo "All ${tested} rules pass against their fixtures."
