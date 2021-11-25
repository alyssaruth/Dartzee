#!/bin/bash
set -eux

scriptDir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

xmlReport="$scriptDir/../build/reports/kover/report.xml"

lineCoverageXml=$(cat $xmlReport | grep "type=\"LINE\"" | tail -1)
linesMissed=$(sed 's/.*missed=".*"//' <<< "$lineCoverageXml")
