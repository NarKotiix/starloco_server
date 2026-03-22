[CmdletBinding()]
param(
    [string]$OldDump = (Join-Path $PSScriptRoot 'maps_ancien.sql'),
    [string]$NewDump = (Join-Path $PSScriptRoot 'maps_new.sql'),
    [string]$MapDataJavaPath = (Join-Path (Split-Path $PSScriptRoot -Parent | Split-Path -Parent) 'src\org\starloco\locos\database\dynamics\data\MapData.java')
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$DefaultDumpOrder = @(
    'id', 'date', 'width', 'heigth', 'places', 'key', 'mapData', 'monsters',
    'capabilities', 'mappos', 'numgroup', 'minSize', 'fixSize', 'maxSize',
    'forbidden', 'sniffed'
)

$RuntimeReadOrder = @(
    'id', 'date', 'width', 'heigth', 'key', 'places', 'mapData', 'monsters',
    'mappos', 'numgroup', 'fixSize', 'minSize', 'maxSize', 'forbidden', 'sniffed'
)

function Get-CreateTableColumns {
    param([string]$Path)

    $reader = [System.IO.StreamReader]::new($Path)
    try {
        $inside = $false
        $columns = New-Object System.Collections.Generic.List[string]
        while (($line = $reader.ReadLine()) -ne $null) {
            if (-not $inside) {
                if ($line -match 'CREATE TABLE\s+`?maps`?') {
                    $inside = $true
                }
                continue
            }

            if ($line -match '^\)') {
                break
            }

            if ($line -match '^\s*`([^`]+)`') {
                [void]$columns.Add($matches[1])
            }
        }

        if ($columns.Count -gt 0) {
            return ,$columns.ToArray()
        }

        return @()
    }
    finally {
        $reader.Close()
    }
}

function Get-FirstInsertTupleText {
    param([string]$Path)

    $reader = [System.IO.StreamReader]::new($Path)
    try {
        while (($line = $reader.ReadLine()) -ne $null) {
            if ($line -match 'INSERT INTO\s+`?maps`?(?:\s*\([^)]*\))?\s+VALUES\s*\(') {
                $builder = New-Object System.Text.StringBuilder
                [void]$builder.AppendLine($line)
                if ($line -match '\);\s*$') {
                    return $builder.ToString()
                }

                while (($next = $reader.ReadLine()) -ne $null) {
                    [void]$builder.AppendLine($next)
                    if ($next -match '\);\s*$') {
                        break
                    }
                }
                return $builder.ToString()
            }
        }

        throw "Aucun INSERT INTO maps trouvé dans '$Path'."
    }
    finally {
        $reader.Close()
    }
}

function Split-SqlTuple {
    param([string]$TupleText)

    $values = New-Object System.Collections.Generic.List[string]
    $current = New-Object System.Text.StringBuilder
    $inString = $false
    $depth = 0

    for ($i = 0; $i -lt $TupleText.Length; $i++) {
        $ch = $TupleText[$i]
        $next = if ($i + 1 -lt $TupleText.Length) { $TupleText[$i + 1] } else { [char]0 }

        if ($inString) {
            [void]$current.Append($ch)
            if ($ch -eq "'") {
                if ($next -eq "'") {
                    [void]$current.Append($next)
                    $i++
                }
                else {
                    $inString = $false
                }
            }
            continue
        }

        switch ($ch) {
            "'" {
                $inString = $true
                [void]$current.Append($ch)
            }
            '(' {
                if ($depth -gt 0) {
                    [void]$current.Append($ch)
                }
                $depth++
            }
            ')' {
                $depth--
                if ($depth -eq 0) {
                    $value = $current.ToString().Trim()
                    if ($value.Length -gt 0 -or $values.Count -gt 0) {
                        [void]$values.Add($value)
                    }
                    return ,$values.ToArray()
                }
                [void]$current.Append($ch)
            }
            ',' {
                if ($depth -eq 1) {
                    [void]$values.Add($current.ToString().Trim())
                    $null = $current.Clear()
                }
                elseif ($depth -gt 1) {
                    [void]$current.Append($ch)
                }
            }
            default {
                if ($depth -gt 0) {
                    [void]$current.Append($ch)
                }
            }
        }
    }

    throw 'Tuple SQL incomplet ou non parsable.'
}

function Convert-SqlLiteralToValue {
    param([string]$Literal)

    $trimmed = $Literal.Trim()
    if ($trimmed -eq 'NULL') {
        return $null
    }

    if ($trimmed.StartsWith("'", [System.StringComparison]::Ordinal) -and $trimmed.EndsWith("'", [System.StringComparison]::Ordinal)) {
        $inner = $trimmed.Substring(1, $trimmed.Length - 2)
        return $inner.Replace("''", "'")
    }

    return $trimmed
}

function Test-HexLike {
    param([AllowNull()][string]$Value)
    if ([string]::IsNullOrEmpty($Value)) { return $false }
    return $Value -match '^[0-9A-Fa-f]+$'
}

function Test-MapPosLike {
    param([AllowNull()][string]$Value)
    if ([string]::IsNullOrEmpty($Value)) { return $false }
    return $Value -match '^-?\d+,-?\d+,\d+$'
}

function Get-RuntimeColumnsFromMapData {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        return @()
    }

    $content = Get-Content -LiteralPath $Path -Raw
    $matches = [regex]::Matches($content, 'get(?:Short|Byte|String|Int)\("([^"]+)"\)')
    $ordered = New-Object System.Collections.Generic.List[string]
    foreach ($m in $matches) {
        $value = $m.Groups[1].Value
        if (-not $ordered.Contains($value)) {
            [void]$ordered.Add($value)
        }
    }
    return ,$ordered.ToArray()
}

