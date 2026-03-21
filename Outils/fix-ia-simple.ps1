#!/usr/bin/env powershell
# Script de correction automatique du problème des IA
# Usage: .\fix-ia-simple.ps1

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   SCRIPT DE CORRECTION - IA NE JOUENT PLUS             ║" -ForegroundColor Cyan
Write-Host "║   Date: 20 Mars 2026                                   ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

$ServerRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$LogDir = Join-Path $ServerRoot "Logs"
$ErrorLogDir = Join-Path $LogDir "Error"

# === ÉTAPE 1: DIAGNOSTIC ===
Write-Host "🔍 ÉTAPE 1: DIAGNOSTIC DES LOGS" -ForegroundColor Yellow
Write-Host "Recherche des erreurs de parsing de monstres...`n" -ForegroundColor Gray

if (Test-Path $ErrorLogDir) {
    $errorFiles = Get-ChildItem "$ErrorLogDir\*.log" -ErrorAction SilentlyContinue
    if ($errorFiles) {
        $errors = $errorFiles | Select-String -Pattern "For input string:" -NoEmphasis
        $count = ($errors | Measure-Object).Count
        Write-Host "  Nombre d'erreurs de parsing trouvées: $count`n" -ForegroundColor Red

        if ($count -gt 0) {
            Write-Host "  Erreurs détectées (uniques):" -ForegroundColor Red
            $errors | Select-Object -Unique -First 10 | ForEach-Object { Write-Host "    $_" }
        }
    }
} else {
    Write-Host "  ⚠️  Répertoire $ErrorLogDir non trouvé`n" -ForegroundColor Yellow
}

# === ÉTAPE 2: COMPILATION ===
Write-Host "`n🔨 ÉTAPE 2: RECOMPILATION DU SERVEUR" -ForegroundColor Yellow
Write-Host "Vérification de la syntaxe Java...`n" -ForegroundColor Gray

Push-Location $ServerRoot

Write-Host "  Exécution: .\gradlew.bat build -x test`n" -ForegroundColor Gray

$buildResult = & ".\gradlew.bat" build -x test 2>&1 | Out-String
$buildSuccess = $LASTEXITCODE -eq 0

if ($buildSuccess) {
    Write-Host "  ✅ Compilation réussie!`n" -ForegroundColor Green
    Write-Host "  JAR créé: build/libs/Server-1.0.0.jar`n" -ForegroundColor Green
} else {
    Write-Host "  ❌ Compilation échouée!`n" -ForegroundColor Red
    if ($buildResult -match "error") {
        Write-Host "  Erreurs détectées:" -ForegroundColor Red
        $buildResult -split "`n" | Where-Object { $_ -match "error|ERROR" } | ForEach-Object { Write-Host "    $_" }
    }
}

Pop-Location

# === ÉTAPE 3: INSTRUCTIONS SQL ===
Write-Host "`n💾 ÉTAPE 3: NETTOYAGE DE LA BASE DE DONNÉES" -ForegroundColor Yellow
Write-Host "Exécutez les commandes suivantes dans MySQL:`n" -ForegroundColor Gray

Write-Host "  Option A: Automatique avec le script SQL" -ForegroundColor Cyan
Write-Host "    mysql -u root -p dofus_game < SQL_FIX_MONSTERS.sql`n"

Write-Host "  Option B: Manuelle dans MySQL" -ForegroundColor Cyan
@"
    1. mysql -u root -p
    2. USE dofus_game;
    3. CREATE TABLE maps_backup AS SELECT * FROM maps;
    4. UPDATE maps SET monsters = '' WHERE monsters LIKE '%#%' OR monsters REGEXP '[0-9]{5},[0-9]{3}';
    5. SELECT COUNT(*) FROM maps WHERE monsters != '';
"@ | Write-Host

# === ÉTAPE 4: REDÉMARRAGE ===
Write-Host "`n🔄 ÉTAPE 4: REDÉMARRAGE DU SERVEUR" -ForegroundColor Yellow
@"
  1. Arrêtez le serveur (Ctrl+C ou fermez la fenêtre)
  2. Supprimez les vieux logs:
     Remove-Item 'Logs\Error\*' -Force
  3. Démarrez le serveur:
     .\Start-Server.bat
"@ | Write-Host -ForegroundColor Gray

# === ÉTAPE 5: VÉRIFICATION ===
Write-Host "`n✅ ÉTAPE 5: VÉRIFICATION DES RÉSULTATS" -ForegroundColor Yellow
Write-Host "Vérifiez que:`n" -ForegroundColor Gray

Write-Host "  ✓ Pas d'erreur 'NumberFormatException' dans les logs" -ForegroundColor Gray
Write-Host "  ✓ Les monstres spawn correctement sur les maps" -ForegroundColor Gray
Write-Host "  ✓ Les IA jouent normalement en combat`n" -ForegroundColor Gray

Write-Host "  Commande pour vérifier:" -ForegroundColor Cyan
Write-Host "    Get-Content 'Logs\server.log' -Tail 100 | Select-String 'NumberFormatException'`n"

# === RÉSUMÉ FINAL ===
Write-Host "`n═══════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "📖 RÉSUMÉ DES ACTIONS" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════`n" -ForegroundColor Cyan

if ($buildSuccess) {
    Write-Host "  [✅] Compilation du code réussie" -ForegroundColor Green
} else {
    Write-Host "  [❌] Compilation du code échouée" -ForegroundColor Red
}

Write-Host "  [⏳] En attente: Nettoyage de la base de données MySQL" -ForegroundColor Yellow
Write-Host "  [⏳] En attente: Redémarrage du serveur" -ForegroundColor Yellow

Write-Host "`n📝 Pour plus d'informations, consultez:" -ForegroundColor Cyan
Write-Host "  - GUIDE_CORRECTION_IA.md" -ForegroundColor White
Write-Host "  - RAPPORT_ANALYSE_IA.md" -ForegroundColor White
Write-Host "  - SQL_FIX_MONSTERS.sql" -ForegroundColor White

Write-Host "`n═══════════════════════════════════════════════════════`n" -ForegroundColor Cyan

