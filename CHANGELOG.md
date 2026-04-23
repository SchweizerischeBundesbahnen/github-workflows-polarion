# Changelog

## 1.0.0 (2026-04-23)


### Features

* add issue-to-project workflow ([#15](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/15)) ([f64598b](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/f64598b6f4eac1c0050fb1d23c790f52e4258383)), closes [#14](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/14)
* add reusable release-please workflow ([#28](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/28)) ([da060b1](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/da060b11dc9e54718d7994ac2e67e1297049d209))
* add reusable release-please-guard workflow ([#74](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/74)) ([bd2f6a5](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/bd2f6a544b61537ab79e54a5719ca28560696ffa))
* add reusable-claude.yml for [@claude](https://github.com/claude) mention workflow ([#63](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/63)) ([3e6ca58](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/3e6ca5846dc503cecf13162feb4cdb17232bd0df))
* generalize review triage to all bot reviewers ([#45](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/45)) ([e47a3fe](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/e47a3fe7150b0f20255672f24c5ff5189d3f0cb7))
* harden reusable-claude.yml and align model/turns across both workflows ([#68](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/68)) ([bc86478](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/bc864784877b93186b9628f029b5235844eea2e8)), closes [#64](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/64)
* minimize outdated Claude summary comments on re-review ([#21](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/21)) ([5d48b96](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/5d48b96f6db29c78c1922ec24a173a9e6061ff78)), closes [#18](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/18)
* triage Copilot review comments in Claude Code Review ([#22](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/22)) ([5f4f747](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/5f4f7471d6759151be8a2e31325e3b59a9388fbe)), closes [#19](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/19)
* triage human reviewer comments in Claude Code Review ([#47](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/47)) ([8cc1344](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/8cc1344325b478c511e6f5d32b49573e8aebca5a))
* use formal PR review verdict and add extra_focus input ([#69](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/69)) ([af58bad](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/af58badffab76f14d6758d80759da03418c09146))


### Bug Fixes

* add actions: read permission for CI MCP server ([#55](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/55)) ([baf9b2c](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/baf9b2c174742939d09a904dd374cb7001629760))
* add actions: read permission for claude-code-action CI MCP server ([baf9b2c](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/baf9b2c174742939d09a904dd374cb7001629760)), closes [#54](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/54)
* add actions:read to claude-code-review caller permissions ([f912157](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/f912157b44dcb4609690edc77cb0461d8308481a))
* add pull-requests:read permission to reusable-actionlint ([#57](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/57)) ([a8540e6](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/a8540e6772b88b2032a6234959ce04821800a719)), closes [#56](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/56)
* add pull-requests:read to actionlint caller permissions ([#59](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/59)) ([6f9e2d2](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/6f9e2d21cd17c9a1b6a162ca856f14de250dca59)), closes [#58](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/58)
* classify PR-level reviews in shell pre-processing step ([#71](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/71)) ([b547880](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/b547880af93ea9364f57bb1e42b185ed0fae1107))
* deprecation of app-id ([319f7d7](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/319f7d7de565700845ca42aba6fcfdf2a2fcec15))
* move thread classification into deterministic shell pre-processing step ([#51](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/51)) ([8e4b297](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/8e4b297f23e7bad3369257d367b7b35196ff7ec2)), closes [#49](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/49)
* pass github_token to claude-code-action to fix git fetch with persist-credentials: false ([#40](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/40)) ([8b26009](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/8b26009a08943f2673be9729c2684b4fa7ca72f6)), closes [#39](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/39)
* prevent hallucinated findings from leaking into review summary ([#72](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/72)) ([81c7ae9](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/81c7ae9286d04d2433e1ae9c62830d4bdf5ae3cf))
* remove redundant github_token from claude-code-action ([#48](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/48)) ([eb185a3](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/eb185a366c96432b9e92fe5c249f890ead1045d5))
* set fetch-depth: 0 to prevent claude-code-action git fetch failure ([#42](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/42)) ([c84be24](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/c84be242ed1da4ee233766f9c7b856e7ed0aa4e0)), closes [#41](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/41)
* set persist-credentials: true for claude-code-action git fetch ([#44](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/44)) ([2aeb7ea](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/2aeb7ea85188b4777c495989a93f4e27035e6f34))
* set persist-credentials: true in reusable-actionlint ([#61](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/61)) ([a9b4272](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/a9b4272663d048037532b90da6fd2f23ef21d682))
* use GraphQL __typename for bot/human classification in review triage ([#50](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/50)) ([9a9a055](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/9a9a0550fc36c24fc118caaf5b871c29c5cee9db)), closes [#49](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/issues/49)


### Reverts

* pin claude-code-action back to v1.0.88 and add extra_focus to caller ([1e9bd1a](https://github.com/SchweizerischeBundesbahnen/github-workflows-polarion/commit/1e9bd1ae10749ff535d5ec65c13abc4ab4f34aef))

## Changelog

All notable changes to this project should be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/) and this project follows [Semantic Versioning](https://semver.org/) where practical.

## Unreleased

- Initial template content
