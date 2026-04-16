@echo off

REM %1 = filename

ren Dartzee.jar Dartzee_OLD.jar
ren %3 Dartzee.jar
del Dartzee_OLD.jar

start javaw -Xms256m -Xmx512m -jar Dartzee.jar justUpdated trueLaunch
exit