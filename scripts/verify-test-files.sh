#!/bin/bash
set -eu

badTestFiles=$(grep -r "class Test[^\:]*$" src/test || true)
if [ ! -z "$badTestFiles" ]; then
  echo "The following test files do not correctly extend AbstractTest:"
  echo $badTestFiles
  exit 1
fi