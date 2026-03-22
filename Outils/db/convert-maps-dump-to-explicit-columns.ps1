[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$InputPath,

    [Parameter(Mandatory = $true)]
    [string]$OutputPath,

    [string]$TableName = 'maps',

    [string[]]$Columns = @(
        'id', 'date', 'width', 'heigth', 'places', 'key', 'mapData', 'monsters',
        'capabilities', 'mappos', 'numgroup', 'minSize', 'fixSize', 'maxSize',
        'forbidden', 'sniffed'
    )
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath $InputPath)) {
    throw "Fichier source introuvable : $InputPath"
}

$resolvedInput = (Resolve-Path -LiteralPath $InputPath).Path
$resolvedOutputDirectory = Split-Path -Path $OutputPath -Parent
if ([string]::IsNullOrWhiteSpace($resolvedOutputDirectory)) {
    $resolvedOutputDirectory = (Get-Location).Path
}
if (-not (Test-Path -LiteralPath $resolvedOutputDirectory)) {
    New-Item -ItemType Directory -Path $resolvedOutputDirectory -Force | Out-Null
}

$escapedTableName = [regex]::Escape($TableName)
$columnSql = ($Columns | ForEach-Object { '`' + $_ + '`' }) -join ', '
$replacement = 'INSERT INTO `' + $TableName + '` (' + $columnSql + ') VALUES'
$pattern = '(?im)INSERT\s+INTO\s+`?' + $escapedTableName + '`?\s+VALUES'

$reader = [System.IO.StreamReader]::new($resolvedInput)
try {
    $content = $reader.ReadToEnd()
}
finally {
    $reader.Close()
}

$alreadyExplicitPattern = '(?im)INSERT\s+INTO\s+`?' + $escapedTableName + '`?\s*\('
$explicitCount = ([regex]::Matches($content, $alreadyExplicitPattern)).Count
$converted = [regex]::Replace($content, $pattern, $replacement)
$replacementCount = ([regex]::Matches($content, $pattern)).Count

[System.IO.File]::WriteAllText($OutputPath, $converted, [System.Text.Encoding]::UTF8)

Write-Output '=== Conversion terminée ==='
Write-Output ("Source               : {0}" -f $resolvedInput)
Write-Output ("Destination          : {0}" -f $OutputPath)
Write-Output ("Table ciblée         : {0}" -f $TableName)
Write-Output ("Colonnes explicites  : {0}" -f ($Columns -join ', '))
Write-Output ("INSERT déjà explicites détectés : {0}" -f $explicitCount)
Write-Output ("INSERT convertis     : {0}" -f $replacementCount)

if ($replacementCount -eq 0 -and $explicitCount -eq 0) {
    Write-Warning 'Aucun INSERT INTO maps VALUES trouvé. Vérifie le nom de table ou le fichier source.'
}
elseif ($replacementCount -eq 0 -and $explicitCount -gt 0) {
    Write-Output 'Le fichier semble déjà utiliser des INSERT avec liste de colonnes.'
}
else {
    Write-Output 'Le dump converti peut maintenant être importé sans dépendre de l ordre physique des colonnes de la table cible.'
}