function Get-DumpSummary {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        throw "Fichier introuvable : $Path"
    }

    $columnsFromDdl = @(Get-CreateTableColumns -Path $Path)
    $insertText = Get-FirstInsertTupleText -Path $Path
    $tuple = Split-SqlTuple -TupleText $insertText
    $values = @($tuple | ForEach-Object { Convert-SqlLiteralToValue $_ })

    $isLikelyPlacesThenKey = $false
    if ($values.Count -ge 6) {
        $isLikelyPlacesThenKey = (-not (Test-HexLike $values[4])) -and (Test-HexLike $values[5])
    }

    $isLikelyNumMinFixMax = $false
    if ($values.Count -ge 14) {
        $isLikelyNumMinFixMax = ($values[10] -match '^-?\d+$') -and ($values[11] -match '^-?\d+$') -and ($values[12] -match '^-?\d+$') -and ($values[13] -match '^-?\d+$')
    }

    $inferredOrder = if (@($columnsFromDdl).Count -gt 0) {
        @($columnsFromDdl)
    }
    elseif ($values.Count -eq 16 -and $isLikelyPlacesThenKey -and (Test-MapPosLike $values[9])) {
        @($DefaultDumpOrder)
    }
    else {
        @()
    }

    [pscustomobject]@{
        Path                  = $Path
        FileName              = [System.IO.Path]::GetFileName($Path)
        HasCreateTable        = (@($columnsFromDdl).Count -gt 0)
        DdlColumns            = @($columnsFromDdl)
        FirstInsertValueCount = $values.Count
        FirstValues           = $values
        InferredOrder         = @($inferredOrder)
        LikelyPlacesThenKey   = $isLikelyPlacesThenKey
        LikelyNumMinFixMax    = $isLikelyNumMinFixMax
        LikelyMapPos          = if ($values.Count -ge 10) { Test-MapPosLike $values[9] } else { $false }
    }
}

function Format-ColumnList {
    param([string[]]$Columns)
    if (-not $Columns -or $Columns.Count -eq 0) {
        return '(indéterminé)'
    }
    return ($Columns -join ', ')
}

$oldSummary = Get-DumpSummary -Path $OldDump
$newSummary = Get-DumpSummary -Path $NewDump
$runtimeColumns = @(Get-RuntimeColumnsFromMapData -Path $MapDataJavaPath | Where-Object { $_ -in $RuntimeReadOrder })
if (-not $runtimeColumns -or @($runtimeColumns).Count -ne @($RuntimeReadOrder).Count) {
    $runtimeColumns = @($RuntimeReadOrder)
}

$allDumpColumns = @($oldSummary.InferredOrder)
if (-not $allDumpColumns -or $allDumpColumns.Count -eq 0) {
    $allDumpColumns = @($newSummary.InferredOrder)
}

