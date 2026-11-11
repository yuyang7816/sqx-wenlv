# 在已导入 sql/wenlv.sql 之后，按序导入 sql/migration 里的增量脚本（当前开源包没有该目录时可跳过）
# 用法：powershell -File lvyou-server/scripts/import-lvyou-migrations.ps1
# 环境变量：MYSQL_BIN（mysql.exe 路径）、MYSQL_USER、MYSQL_PASS、MYSQL_DB

param(
    [string]$MysqlBin = $env:MYSQL_BIN,
    [string]$Db = $(if ($env:MYSQL_DB) { $env:MYSQL_DB } else { "wenlv" }),
    [string]$User = $(if ($env:MYSQL_USER) { $env:MYSQL_USER } else { "root" }),
    [string]$Pass = $(if ($env:MYSQL_PASS) { $env:MYSQL_PASS } else { "hanstg" })
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$MigrationDir = Join-Path $Root "sql\migration"

if (-not $MysqlBin) {
    $candidates = @(
        "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
        "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe",
        "C:\xampp\mysql\bin\mysql.exe"
    )
    foreach ($c in $candidates) {
        if (Test-Path $c) { $MysqlBin = $c; break }
    }
}
if (-not $MysqlBin -or -not (Test-Path $MysqlBin)) {
    Write-Host "FAIL: mysql.exe not found. Set MYSQL_BIN or add mysql to PATH." -ForegroundColor Red
    exit 1
}

$files = Get-ChildItem -Path $MigrationDir -Filter "V*.sql" | Sort-Object Name
if ($files.Count -eq 0) {
    Write-Host "FAIL: no migration files in $MigrationDir" -ForegroundColor Red
    exit 1
}

Write-Host ("Import {0} migrations into `{1}` via {2}" -f $files.Count, $Db, $MysqlBin) -ForegroundColor Cyan
$fail = 0
foreach ($f in $files) {
    Write-Host ("-> " + $f.Name)
    $args = @("-u$User", "-p$Pass", "--default-character-set=utf8mb4", $Db)
    & $MysqlBin @args -e "source $($f.FullName -replace '\\','/')"
    if ($LASTEXITCODE -ne 0) {
        Write-Host ("FAIL: " + $f.Name) -ForegroundColor Red
        $fail++
        break
    }
}
if ($fail -gt 0) { exit 1 }
Write-Host "All migrations imported." -ForegroundColor Green
exit 0
