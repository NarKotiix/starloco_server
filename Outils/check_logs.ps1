# Script pour vérifier les logs AIProfiling

Write-Host "=== Vérification du dossier AIProfiling ===" -ForegroundColor Cyan
$aiprofPath = "H:\server_dofus\Starloco-Fun\Server\Logs\AIProfiling"
if (Test-Path $aiprofPath) {
    Write-Host "✓ Dossier existe: $aiprofPath" -ForegroundColor Green
    $files = Get-ChildItem $aiprofPath -File
    if ($files.Count -gt 0) {
        Write-Host "✓ Fichiers trouvés:" -ForegroundColor Green
        $files | ForEach-Object { Write-Host "  - $($_.Name) ($($_.Length) bytes)" }
    } else {
        Write-Host "✗ Aucun fichier de log trouvé" -ForegroundColor Yellow
    }
} else {
    Write-Host "✗ Dossier n'existe pas" -ForegroundColor Red
}

Write-Host "`n=== Vérification logback.xml ===" -ForegroundColor Cyan
$logbackPath = "H:\server_dofus\Starloco-Fun\Server\src\logback.xml"
if (Test-Path $logbackPath) {
    Write-Host "✓ logback.xml existe" -ForegroundColor Green
    $content = Get-Content $logbackPath
    if ($content -match "ai.profiling") {
        Write-Host "✓ Logger 'ai.profiling' configuré dans logback.xml" -ForegroundColor Green
    }
    if ($content -match "AI_PROFILING_FILE") {
        Write-Host "✓ Appender 'AI_PROFILING_FILE' configuré" -ForegroundColor Green
    }
}

Write-Host "`n=== Vérification AbstractIA.java ===" -ForegroundColor Cyan
$iaPath = "H:\server_dofus\Starloco-Fun\Server\src\org\starloco\locos\fight\ia\AbstractIA.java"
if (Test-Path $iaPath) {
    $content = Get-Content $iaPath
    if ($content -match "AIPROF_LOGGER") {
        Write-Host "✓ Logger AIPROF_LOGGER utilisé dans AbstractIA" -ForegroundColor Green
        $count = ([regex]::Matches($content, "AIPROF_LOGGER")).Count
        Write-Host "  → $count occurrences de AIPROF_LOGGER trouvées" -ForegroundColor Green
    }
}

Write-Host "`n=== Fichiers de logs récents ===" -ForegroundColor Cyan
Get-ChildItem "H:\server_dofus\Starloco-Fun\Server\Logs\*.log" | Sort-Object LastWriteTime -Descending | Select-Object -First 5 | Format-Table Name, @{Name="LastWrite";Expression={$_.LastWriteTime}}, @{Name="Size";Expression={"$([math]::Round($_.Length/1MB, 2)) MB"}} | Out-String

