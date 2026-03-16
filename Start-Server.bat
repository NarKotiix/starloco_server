@echo off
REM ============================================================================
REM Script de lancement du serveur StarLoco
REM - Active l'encodage UTF-8 dans la console Windows
REM - Support des couleurs ANSI pour les logs
REM - Mode debug optionnel
REM ============================================================================

setlocal enabledelayedexpansion

REM Activer UTF-8 dans la console Windows (code page 65001)
chcp 65001 > nul

REM Forcer UTF-8 et support ANSI via les variables d'environnement
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8

REM Chemin du JAR
set JAR_PATH=build\libs\Server-1.0.0.jar

REM Vérifier si le JAR existe
if not exist "%JAR_PATH%" (
    echo.
    echo [ERREUR] Le fichier %JAR_PATH% n'existe pas.
    echo Assurez-vous d'avoir compilé le projet avec : gradlew build
    echo.
    pause
    exit /b 1
)

REM Mode de démarrage
if "%1"=="--debug" (
    echo [DEBUG MODE] Démarrage du serveur avec informations de debug
    java %JAVA_TOOL_OPTIONS% -Xms512M -Xmx2G -jar "%JAR_PATH%"
) else (
    REM Mode normal
    echo Démarrage du serveur StarLoco...
    echo Chemin du JAR: %JAR_PATH%
    echo Encodage: UTF-8
    echo Support couleurs: ANSI
    echo.
    java %JAVA_TOOL_OPTIONS% -Xms512M -Xmx2G -jar "%JAR_PATH%"
)

REM Pause à la fin si erreur de démarrage
if errorlevel 1 (
    echo.
    echo [ERREUR] Le serveur s'est arrêté de manière inattendue.
    pause
)

endlocal

