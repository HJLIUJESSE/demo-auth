param(
  [Parameter(ValueFromRemainingArguments = $true)]
  [string[]]$Rest
)

$npm = "C:\Program Files\nodejs\npm.cmd"
if (-not (Test-Path $npm)) {
  Write-Error "npm not found at $npm"
  exit 1
}

Push-Location $PSScriptRoot
try {
  if (-not (Test-Path "node_modules")) {
    & $npm install
  }
  if ($Rest.Length -gt 0) {
    & $npm run dev -- $Rest
  } else {
    & $npm run dev
  }
}
finally {
  Pop-Location
}

