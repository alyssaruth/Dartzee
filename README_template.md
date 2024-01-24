# Dartzee

[![Build Status](https://github.com/alyssaruth/Dartzee/workflows/CI/badge.svg)](https://github.com/alyssaruth/dartzee/actions)
![Coverage: $coveragePercent](https://img.shields.io/badge/coverage-${coveragePercent}25-brightgreen)

All rights reserved

## Setup

This project uses [Daktari](https://github.com/glean-notes/daktari) to aid with setup. The recommended way to install it is via ASDF, by running `./init-asdf.sh` from the repo root. Alternatively, it can be installed directly using pip.

Once installed, run `daktari` from the repo root to check your setup. If you're missing anything, it will tell you what to do.

## Run

To run the project, just run the gradle task `runDev`

## Credentials

There are some credential files (not in source control) that you'll need in `src/main/resources` for certain things to work (although the app will run without them):

- `AWS_LOGS` - For posting logs to elasticsearch (disabled by default in devMode anyway)
- `AWS_SYNC` - For accessing S3 during a database sync

### Backlog

Full backlog can be found [here](https://trello.com/b/Plz8blWw/dartzee)
