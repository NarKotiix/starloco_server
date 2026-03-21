#!/usr/bin/env powershell
# Script de correction automatique du problème des IA

# Configuration
$ServerRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$LogDir = Join-Path $ServerRoot "Logs"
$ErrorLogDir = Join-Path $LogDir "Error"

Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   SCRIPT DE CORRECTION - IA NE JOUENT PLUS             ║" -ForegroundColor Cyan
Write-Host "║   Date: 20 Mars 2026                                   ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Fonction pour exécuter un test
function Test-Diagnostic {
    Write-Host "🔍 ÉTAPE 1: DIAGNOSTIC DES LOGS" -ForegroundColor Yellow
    Write-Host "Recherche des erreurs de parsing de monstres..." -ForegroundColor Gray

    if (Test-Path $ErrorLogDir) {
        $errors = @(Get-ChildItem "$ErrorLogDir\*.log" -ErrorAction SilentlyContinue |
                   Select-String -Pattern "NumberFormatException.*For input string:" -NoEmphasis |
                   Measure-Object).Count

        Write-Host "  Nombre d'erreurs de parsing trouvées: $errors" -ForegroundColor Red

        if ($errors -gt 0) {
            Write-Host "  Erreurs détectées:" -ForegroundColor Red
            Get-ChildItem "$ErrorLogDir\*.log" -ErrorAction SilentlyContinue |
            Select-String -Pattern "For input string:" -NoEmphasis |
            Select-Object -Unique -First 10 |
            ForEach-Object { Write-Host "    $_" }
        }
    } else {
        Write-Host "  ⚠️  Répertoire $ErrorLogDir non trouvé" -ForegroundColor Yellow
    }
    Write-Host ""
}

# Fonction pour compiler
function Test-Compilation {
    Write-Host "🔨 ÉTAPE 2: RECOMPILATION DU SERVEUR" -ForegroundColor Yellow
    Write-Host "Vérification de la syntaxe Java..." -ForegroundColor Gray

    Push-Location $ServerRoot

    Write-Host "  Exécution: .\gradlew.bat build -x test" -ForegroundColor Gray

    try {
        $result = & ".\gradlew.bat" build -x test 2>&1
        $buildSuccess = $LASTEXITCODE -eq 0

        if ($buildSuccess) {
            Write-Host "  ✅ Compilation réussie!" -ForegroundColor Green
            Write-Host "  JAR créé: build/libs/Server-1.0.0.jar" -ForegroundColor Green
        } else {
            Write-Host "  ❌ Compilation échouée!" -ForegroundColor Red
            Write-Host "  Erreurs:" -ForegroundColor Red
            $result | Where-Object { $_ -match "error|ERROR" } | ForEach-Object { Write-Host "    $_" }
        }
    } catch {
        Write-Host "  ❌ Erreur lors de la compilation: $_" -ForegroundColor Red
        $buildSuccess = $false
    }

    Pop-Location
    Write-Host ""

    return $buildSuccess
}

# Fonction pour afficher les instructions SQL
function Show-SQLInstructions {
    Write-Host "💾 ÉTAPE 3: NETTOYAGE DE LA BASE DE DONNÉES" -ForegroundColor Yellow
    Write-Host "Exécutez les commandes suivantes dans MySQL:" -ForegroundColor Gray
    Write-Host ""

    Write-Host "Option A: Automatique avec le script SQL" -ForegroundColor Cyan
    Write-Host "  mysql -u root -p dofus_game < SQL_FIX_MONSTERS.sql" -ForegroundColor White
    Write-Host ""

    Write-Host "Ou option B: Manuelle dans MySQL" -ForegroundColor Cyan
    Write-Host "  1. mysql -u root -p" -ForegroundColor White
    Write-Host "  2. USE dofus_game;" -ForegroundColor White
    Write-Host "  3. CREATE TABLE maps_backup AS SELECT * FROM maps;" -ForegroundColor White
    Write-Host "  4. UPDATE maps SET monsters = '' WHERE monsters LIKE '%#%';" -ForegroundColor White
    Write-Host "  5. UPDATE maps SET monsters = '' WHERE monsters REGEXP '[0-9]{5},[0-9]{3}';" -ForegroundColor White
    Write-Host "  6. SELECT COUNT(*) FROM maps WHERE monsters LIKE '%#%';" -ForegroundColor White
    Write-Host ""
}

