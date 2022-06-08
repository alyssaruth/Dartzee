# Dartzee

[![Build Status](https://github.com/alyssaburlton/Dartzee/workflows/CI/badge.svg)](https://github.com/alyssaburlton/dartzee/actions)
![Coverage: 86.70%](https://img.shields.io/badge/coverage-86.70%25-brightgreen)

All rights reserved

## Run

To run the project, just run the gradle task `runDev`

## Credentials

There are some credential files (not in source control) that you'll need in `src/main/resources` for certain things to work (although the app will run without them):

- `AWS_LOGS` - For posting logs to elasticsearch (disabled by default in devMode anyway)
- `AWS_SYNC` - For accessing S3 during a database sync

### Backlog
Full backlog can be found [here](https://trello.com/b/Plz8blWw/dartzee)
