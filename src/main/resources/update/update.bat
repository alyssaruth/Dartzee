@echo on

REM %1 = filename

timeout 2
del Dartzee.jar || goto :error
ren %1 Dartzee.jar || goto :error

start javaw -Xms256m -Xmx512m -jar Dartzee.jar justUpdated trueLaunch || goto :error
exit

:error
echo Something went wrong! Please copy the contents of this so Alyssa can fix it :) Error level: #%errorlevel%
