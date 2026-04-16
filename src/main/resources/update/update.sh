#!/bin/bash
set -eu

filename=$1

mv Dartzee.jar Dartzee_OLD.jar
mv $filename Dartzee.jar
rm Dartzee_OLD.jar

java -Xms256m -Xmx512m -jar Dartzee.jar justUpdated trueLaunch
