@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title StarLoco v1.0 - CTRL+C Shutdown propre

set JAR_PATH=build\libs\Server-1.0.0.jar
set JVM_OPENS=--add-opens=java.base/java.lang=ALL-UNNAMED

if not exist "%JAR_PATH%" (
    echo [ERREUR] JAR absent: %JAR_PATH%
    echo Exemple: gradlew.bat build
    timeout /t 5 >nul
    exit /b 1
)

echo ========================================
echo StarLoco Server Launcher
echo ========================================
echo JAR: %JAR_PATH%
echo UTF-8 ^| Couleurs ANSI ^| CTRL+C OK
echo ========================================
java %JVM_OPENS% -Dfile.encoding=UTF-8 -Xms512M -Xmx2G -jar "%JAR_PATH%"
echo.
echo [OK] Shutdown complete - Logs dans Logs/
timeout /t 2 >nul