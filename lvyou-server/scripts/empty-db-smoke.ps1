# 空库冒烟：import → 启动 → 登录 → 我的页接口
# 前置：Redis(6379) + MySQL(rongeyou V000+全量 migration) + server:9135
# 用法：powershell -File lvyou-server/scripts/empty-db-smoke.ps1
# 登录：$env:QA_APP_PHONE + $env:QA_APP_CODE（先调 sendPhoneCode 或 Redis 已有验证码）
# 输出：docs/project-extension/empty-db-smoke-run.json（本次执行快照）

param(
    [string]$Base = "http://127.0.0.1:9135/sqx_fast",
    [string]$AppPhone = $env:QA_APP_PHONE,
    [string]$AppCode = $env:QA_APP_CODE,
    [string]$OutJson = $(Join-Path (Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)) "..\docs\project-extension\empty-db-smoke-run.json")
)

$ErrorActionPreference = "Continue"
$executedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ssK")
$results = @()
$failures = @()
$bootSmokeSkipped = $false
$bootSkipReason = $null

function Write-Step($msg) { Write-Host ("`n==> " + $msg) -ForegroundColor Cyan }

function Invoke-Api {
    param(
        [string]$Method = "GET",
        [string]$Path,
        [hashtable]$Headers = @{},
        $Body = $null,
        [int[]]$ExpectCodes = @(200),
        [switch]$AllowBizCode
    )
    $url = $Base + $Path
    $params = @{
        Uri             = $url
        Method          = $Method
        UseBasicParsing = $true
        TimeoutSec      = 20
        Headers         = $Headers
    }
    if ($Body -ne $null) {
        if ($Body -is [string]) {
            $params.Body = $Body
            $params.ContentType = "application/json"
        } else {
            $params.Body = ($Body | ConvertTo-Json -Depth 6 -Compress)
            $params.ContentType = "application/json"
        }
    }
    $status = 0
    $json = $null
    $raw = $null
    try {
        $resp = Invoke-WebRequest @params
        $status = [int]$resp.StatusCode
        $raw = $resp.Content
    } catch {
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode.value__
            try {
                $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                $raw = $reader.ReadToEnd()
            } catch { $raw = $_.Exception.Message }
        } else {
            $raw = $_.Exception.Message
        }
    }
    if ($raw) {
        try { $json = $raw | ConvertFrom-Json } catch { $json = $null }
    }
    $ok = ($ExpectCodes -contains $status)
    if ($json -and $json.PSObject.Properties.Name -contains "code") {
        if ($json.code -ne 0 -and -not $AllowBizCode) {
            if ($ok) { $ok = $false }
        }
    }
    if (-not $ok) {
        $script:failures += "$Method $Path status=$status body=$raw"
    }
    $script:results += [PSCustomObject]@{
        method = $Method
        path   = $Path
        status = $status
        code   = $(if ($json) { $json.code } else { $null })
        ok     = $ok
    }
    $label = $(if ($ok) { "OK" } else { "FAIL" })
    Write-Host ("[{0}] {1} {2}" -f $label, $Method, $Path)
    return [PSCustomObject]@{ Status = $status; Json = $json; Raw = $raw; Ok = $ok }
}

