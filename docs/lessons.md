# Lessons

Notes for people working with an agent on this repository. Each entry is a
collaboration failure worth recognising early, not a code convention.

## 2026-09-22 — An agent will report a capability as absent after searching the wrong place

Asked whether a mechanism existed to propagate fixes from a repository template
to the repositories generated from it, the agent searched the organisation's
code for a workflow that did the job, found none, and reported that no such
mechanism existed anywhere. It then wrote that conclusion into a filed ticket
and proposed building the thing from scratch. The mechanism already existed —
as an installed, enabled tool in the agent's own environment, whose description
named the exact phrase the question had used.

The failure has a shape worth recognising: the agent searched the subject of the
question and not its own capabilities, and a negative answer from a search
carries the same confident tone as a positive one. It costs a colleague a
proposal to build what they already own, and it is expensive to catch late
because the wrong answer is not visibly uncertain.

When the question is whether something can be done rather than what some code
says, ask the agent to list the tools it already has for it before it searches
anywhere else, and treat any "there is no X" as provisional until it has said
which places it looked and why those were the right ones.

## 2026-09-21 — Ask the agent what evidence a trust judgement rests on

A Renovate bump replaced the actionlint binary CI runs with a community fork,
and the default branch went red. Asked to fix it, the agent assessed the fork
by its **star count and repository age**, concluded it was unvetted, and built
and pushed a pull request to remove it. Every number it cited was accurate. The
conclusion was wrong: the upstream project had been unmaintained for five
months, the fork was its acknowledged successor, and the maintainers of the
wrapper action had moved deliberately. All of that was sitting in a single open
issue on the upstream tracker that the agent never opened.

The tell was that the evidence was **quantitative and adjacent** rather than
direct. Stars and age measure attention, not maintenance. The question was
"is this project maintained", and the channel that answers it is the project's
own tracker and commit history.

So when an agent characterises a dependency, a tool or a third party, ask which
source it read. "It has 29 stars and is six weeks old" is a different kind of
answer from "its maintainer says X in issue N", and only the second is evidence
about maintenance. The first arrives with the same confidence.

The same session showed the failure repeating in a smaller shape: asked to fix
a lint finding, the agent proposed in turn an ignore rule, a permanent version
freeze, and a third-party binary compiled on the developer's own machine. Each
would have made the gate green. Each was rejected. The pattern worth naming out
loud is reaching for the change that clears the signal rather than the one that
removes the cause — and it is easiest to catch by asking what the proposed fix
would still be doing in a year.

Two things that worked, and are worth asking for by name:

- **Make it measure rather than argue.** A review objected that a syntax change
  depended on a runner feature flag nothing in the pull request could exercise.
  Rather than debating it, the agent added a temporary workflow that triggered
  on the branch, observed the flag on a real runner, and deleted it in the same
  pull request. The disagreement ended in one push.
- **Ask it to re-derive its own green result.** A verification printed
  `automerge=inherit` for a value that was actually `false`, because the `jq`
  `//` operator treats `false` as empty. The check looked like it had passed.
  A confirming result deserves the same scepticism as a failing one.
