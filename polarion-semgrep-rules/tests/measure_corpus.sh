#!/usr/bin/env bash
# tests/measure_corpus.sh — count rule-pack findings per target repository.
#
# Not a CI gate: it needs local clones, so it cannot run on a runner. It exists
# because the flags decide whether two measurements are comparable at all, and
# retyping them by hand is where a number quietly stops meaning what the last one
# meant. It pins the semgrep version, `--no-rewrite-rule-ids`, and the excludes
# the reusable workflow passes.
#
# A missing or unreadable target is a FAILURE, never a skip. A skip would let
# this print a total that looks like the baseline while having measured fewer
# repositories than the baseline covers.
#
# Usage:
#   tests/measure_corpus.sh <target-dir>...
#   POLARION_TARGETS_DIR=<dir> tests/measure_corpus.sh
#     with no arguments, every immediate subdirectory of that directory.

set -euo pipefail
shopt -s nullglob

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACK_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
RULES_DIR="${PACK_DIR}/rules"
VERSION="$(tr -d '[:space:]' < "${PACK_DIR}/SEMGREP_VERSION")"

read -r -a SEMGREP_CMD <<< "${SEMGREP:-uvx semgrep==${VERSION}}"

if ! command -v jq >/dev/null 2>&1; then
    echo "ERROR: jq not installed." >&2
    exit 1
fi

targets=("$@")
if [[ ${#targets[@]} -eq 0 ]]; then
    if [[ -z "${POLARION_TARGETS_DIR:-}" ]]; then
        echo "ERROR: pass target directories, or set POLARION_TARGETS_DIR." >&2
        exit 1
    fi
    for d in "${POLARION_TARGETS_DIR}"/*/; do
        targets+=("${d%/}")
    done
fi

if [[ ${#targets[@]} -eq 0 ]]; then
    echo "ERROR: no targets found." >&2
    exit 1
fi

total=0
for target in "${targets[@]}"; do
    if [[ ! -d "${target}" ]]; then
        echo "FAIL: ${target} is not a directory" >&2
        exit 1
    fi

    if ! json=$("${SEMGREP_CMD[@]}" scan \
        --config "${RULES_DIR}" \
        --no-rewrite-rule-ids \
        --json --metrics off --quiet \
        --exclude .polarion-rules \
        --exclude target --exclude node_modules --exclude build \
        "${target}" 2>/dev/null); then
        echo "FAIL: semgrep errored on ${target}" >&2
        exit 1
    fi

    n=$(printf '%s' "${json}" | jq '.results | length')
    printf '%-46s %3s\n' "$(basename "${target}")" "${n}"
    # Per-rule breakdown, so a total that holds while its composition changed is
    # visible rather than reassuring.
    printf '%s' "${json}" | jq -r '.results | group_by(.check_id) | .[] |
        "    \(.[0].check_id)  \(length)"'
    total=$((total + n))
done

echo ""
echo "TOTAL ${total} finding(s) across ${#targets[@]} target(s), semgrep ${VERSION}"
