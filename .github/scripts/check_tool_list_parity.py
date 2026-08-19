#!/usr/bin/env python3
"""Assert that a claude-code-action step declares one tool list, not two.

The action reads the agent's tool list from two places that mean different
things. ``settings.permissions.allow`` governs whether a call is permitted.
``claude_args --allowedTools`` governs whether an MCP server is started, because
``install-mcp-server.ts`` starts a server only when the allowed-tools list
carries its ``mcp__<server>__`` prefix, and in agent mode that list is parsed
from ``claude_args`` alone.

A tool declared in ``settings`` alone is therefore permitted but unavailable:
its server never starts, and nothing reports it. That is the defect this script
exists to make loud.

The reverse is not symmetrical. ``base-action/src/parse-sdk-options.ts`` merges
both flag spellings and passes the result to the SDK as ``allowedTools``, its
permission allowlist, so a tool declared in ``claude_args`` alone is both
started and permitted. Where both lists exist they are still required to agree,
because a reader cannot tell which one is authoritative, but a step declaring
only ``claude_args`` is the single-list state and passes.

Usage:
    check_tool_list_parity.py <workflow.yml> [<workflow.yml> ...]

Exits 0 when every claude-code-action step in every file agrees with itself,
1 on any divergence, and 2 when a file cannot be read or parsed.
"""

from __future__ import annotations

import json
import shlex
import sys

import yaml

ACTION = "anthropics/claude-code-action"

# Mirrors parseAllowedTools in the action's src/modes/agent/parse-tools.ts: both
# flag spellings, every occurrence merged, all consecutive non-flag values
# consumed, comment lines stripped. The action compares the flag token exactly
# (`flag = arg.slice(2)`), so `--allowedTools=X` is not recognised there and must
# not be recognised here — treating it as a declaration is what would let a
# config that grants nothing pass this check.
ALLOWED_TOOLS_FLAGS = ("--allowedTools", "--allowed-tools")


def parse_allowed_tools(claude_args: str) -> set[str] | None:
    """Return the --allowedTools entries, or None when the flag is absent."""
    tokens = shlex.split(
        "\n".join(
            line for line in claude_args.splitlines() if not line.strip().startswith("#")
        )
    )
    tools: set[str] = set()
    seen_flag = False
    index = 0
    while index < len(tokens):
        if tokens[index] not in ALLOWED_TOOLS_FLAGS:
            index += 1
            continue
        seen_flag = True
        index += 1
        while index < len(tokens) and not tokens[index].startswith("--"):
            # No tool name contains a comma: the argument syntax has no way to
            # escape one, so a comma is unambiguously a separator.
            tools.update(e.strip() for e in tokens[index].split(",") if e.strip())
            index += 1
    return tools if seen_flag else None


def parse_settings_allow(settings: object) -> set[str] | None:
    """Return settings.permissions.allow, or None when the block omits it.

    The action accepts the input as a JSON string or as a path to a JSON file
    (action.yml: "Claude Code settings as JSON string or path to settings JSON
    file"), and JSON is valid YAML so the inline form can also arrive already
    parsed as a mapping. All three are resolved here.
    """
    text = settings if isinstance(settings, str) else json.dumps(settings)
    if not text.lstrip().startswith("{"):
        with open(text.strip(), encoding="utf-8") as handle:
            text = handle.read()
    document = json.loads(text)
    permissions = document.get("permissions", {}) if isinstance(document, dict) else None
    if not isinstance(permissions, dict):
        # A settings *file* may hold any JSON document, and permissions may be
        # written as something other than a mapping. .get() on either raises
        # AttributeError, which no handler here catches, so the run would end on
        # a traceback naming this line instead of the workflow, job and step.
        raise TypeError("settings has no permissions mapping")
    allow = permissions.get("allow")
    if allow is None:
        return None
    # set() over a str yields its characters, and a non-str entry has no
    # .startswith, so the settings-only branch below would either find no mcp__
    # server and pass — a silent pass on exactly the configuration this script
    # exists to catch — or raise AttributeError outside any handler.
    # permissions.allow is a JSON array of strings; anything else is malformed
    # input worth naming rather than coercing.
    if not isinstance(allow, list) or not all(isinstance(t, str) for t in allow):
        raise TypeError(f"permissions.allow is not a list of strings: {allow!r}")
    return set(allow)


def check_step(path: str, job: str, index: int, step: dict) -> list[str]:
    """Return one message per divergence found in a single action step."""
    where = f"{path}: job '{job}', step {index}"
    with_ = step.get("with") or {}
    settings, claude_args = with_.get("settings"), with_.get("claude_args")

    # A ${{ }} expression is resolved by the runner, so its value is not in the
    # file. Neither list can be read, and reporting the expression text as a
    # divergence would attach a runtime claim to something never parsed. Say the
    # step went unchecked instead, on stderr, so the gap stays visible: silently
    # returning would let an expression remove a step from coverage, which is
    # the failure this script exists to prevent.
    unresolved = [
        name
        for name, value in (("settings", settings), ("claude_args", claude_args))
        if isinstance(value, str) and "${{" in value
    ]
    if unresolved:
        print(
            f"{where}: not checked, {' and '.join(unresolved)} is a GitHub "
            f"expression resolved at run time",
            file=sys.stderr,
        )
        return []

    try:
        from_settings = None if settings is None else parse_settings_allow(settings)
    except (json.JSONDecodeError, OSError, TypeError) as exc:
        return [f"{where}: settings cannot be read as JSON ({exc})"]
    try:
        from_args = None if claude_args is None else parse_allowed_tools(str(claude_args))
    except ValueError as exc:
        return [f"{where}: claude_args cannot be tokenized ({exc})"]

    if from_settings is None:
        # Either the step declares no tool list at all, or it declares only
        # claude_args. The second is the single-list state: --allowedTools is
        # itself the SDK's permission allowlist and also decides which MCP
        # servers start, so there is no second list to diverge from.
        return []
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
            f"{args_only} — permitted and started, but the step declares two "
            f"lists and a reader cannot tell which is authoritative"
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
