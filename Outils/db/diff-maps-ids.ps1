[CmdletBinding()]
param(
    [string]$OldDump = (Join-Path $PSScriptRoot 'maps_ancien.sql'),
    [string]$NewDump = (Join-Path $PSScriptRoot 'maps_new.sql'),
    [string]$MissingIdsOutput = (Join-Path $PSScriptRoot 'maps_missing_in_new_ids.txt'),
    [string]$MissingSqlOutput = (Join-Path $PSScriptRoot 'maps_missing_in_new_from_ancien.sql')
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-MapIdSet {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        throw "Fichier introuvable : $Path"
    }

    $ids = New-Object 'System.Collections.Generic.HashSet[int]'
    $reader = [System.IO.StreamReader]::new($Path)
    try {
        while (($line = $reader.ReadLine()) -ne $null) {
            if ($line -match '^INSERT INTO `maps` VALUES \((\d+),') {
                [void]$ids.Add([int]$matches[1])
            }
        }
    }
    finally {
        $reader.Close()
    }

    return $ids
}

function Get-MatchingInsertLines {
    param(
        [string]$Path,
        [System.Collections.Generic.HashSet[int]]$IdsToKeep
    )

    $lines = New-Object System.Collections.Generic.List[string]
    $reader = [System.IO.StreamReader]::new($Path)
    try {
        while (($line = $reader.ReadLine()) -ne $null) {
            if ($line -match '^INSERT INTO `maps` VALUES \((\d+),') {
                $id = [int]$matches[1]
                if ($IdsToKeep.Contains($id)) {
                    [void]$lines.Add($line)
                }
            }
        }
    }
    finally {
        $reader.Close()
    }

    return $lines
}

$oldIds = Get-MapIdSet -Path $OldDump
$newIds = Get-MapIdSet -Path $NewDump

$missingInNew = @($oldIds | Where-Object { -not $newIds.Contains($_) } | Sort-Object)
$missingInOld = @($newIds | Where-Object { -not $oldIds.Contains($_) } | Sort-Object)

$missingIdLines = @(
    '# IDs présents dans maps_ancien.sql mais absents de maps_new.sql',
    ('# Count = ' + $missingInNew.Count),
    ($missingInNew -join ',')
)
Set-Content -LiteralPath $MissingIdsOutput -Value $missingIdLines -Encoding UTF8

$idsToKeep = New-Object 'System.Collections.Generic.HashSet[int]'
foreach ($id in $missingInNew) {
    [void]$idsToKeep.Add([int]$id)
}

$matchingLines = Get-MatchingInsertLines -Path $OldDump -IdsToKeep $idsToKeep
$sqlContent = @(
    '-- INSERT de maps présentes dans maps_ancien.sql mais absentes de maps_new.sql',
    ('-- Count = ' + $matchingLines.Count),
    ''
) + $matchingLines
Set-Content -LiteralPath $MissingSqlOutput -Value $sqlContent -Encoding UTF8

Write-Output '=== Diff maps ancien -> new ==='
Write-Output ("Ancien total : {0}" -f $oldIds.Count)
Write-Output ("New total    : {0}" -f $newIds.Count)
Write-Output ("Manquantes dans new : {0}" -f $missingInNew.Count)
Write-Output ("En trop dans new    : {0}" -f $missingInOld.Count)
Write-Output ''
Write-Output 'IDs manquants dans maps_new :'
Write-Output ($missingInNew -join ',')
Write-Output ''
Write-Output ("Liste écrite dans : {0}" -f $MissingIdsOutput)
Write-Output ("INSERT extraits dans : {0}" -f $MissingSqlOutput)

