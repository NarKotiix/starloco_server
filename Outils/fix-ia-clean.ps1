#!/usr/bin/env powershell
# Script de correction du probleme des IA
# Usage: .\fix-ia-clean.ps1

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "SCRIPT DE CORRECTION - IA NE JOUENT PLUS" -ForegroundColor Cyan
Write-Host "Date: 20 Mars 2026" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

$ServerRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$LogDir = Join-Path $ServerRoot "Logs"
$ErrorLogDir = Join-Path $LogDir "Error"

# STEP 1: DIAGNOSTIC
Write-Host "[STEP 1] DIAGNOSTIC DES LOGS" -ForegroundColor Yellow
Write-Host "Recherche des erreurs de parsing de monstres..." -ForegroundColor Gray
Write-Host ""

if (Test-Path $ErrorLogDir) {
    $errorFiles = Get-ChildItem "$ErrorLogDir\*.log" -ErrorAction SilentlyContinue
    if ($errorFiles) {
        $errors = $errorFiles | Select-String -Pattern "For input string:" -NoEmphasis
        $count = ($errors | Measure-Object).Count
        Write-Host "  Nombre d'erreurs de parsing: $count" -ForegroundColor Red
        Write-Host ""

        if ($count -gt 0) {
            Write-Host "  Erreurs detaillees:" -ForegroundColor Red
            $errors | Select-Object -Unique -First 5 | ForEach-Object {
                Write-Host "    $_"
            }
        }
    }
} else {
    Write-Host "  Repertoire $ErrorLogDir non trouve" -ForegroundColor Yellow
}

Write-Host ""

# STEP 2: COMPILATION
Write-Host "[STEP 2] RECOMPILATION DU SERVEUR" -ForegroundColor Yellow
Write-Host "Compilation Java..." -ForegroundColor Gray
Write-Host ""

Push-Location $ServerRoot

$buildResult = & ".\gradlew.bat" build -x test 2>&1
$buildSuccess = $LASTEXITCODE -eq 0

if ($buildSuccess) {
    Write-Host "  [OK] Compilation reussie!" -ForegroundColor Green
    Write-Host "  JAR: build/libs/Server-1.0.0.jar" -ForegroundColor Green
} else {
    Write-Host "  [ERREUR] Compilation echouee!" -ForegroundColor Red
}

Pop-Location

Write-Host ""

# STEP 3: INSTRUCTIONS SQL
Write-Host "[STEP 3] NETTOYAGE DE LA BASE DE DONNEES MySQL" -ForegroundColor Yellow
Write-Host "Executez les commandes suivantes:" -ForegroundColor Gray
Write-Host ""

Write-Host "  Option A: Script SQL automatique" -ForegroundColor Cyan
Write-Host "  mysql -u root -p dofus_game < SQL_FIX_MONSTERS.sql" -ForegroundColor White
Write-Host ""

Write-Host "  Option B: Commandes manuelles dans MySQL" -ForegroundColor Cyan
Write-Host "  1. mysql -u root -p" -ForegroundColor White
Write-Host "  2. USE dofus_game;" -ForegroundColor White
Write-Host "  3. CREATE TABLE maps_backup AS SELECT * FROM maps;" -ForegroundColor White
Write-Host "  4. UPDATE maps SET monsters = ''" -ForegroundColor White
Write-Host "     WHERE monsters LIKE '%#%'" -ForegroundColor White
Write-Host "     OR monsters REGEXP '[0-9]{5},[0-9]{3}';" -ForegroundColor White
Write-Host "  5. SELECT COUNT(*) FROM maps WHERE monsters != '';" -ForegroundColor White
Write-Host ""

# STEP 4: RESTART
Write-Host "[STEP 4] REDEMARRAGE DU SERVEUR" -ForegroundColor Yellow
Write-Host "Actions a faire:" -ForegroundColor Gray
Write-Host ""
Write-Host "  1. Arretez le serveur (Ctrl+C)" -ForegroundColor Gray
Write-Host "  2. Supprimez les vieux logs:" -ForegroundColor Gray
Write-Host "     Remove-Item 'Logs\Error\*' -Force" -ForegroundColor Gray
Write-Host "  3. Demarrez le serveur:" -ForegroundColor Gray
Write-Host "     .\Start-Server.bat" -ForegroundColor Gray
Write-Host ""

# STEP 5: VERIFICATION
Write-Host "[STEP 5] VERIFICATION" -ForegroundColor Yellow
Write-Host "Verifiez que:" -ForegroundColor Gray
Write-Host ""
Write-Host "  - Pas d'erreur 'NumberFormatException' dans Logs\server.log" -ForegroundColor Gray
Write-Host "  - Les monstres spawent correctement" -ForegroundColor Gray
Write-Host "  - Les IA jouent normalement" -ForegroundColor Gray
Write-Host ""

Write-Host "  Commande pour verifier:" -ForegroundColor Cyan
Write-Host "  Get-Content 'Logs\server.log' -Tail 50 | Select-String 'NumberFormatException'" -ForegroundColor White
Write-Host ""

# SUMMARY
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "RESUME" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

if ($buildSuccess) {
    Write-Host "  [OK] Compilation du code reussie" -ForegroundColor Green
} else {
    Write-Host "  [ERREUR] Compilation du code echouee" -ForegroundColor Red
}

Write-Host "  [ATTENTE] Nettoyage de la base de donnees" -ForegroundColor Yellow
Write-Host "  [ATTENTE] Redemarrage du serveur" -ForegroundColor Yellow
Write-Host ""

Write-Host "Documentation:" -ForegroundColor Cyan
Write-Host "  - GUIDE_CORRECTION_IA.md" -ForegroundColor White
Write-Host "  - RAPPORT_ANALYSE_IA.md" -ForegroundColor White
Write-Host "  - SQL_FIX_MONSTERS.sql" -ForegroundColor White
Write-Host ""

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

