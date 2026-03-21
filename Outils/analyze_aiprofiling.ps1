#!/usr/bin/env powershell
<#
.SYNOPSIS
Script d'analyse des logs AIProfiling après un test de combat

.DESCRIPTION
Affiche les statistiques et résultats du test AIProfiling

.USAGE
.\analyze_aiprofiling.ps1
#>

param(
    [string]$LogFile = "Logs\AIProfiling\ai_profiling.log"
)

Write-Host "
╔══════════════════════════════════════════════════════════════════════╗
║        ANALYSE DES LOGS AI PROFILING                                 ║
║        Vérification de la correction du deadlock                      ║
╚══════════════════════════════════════════════════════════════════════╝
" -ForegroundColor Cyan

# Vérifier que le fichier existe
if (-not (Test-Path $LogFile)) {
    Write-Host "❌ ERREUR: Fichier $LogFile non trouvé!" -ForegroundColor Red
    Write-Host "   Assurez-vous que:" -ForegroundColor Yellow
    Write-Host "   1. Le serveur a été lancé" -ForegroundColor Yellow
    Write-Host "   2. Un combat avec IA a été exécuté" -ForegroundColor Yellow
    Write-Host "   3. Le serveur a été fermé proprement" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Fichier trouvé: $LogFile`n" -ForegroundColor Green

# Récupérer les logs
$content = Get-Content $LogFile
$lines = $content.Count

Write-Host "📊 STATISTIQUES GÉNÉRALES:" -ForegroundColor Cyan
Write-Host "   Total de lignes: $lines`n" -ForegroundColor White

# Compter les types de messages
$endTurnExit = ($content | Select-String "endTurn exit" -AllMatches).Matches.Count
$endTurnPolling = ($content | Select-String "endTurn polling" -AllMatches).Matches.Count
$endTurnStuck = ($content | Select-String "endTurn stuck" -AllMatches).Matches.Count
$schedulerBlocked = ($content | Select-String "scheduler blocked" -AllMatches).Matches.Count
$schedulerStuck = ($content | Select-String "scheduler stuck" -AllMatches).Matches.Count
$schedulerWait = ($content | Select-String "scheduler wait" -AllMatches).Matches.Count

Write-Host "📈 COMPTEURS:" -ForegroundColor Cyan
Write-Host "   ✅ endTurn exit (turn changed): $endTurnExit" -ForegroundColor Green
Write-Host "   📊 endTurn polling (boucle d'attente): $endTurnPolling" -ForegroundColor White
Write-Host "   ⚠️  endTurn stuck (timeout): $endTurnStuck" -ForegroundColor Yellow
Write-Host "   📊 scheduler blocked (curAction): $schedulerBlocked" -ForegroundColor White
Write-Host "   ⚠️  scheduler stuck (timeout): $schedulerStuck" -ForegroundColor Yellow
Write-Host "   📊 scheduler wait: $schedulerWait" -ForegroundColor White
Write-Host ""

# Analyser la santé globale
Write-Host "🔍 ANALYSE DE SANTÉ:" -ForegroundColor Cyan
$healthy = $true

if ($endTurnStuck -gt 0) {
    Write-Host "   ❌ PROBLÈME DÉTECTÉ: $endTurnStuck timeout 'endTurn stuck'" -ForegroundColor Red
    Write-Host "      → Le deadlock du endTurn() est survenu!" -ForegroundColor Red
    $healthy = $false
} else {
    Write-Host "   ✅ Aucun timeout 'endTurn stuck'" -ForegroundColor Green
}

if ($schedulerStuck -gt 0) {
    Write-Host "   ❌ PROBLÈME DÉTECTÉ: $schedulerStuck timeout 'scheduler stuck'" -ForegroundColor Red
    Write-Host "      → Le scheduler a été bloqué trop longtemps!" -ForegroundColor Red
    $healthy = $false
} else {
    Write-Host "   ✅ Aucun timeout 'scheduler stuck'" -ForegroundColor Green
}

if ($endTurnExit -gt 0) {
    Write-Host "   ✅ Correction active: $endTurnExit sorties rapides détectées" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  Aucune sortie 'turn changed' détectée (peut être normal si pas d'invocations)" -ForegroundColor Yellow
}

Write-Host ""

if ($healthy) {
    Write-Host "╔════════════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
    Write-Host "║ ✅ RÉSULTAT: SYSTÈME SAIN - Pas de deadlock détecté!                  ║" -ForegroundColor Green
    Write-Host "╚════════════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
} else {
    Write-Host "╔════════════════════════════════════════════════════════════════════════╗" -ForegroundColor Red
    Write-Host "║ ❌ RÉSULTAT: PROBLÈMES DÉTECTÉS - Voir les erreurs ci-dessus          ║" -ForegroundColor Red
    Write-Host "╚════════════════════════════════════════════════════════════════════════╝" -ForegroundColor Red
}

Write-Host "`n📋 EXTRAIT DES LOGS (dernières 30 lignes):`n" -ForegroundColor Cyan
Write-Host "───────────────────────────────────────────────────────────────────────────" -ForegroundColor Gray

$content | Select-Object -Last 30 | ForEach-Object {
    if ($_ -match "endTurn exit|turn changed") {
        Write-Host $_ -ForegroundColor Green
    } elseif ($_ -match "stuck") {
        Write-Host $_ -ForegroundColor Red
    } elseif ($_ -match "WARN") {
        Write-Host $_ -ForegroundColor Yellow
    } else {
        Write-Host $_
    }
}

Write-Host "`n───────────────────────────────────────────────────────────────────────────" -ForegroundColor Gray

Write-Host "`n💡 INTERPRÉTATION:" -ForegroundColor Cyan
Write-Host "   • 'endTurn exit' = ✅ Correction fonctionne (sortie rapide)" -ForegroundColor White
Write-Host "   • 'endTurn polling' = 📊 Métrique normale (attente de tour)" -ForegroundColor White
Write-Host "   • 'endTurn stuck' = ❌ Problème! Deadlock détecté" -ForegroundColor Red
Write-Host "   • 'scheduler blocked' = 📊 Métrique normale (isCurAction bloqué)" -ForegroundColor White
Write-Host "   • 'scheduler stuck' = ❌ Problème! Action trop longue" -ForegroundColor Red
Write-Host ""

