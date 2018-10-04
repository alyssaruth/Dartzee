@echo off

REM %1 = noOfBytes
REM %2 = version number
REM %3 = filename
REM %4 = port

echo Performing download of %1 bytes (Version %2)

java -jar AppUpdater.jar %1 %2 %3 %4
IF ERRORLEVEL 1 exit

start javaw -jar %3 justUpdated
exit