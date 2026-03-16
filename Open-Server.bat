@echo off
title StarLoco - Game Server

REM Dossier du script
cd /d "%~dp0"

REM Chemin vers ta Java 8 (optionnel mais recommandé)
REM set JAVA_HOME=C:\Program Files\Java\jre1.8.0_481
REM set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo [INFO] Lancement du serveur StarLoco...
echo.

:START
java -Xms512m -Xmx1024m -XX:+UseG1GC -jar game.jar

echo.
echo [INFO] Le serveur est ferme.
echo [INFO] Appuyez sur une touche pour quitter...
pause >nul
goto :EOF