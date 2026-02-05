$extensions = @('.cs', '.js', '.json', '.md', '.txt')
$ignoreDirs = @('.git', 'Library', 'Temp', 'Build', 'obj', 'node_modules')

$sb = [System.Text.StringBuilder]::new()
[void]$sb.AppendLine("### 1. FILE STRUCTURE ###")

# Get file structure
function Get-Structure {
    param($path, $indent = 0)
    $items = Get-ChildItem -Path $path -ErrorAction SilentlyContinue
    foreach ($item in $items) {
        $skip = $false
        foreach ($ig in $ignoreDirs) {
            if ($item.Name -eq $ig) { $skip = $true; break }
        }
        if ($skip) { continue }
        
        $spaces = "    " * $indent
        if ($item.PSIsContainer) {
            [void]$sb.AppendLine("$spaces$($item.Name)/")
            Get-Structure -path $item.FullName -indent ($indent + 1)
        } else {
            [void]$sb.AppendLine("$spaces$($item.Name)")
        }
    }
}

Get-Structure -path "."

[void]$sb.AppendLine("")
[void]$sb.AppendLine("### 2. FILE CONTENTS ###")

# Get file contents
Get-ChildItem -Recurse -File -ErrorAction SilentlyContinue | ForEach-Object {
    $skip = $false
    foreach ($ig in $ignoreDirs) {
        if ($_.FullName -like "*\$ig\*" -or $_.FullName -like "*\$ig") { $skip = $true; break }
    }
    if ($skip) { return }
    
    $matchExt = $false
    foreach ($ext in $extensions) {
        if ($_.Name.EndsWith($ext)) { $matchExt = $true; break }
    }
    if (-not $matchExt) { return }
    
    [void]$sb.AppendLine("")
    [void]$sb.AppendLine("--- START OF $($_.Name) ---")
    try {
        $content = Get-Content $_.FullName -Raw -Encoding UTF8 -ErrorAction SilentlyContinue
        [void]$sb.Append($content)
    } catch {
        [void]$sb.AppendLine("[Error reading file]")
    }
    [void]$sb.AppendLine("--- END OF $($_.Name) ---")
}

$sb.ToString() | Out-File -FilePath "PROJECT_CONTEXT.txt" -Encoding utf8
Write-Host "Done! PROJECT_CONTEXT.txt created."
