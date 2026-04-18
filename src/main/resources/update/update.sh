#!/bin/bash
set -eu

filename=$1

sleep 2
mv Dartzee.jar Dartzee_OLD.jar
mv $filename Dartzee.jar
rm Dartzee_OLD.jar
chmod +x Dartzee.jar

java -Xms256m -Xmx512m -jar Dartzee.jar justUpdated trueLaunch