# Fonction pour afficher les instructions de redémarrage
function Show-RestartInstructions {
    Write-Host "🔄 ÉTAPE 4: REDÉMARRAGE DU SERVEUR" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  1. Arrêtez le serveur (Ctrl+C ou fermez la fenêtre)" -ForegroundColor Gray
    Write-Host "  2. Supprimez les vieux logs:" -ForegroundColor Gray
    Write-Host "     Remove-Item 'Logs\Error\*' -Force" -ForegroundColor Gray
    Write-Host "  3. Démarrez le serveur:" -ForegroundColor Gray
    Write-Host "     .\Start-Server.bat" -ForegroundColor Gray
    Write-Host ""
}

# Fonction pour tester les résultats
function Test-Results {
    Write-Host "✅ ÉTAPE 5: VÉRIFICATION DES RÉSULTATS" -ForegroundColor Yellow
    Write-Host ""

    Write-Host "Vérifiez que:" -ForegroundColor Gray
    Write-Host "  ✓ Pas d'erreur 'NumberFormatException' dans les logs" -ForegroundColor Gray
    Write-Host "  ✓ Les monstres spawn correctement sur les maps" -ForegroundColor Gray
    Write-Host "  ✓ Les IA jouent normalement en combat" -ForegroundColor Gray
    Write-Host ""

    Write-Host "Commande pour vérifier les erreurs:" -ForegroundColor Cyan
    Write-Host "  Get-Content 'Logs\server.log' -Tail 100 | Select-String 'NumberFormatException'" -ForegroundColor White
    Write-Host ""

    Write-Host "Commande pour compter les monstres chargés:" -ForegroundColor Cyan
    Write-Host "  Get-Content 'Logs\server.log' | Select-String 'monsters' | Measure-Object" -ForegroundColor White
    Write-Host ""
}

# Fonction d'aide
function Show-Help {
    Write-Host "Usage: .\fix-ia.ps1 [option]" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Gray
    Write-Host "  -Full       Exécute tout le diagnostic et la compilation" -ForegroundColor Gray
    Write-Host "  -Compile    Recompile le serveur" -ForegroundColor Gray
    Write-Host "  -Diagnose   Affiche uniquement le diagnostic" -ForegroundColor Gray
    Write-Host "  -Help       Affiche cette aide" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Exemples:" -ForegroundColor Gray
    Write-Host "  .\fix-ia.ps1 -Full" -ForegroundColor White
    Write-Host "  .\fix-ia.ps1 -Compile" -ForegroundColor White
    Write-Host ""
}

# Main
if ($args -contains "-Help") {
    Show-Help
    exit 0
}

if ($args -contains "-Diagnose" -or $args -contains "-Full") {
    Test-Diagnostic
}

if ($args -contains "-Compile" -or $args -contains "-Full") {
    $compilationOk = Test-Compilation

    if (-not $compilationOk) {
        Write-Host "❌ La compilation a échoué. Vérifiez les erreurs ci-dessus." -ForegroundColor Red
        exit 1
    }
}

# Afficher les instructions
if ($args -contains "-Full" -or $args.Count -eq 0) {
    Show-SQLInstructions
    Show-RestartInstructions
    Test-Results
}

Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "📖 Pour plus d'informations, consultez:" -ForegroundColor Cyan
Write-Host "   - GUIDE_CORRECTION_IA.md" -ForegroundColor White
Write-Host "   - RAPPORT_ANALYSE_IA.md" -ForegroundColor White
Write-Host "═══════════════════════════════════════════════════════" -ForegroundColor Cyan


