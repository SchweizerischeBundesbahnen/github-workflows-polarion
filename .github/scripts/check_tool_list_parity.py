#!/usr/bin/env python3
"""Assert that a claude-code-action step declares one tool list, not two.

The action reads the agent's tool list from two places that mean different
things. ``settings.permissions.allow`` governs whether a call is permitted.
``claude_args --allowedTools`` governs whether an MCP server is started, because
``install-mcp-server.ts`` starts a server only when the allowed-tools list
carries its ``mcp__<server>__`` prefix, and in agent mode that list is parsed
from ``claude_args`` alone.

A tool present in only one list therefore fails silently, in one of two
directions: declared in ``settings`` alone it is permitted but its server never
starts, and declared in ``claude_args`` alone its server starts but every call
is denied. Neither produces an error, a warning, or any signal on the pull
request. That is the defect this script exists to make loud.

Usage:
    check_tool_list_parity.py <workflow.yml> [<workflow.yml> ...]

Exits 0 when every claude-code-action step in every file agrees with itself,
1 on any divergence, and 2 when a file cannot be read or parsed.
"""

from __future__ import annotations

import json
import re
import sys

import yaml

ACTION = "anthropics/claude-code-action"

# Matches --allowedTools followed by its value, quoted or bare. claude_args is a
# folded scalar, so the value arrives on one line by the time it is read here.
ALLOWED_TOOLS = re.compile(
    r"--allowedTools[=\s]+(?:\"([^\"]*)\"|'([^']*)'|(\S+))"
)


def parse_allowed_tools(claude_args: str) -> set[str] | None:
    """Return the --allowedTools entries, or None when the flag is absent."""
    match = ALLOWED_TOOLS.search(claude_args)
    if match is None:
        return None
    value = next(group for group in match.groups() if group is not None)
    # No tool name contains a comma: the argument syntax has no way to escape
    # one, so a comma is unambiguously a separator.
    return {entry.strip() for entry in value.split(",") if entry.strip()}


def parse_settings_allow(settings: str) -> set[str] | None:
    """Return settings.permissions.allow, or None when the block omits it."""
    allow = json.loads(settings).get("permissions", {}).get("allow")
    return None if allow is None else set(allow)


def check_step(path: str, job: str, index: int, step: dict) -> list[str]:
    """Return one message per divergence found in a single action step."""
    where = f"{path}: job '{job}', step {index}"
    with_ = step.get("with") or {}
    settings, claude_args = with_.get("settings"), with_.get("claude_args")

    try:
        from_settings = None if settings is None else parse_settings_allow(settings)
    except (json.JSONDecodeError, AttributeError) as exc:
        return [f"{where}: settings is not valid JSON ({exc})"]
    from_args = None if claude_args is None else parse_allowed_tools(str(claude_args))

    if from_settings is None:
        if from_args is None:
            # The step relies on the action's defaults for both. Nothing to
            # compare, and nothing can diverge.
            return []
        return [
            f"{where}: --allowedTools declares {sorted(from_args)} but there is no "
            f"settings.permissions.allow, so every call is denied"
        ]
    if from_args is None:
        servers = sorted(t for t in from_settings if t.startswith("mcp__"))
        if not servers:
            return []
        return [
            f"{where}: settings.permissions.allow declares {servers} but there is no "
            f"--allowedTools, so those MCP servers never start"
        ]

    messages = []
    if settings_only := sorted(from_settings - from_args):
        messages.append(
            f"{where}: in settings.permissions.allow but not --allowedTools: "
            f"{settings_only} — permitted, but any MCP server among them never starts"
        )
    if args_only := sorted(from_args - from_settings):
        messages.append(
            f"{where}: in --allowedTools but not settings.permissions.allow: "
            f"{args_only} — the server starts, but every call is denied"
        )
    return messages


def check_file(path: str) -> list[str]:
    with open(path, encoding="utf-8") as handle:
        workflow = yaml.safe_load(handle)

    messages = []
    for job_name, job in (workflow.get("jobs") or {}).items():
        for index, step in enumerate(job.get("steps") or [], start=1):
            if isinstance(step, dict) and str(step.get("uses", "")).startswith(ACTION):
                messages.extend(check_step(path, job_name, index, step))
    return messages


def main(argv: list[str]) -> int:
    if not argv:
        print(f"usage: {sys.argv[0]} <workflow.yml> [...]", file=sys.stderr)
        return 2

    messages = []
    for path in argv:
        try:
            messages.extend(check_file(path))
        except (OSError, yaml.YAMLError) as exc:
            print(f"{path}: cannot be read ({exc})", file=sys.stderr)
            return 2

    for message in messages:
        print(message, file=sys.stderr)
    if messages:
        print(
            "\nThe two lists must hold the same tools. See the INVARIANT comment "
            "above claude_args in the affected workflow.",
            file=sys.stderr,
        )
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
