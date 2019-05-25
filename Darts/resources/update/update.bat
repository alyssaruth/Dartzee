@echo off

REM %1 = noOfBytes
REM %2 = version number
REM %3 = filename
REM %4 = assetId

echo Performing download of %1 bytes (Version %2)

curl -LJO -H "Accept: application/octet-stream" https://api.github.com/repos/alexburlton/DartzeeRelease/releases/assets/%4

ren Dartzee.jar Dartzee_OLD.jar
ren %3 Dartzee.jar
del Dartzee_OLD.jar

start javaw -jar Dartzee.jar justUpdated
exit