$runtimeOnly = @($runtimeColumns | Where-Object { $_ -notin $allDumpColumns })
$dumpOnly = @($allDumpColumns | Where-Object { $_ -notin $runtimeColumns })
$sameOrder = ((@($oldSummary.InferredOrder) -join '|') -eq (@($newSummary.InferredOrder) -join '|')) -and @($oldSummary.InferredOrder).Count -gt 0

Write-Output '=== Diagnostic table maps ==='
Write-Output ''
Write-Output ("Ancien dump : {0}" -f $oldSummary.FileName)
Write-Output ("- CREATE TABLE présent : {0}" -f $(if ($oldSummary.HasCreateTable) { 'oui' } else { 'non (data-only)' }))
Write-Output ("- Nb valeurs du premier INSERT : {0}" -f $oldSummary.FirstInsertValueCount)
Write-Output ("- Ordre inféré : {0}" -f (Format-ColumnList $oldSummary.InferredOrder))
Write-Output ("- Heuristique places/key : {0}" -f $(if ($oldSummary.LikelyPlacesThenKey) { 'col5=places, col6=key' } else { 'non déterminée' }))
Write-Output ("- Heuristique queue numgroup/minSize/fixSize/maxSize : {0}" -f $(if ($oldSummary.LikelyNumMinFixMax) { 'compatible' } else { 'non déterminée' }))
Write-Output ''
Write-Output ("Nouveau dump : {0}" -f $newSummary.FileName)
Write-Output ("- CREATE TABLE présent : {0}" -f $(if ($newSummary.HasCreateTable) { 'oui' } else { 'non' }))
Write-Output ("- Nb valeurs du premier INSERT : {0}" -f $newSummary.FirstInsertValueCount)
Write-Output ("- Ordre DDL / inféré : {0}" -f (Format-ColumnList $newSummary.InferredOrder))
Write-Output ("- Heuristique places/key : {0}" -f $(if ($newSummary.LikelyPlacesThenKey) { 'col5=places, col6=key' } else { 'non déterminée' }))
Write-Output ("- Heuristique queue numgroup/minSize/fixSize/maxSize : {0}" -f $(if ($newSummary.LikelyNumMinFixMax) { 'compatible' } else { 'non déterminée' }))
Write-Output ''
Write-Output 'Contrat runtime Java (`MapData.java`) :'
Write-Output ("- Colonnes lues par nom : {0}" -f (Format-ColumnList $runtimeColumns))
Write-Output ''
Write-Output 'Comparaison :'
Write-Output ("- Ancien vs nouveau dump : {0}" -f $(if ($sameOrder) { 'même structure logique détectée' } else { 'écart possible ou ordre indéterminé' }))
Write-Output ("- Colonnes présentes dans le dump mais non lues par le runtime : {0}" -f $(if ($dumpOnly.Count -gt 0) { $dumpOnly -join ', ' } else { '(aucune)' }))
Write-Output ("- Colonnes lues par le runtime mais absentes de l ordre du dump : {0}" -f $(if ($runtimeOnly.Count -gt 0) { $runtimeOnly -join ', ' } else { '(aucune)' }))
Write-Output ''

if ($sameOrder -and @($dumpOnly).Count -eq 1 -and $dumpOnly[0] -eq 'capabilities') {
    Write-Output 'Conclusion :'
    Write-Output '- Le dump ancien et le dump récent semblent partager la même structure logique de `maps`.'
    Write-Output '- Le vrai changement visible est surtout que `maps_new.sql` embarque le `CREATE TABLE`, alors que `maps_ancien.sql` est data-only.'
    Write-Output '- La colonne `capabilities` existe dans le dump récent mais n est pas consommée par `MapData.java`.'
    Write-Output '- Le risque principal n est donc pas un nouveau schéma métier, mais un import dangereux avec `INSERT INTO maps VALUES (...)` si la table cible locale n a pas exactement le même ordre de colonnes.'
}
else {
    Write-Output 'Conclusion :'
    Write-Output '- Un écart potentiel de structure ou d ordre existe, ou n a pas pu être prouvé automatiquement.'
    Write-Output '- Utilise impérativement des `INSERT INTO maps (<colonnes>) VALUES (...)` explicites pour éviter tout décalage silencieux.'
}

Write-Output ''
Write-Output 'Ordre canonique recommandé pour les dumps / imports explicites :'
Write-Output ("- {0}" -f ($DefaultDumpOrder -join ', '))