Write-Step "0. server health"
$ping = Invoke-Api -Method GET -Path "/app/config/info"
if (-not $ping.Ok) {
    $bootSmokeSkipped = $true
    $bootSkipReason = "server unreachable at $Base (need Redis 6379 + MySQL rongeyou + migration imported)"
    Write-Host $bootSkipReason -ForegroundColor Yellow
} else {

Write-Step "1. public read APIs (empty-db-core)"
$publicEndpoints = @(
    @{ Method = "POST"; Path = "/app/base/banner/page"; Body = '{"page":1,"limit":10}' },
    @{ Method = "GET";  Path = "/app/agreement/user" },
    @{ Method = "GET";  Path = "/app/home/recommend" },
    @{ Method = "GET";  Path = "/app/notice/marquee" },
    @{ Method = "GET";  Path = "/app/destination/list" },
    @{ Method = "GET";  Path = "/app/scenic/list?page=1&limit=10" },
    @{ Method = "GET";  Path = "/app/help/faq" },
    @{ Method = "GET";  Path = "/app/base/user/info"; ExpectCodes = @(401) }
)
foreach ($ep in $publicEndpoints) {
    $expect = if ($ep.ExpectCodes) { $ep.ExpectCodes } else { @(200) }
    if ($ep.Body) {
        Invoke-Api -Method $ep.Method -Path $ep.Path -Body $ep.Body -ExpectCodes $expect | Out-Null
    } else {
        Invoke-Api -Method $ep.Method -Path $ep.Path -ExpectCodes $expect | Out-Null
    }
}

Write-Step "2. App JWT login (phoneCodeLogin)"
$appToken = $null
$userId = $null
if ($AppPhone -and $AppCode) {
    $login = Invoke-Api -Method POST -Path "/app/base/auth/phoneCodeLogin" -Body @{
        phoneNo    = $AppPhone
        verifyCode = $AppCode
    }
    if ($login.Json -and $login.Json.data -and $login.Json.data.token) {
        $appToken = $login.Json.data.token
        if ($login.Json.data.user -and $login.Json.data.user.id) {
            $userId = $login.Json.data.user.id
        }
    }
}
if (-not $appToken) {
    $failures += "App JWT missing - set QA_APP_PHONE + QA_APP_CODE (sendPhoneCode first)"
    Write-Host "WARN: skip authenticated my-page APIs" -ForegroundColor Yellow
} else {
    $hdr = @{ token = $appToken }

    Write-Step "3. me + my-page summary APIs"
    $myApis = @(
        @{ Method = "GET"; Path = "/app/base/user/info" },
        @{ Method = "GET"; Path = "/app/messages/unread-count" },
        @{ Method = "GET"; Path = "/app/coupon/count" },
        @{ Method = "GET"; Path = "/app/reservation/canteen/pending-count" },
        @{ Method = "GET"; Path = "/app/merchant/status" },
        @{ Method = "GET"; Path = "/app/favorite/list?page=1&limit=1" },
        @{ Method = "GET"; Path = "/app/wallet/summary" },
        @{ Method = "GET"; Path = "/app/invite/summary" },
        @{ Method = "GET"; Path = "/app/invite/commission/summary" }
    )
    foreach ($ep in $myApis) {
        Invoke-Api -Method $ep.Method -Path $ep.Path -Headers $hdr | Out-Null
    }

    Write-Step "4. register ensure wallet/commission (read-back)"
    $wallet = Invoke-Api -Method GET -Path "/app/wallet/summary" -Headers $hdr
    if ($wallet.Ok -and $wallet.Json -and $wallet.Json.data) {
        $bal = $wallet.Json.data.balance
        Write-Host ("wallet ensure OK balance={0}" -f $bal) -ForegroundColor Green
    }
    $commission = Invoke-Api -Method GET -Path "/app/invite/commission/summary" -Headers $hdr
    if ($commission.Ok -and $commission.Json -and $commission.Json.data) {
        Write-Host ("commission summary OK total={0}" -f $commission.Json.data.total) -ForegroundColor Green
    }
}

}

$failCount = @($results | Where-Object { -not $_.ok }).Count
$passed = (-not $bootSmokeSkipped) -and ($failCount -eq 0)

$snapshot = @{
    taskId           = "page-ba55df05-ext-empty-db-smoke"
    script           = "lvyou-server/scripts/empty-db-smoke.ps1"
    baseUrl          = $Base
    executedAt       = $executedAt
    bootSmokeSkipped = $bootSmokeSkipped
    bootSkipReason   = $bootSkipReason
    passed           = $passed
    total            = $results.Count
    fail             = $failCount
    loginApi         = "POST /app/base/auth/phoneCodeLogin"
    meApi            = "GET /app/base/user/info"
    importCmd        = "powershell -File lvyou-server/scripts/import-lvyou-migrations.ps1"
    baselineNote     = "V000=既有 sqx 平台库 rongeyou；文旅增量见 sql/migration/V*.sql"
    endpoints        = @($results)
    failures         = @($failures)
}
$snapshot | ConvertTo-Json -Depth 6 | Set-Content -Path $OutJson -Encoding UTF8
Write-Host ("`nSnapshot -> " + $OutJson)
Write-Host ("Total: {0}, Fail: {1}, bootSmokeSkipped: {2}" -f $results.Count, $failCount, $bootSmokeSkipped)
if ($bootSmokeSkipped -or $failCount -gt 0) { exit 1 }
exit 0
