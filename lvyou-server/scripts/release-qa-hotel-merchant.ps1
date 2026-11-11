# release-qa-001：hotel-order-pay + merchant-onboard 服务端 API 回归
# 前置：Redis(6379) + MySQL(rongeyou) + server:9135 已启动；已导入 migration 种子
# 用法：powershell -File lvyou-server/scripts/release-qa-hotel-merchant.ps1
# 可选环境变量：$env:QA_APP_PHONE / $env:QA_APP_CODE / $env:QA_ADMIN_USER / $env:QA_ADMIN_PASS

param(
    [string]$Base = "http://127.0.0.1:9135/sqx_fast",
    [string]$AppPhone = $env:QA_APP_PHONE,
    [string]$AppCode = $env:QA_APP_CODE,
    [string]$AdminUser = $env:QA_ADMIN_USER,
    [string]$AdminPass = $env:QA_ADMIN_PASS
)

$ErrorActionPreference = "Continue"
$results = @()
$failures = @()

function Write-Step($msg) { Write-Host ("`n==> " + $msg) -ForegroundColor Cyan }

function Invoke-Api {
    param(
        [string]$Method = "GET",
        [string]$Path,
        [hashtable]$Headers = @{},
        $Body = $null,
        [int[]]$ExpectCodes = @(200)
    )
    $url = $Base + $Path
    $params = @{
        Uri             = $url
        Method          = $Method
        UseBasicParsing = $true
        TimeoutSec      = 30
        Headers         = $Headers
    }
    if ($Body -ne $null) {
        if ($Body -is [string]) {
            $params.Body = $Body
            $params.ContentType = "application/json"
        } else {
            $params.Body = ($Body | ConvertTo-Json -Depth 8 -Compress)
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
        if ($json.code -ne 0 -and $ok) { $ok = $false }
    }
    $script:results += [PSCustomObject]@{
        method = $Method
        path   = $Path
        status = $status
        code   = $(if ($json) { $json.code } else { $null })
        ok     = $ok
    }
    if (-not $ok) {
        $script:failures += "$Method $Path status=$status body=$raw"
    }
    return [PSCustomObject]@{ Status = $status; Json = $json; Raw = $raw; Ok = $ok }
}

function Require-Ok($resp, $label) {
    if (-not $resp.Ok) {
        Write-Host ("FAIL: " + $label) -ForegroundColor Red
        return $false
    }
    Write-Host ("OK: " + $label) -ForegroundColor Green
    return $true
}

Write-Step "0. health check"
$ping = Invoke-Api -Method GET -Path "/app/config/info" -ExpectCodes @(200)
if (-not (Require-Ok $ping "server reachable")) {
    Write-Host "Start Redis + server:9135 first" -ForegroundColor Red
    $failures += "server unreachable at $Base (Redis 6379 required for startup)"
}

if ($failures.Count -gt 0 -and -not $ping.Ok) {
    Write-Step "skip live API (server down)"
} else {

Write-Step "1. scenario A public hotel APIs"
$dest = Invoke-Api -Method GET -Path "/app/destination/list"
Require-Ok $dest "GET /app/destination/list" | Out-Null
$destId = $null
if ($dest.Json -and $dest.Json.data -and $dest.Json.data.Count -gt 0) {
    $destId = $dest.Json.data[0].id
}

$hotelListPath = if ($destId) {
    "/app/hotel/list?destinationId=$destId" + "&page=1" + "&limit=10"
} else {
    "/app/hotel/list?page=1" + "&limit=10"
}
$hotels = Invoke-Api -Method GET -Path $hotelListPath
Require-Ok $hotels "GET /app/hotel/list" | Out-Null
$hotelId = $null
$roomId = $null
if ($hotels.Json -and $hotels.Json.data -and $hotels.Json.data.records -and $hotels.Json.data.records.Count -gt 0) {
    $hotelId = $hotels.Json.data.records[0].id
}
if (-not $hotelId) {
    $failures += "hotel list empty - seed data missing"
    Write-Host "WARN: no hotel seed data, skip detail/create" -ForegroundColor Yellow
} else {
    $detail = Invoke-Api -Method GET -Path "/app/hotel/$hotelId"
    Require-Ok $detail "GET /app/hotel/{id}" | Out-Null
    $rooms = Invoke-Api -Method GET -Path "/app/hotel/$hotelId/rooms"
    Require-Ok $rooms "GET /app/hotel/{id}/rooms" | Out-Null
    if ($rooms.Json -and $rooms.Json.data -and $rooms.Json.data.Count -gt 0) {
        $roomId = $rooms.Json.data[0].id
    }
}

Write-Step "2. App JWT login"
$appToken = $null
if ($AppPhone -and $AppCode) {
    $login = Invoke-Api -Method POST -Path "/app/base/auth/phoneCodeLogin" -Body @{
        phoneNo    = $AppPhone
        verifyCode = $AppCode
    }
    if ($login.Json -and $login.Json.data -and $login.Json.data.token) {
        $appToken = $login.Json.data.token
    }
}
if (-not $appToken) {
    $failures += "App JWT missing - set QA_APP_PHONE + QA_APP_CODE or pre-login token"
    Write-Host "WARN: no App token, skip authenticated APIs" -ForegroundColor Yellow
} else {
    $appHeaders = @{ token = $appToken }
    $me = Invoke-Api -Method GET -Path "/app/base/user/info" -Headers $appHeaders
    Require-Ok $me "GET /app/base/user/info (App JWT)" | Out-Null

    Write-Step "3. scenario A create order + wallet pay + list"
    if ($hotelId -and $roomId) {
        $checkIn = (Get-Date).AddDays(7).ToString("yyyy-MM-dd")
        $checkOut = (Get-Date).AddDays(8).ToString("yyyy-MM-dd")
        $create = Invoke-Api -Method POST -Path "/app/order/create" -Headers $appHeaders -Body @{
            category     = "hotel"
            hotelId      = $hotelId
            roomId       = $roomId
            checkInDate  = $checkIn
            checkOutDate = $checkOut
            guestCount   = 1
            contactName  = "QA"
            contactPhone = $AppPhone
        }
        if (Require-Ok $create "POST /app/order/create") {
            $orderNo = $create.Json.data.orderNo
            $orderId = $create.Json.data.orderId
            $couponPath = "/app/coupon/available?scope=hotel" + "&amount=100"
            $coupon = Invoke-Api -Method GET -Path $couponPath -Headers $appHeaders
            Require-Ok $coupon "GET /app/coupon/available" | Out-Null
            $pay = Invoke-Api -Method POST -Path "/app/wallet/pay" -Headers $appHeaders -Body @{ orderNo = $orderNo }
            Require-Ok $pay "POST /app/wallet/pay" | Out-Null
            $listPath = "/app/order/list?category=hotel" + "&page=1" + "&limit=10"
            $list = Invoke-Api -Method GET -Path $listPath -Headers $appHeaders
            Require-Ok $list "GET /app/order/list" | Out-Null
            $detailOrder = Invoke-Api -Method GET -Path "/app/order/$orderId" -Headers $appHeaders
            Require-Ok $detailOrder "GET /app/order/{id}" | Out-Null
        }
    }

    Write-Step "4. scenario B merchant apply/status"
    $statusBefore = Invoke-Api -Method GET -Path "/app/merchant/status" -Headers $appHeaders
    Require-Ok $statusBefore "GET /app/merchant/status" | Out-Null
    $curStatus = $null
    if ($statusBefore.Json -and $statusBefore.Json.data) {
        $curStatus = $statusBefore.Json.data.status
    }
    if ($curStatus -eq "none" -or $curStatus -eq "rejected") {
        $apply = Invoke-Api -Method POST -Path "/app/merchant/apply" -Headers $appHeaders -Body @{
            contactName        = "QA Merchant"
            contactPhone       = $AppPhone
            storeName          = "QA Test Hotel"
            storeRegion        = "Test District"
            storeAddressDetail = "No.1 Test Road"
            storeType          = "hotel"
            licenseNo          = "91330000QA000000X"
            licenseImage       = "/upload/qa/license.jpg"
            legalName          = "Zhang San"
            legalIdNo          = "110101199001011234"
            legalIdFront       = "/upload/qa/id-front.jpg"
            legalIdBack        = "/upload/qa/id-back.jpg"
        }
        Require-Ok $apply "POST /app/merchant/apply" | Out-Null
        $statusPending = Invoke-Api -Method GET -Path "/app/merchant/status" -Headers $appHeaders
        Require-Ok $statusPending "GET /app/merchant/status pending" | Out-Null
    }
}

Write-Step "5. Admin audit + merchant center"
$adminToken = $null
if ($AdminUser -and $AdminPass) {
    $adminLogin = Invoke-Api -Method POST -Path "/sys/login" -Body @{
        username = $AdminUser
        password = $AdminPass
    }
    if ($adminLogin.Json -and $adminLogin.Json.data -and $adminLogin.Json.data.token) {
        $adminToken = $adminLogin.Json.data.token
    }
}
if (-not $adminToken) {
    $failures += "Admin token missing - set QA_ADMIN_USER + QA_ADMIN_PASS"
    Write-Host "WARN: no Admin token, skip audit APIs" -ForegroundColor Yellow
} else {
    $adminHeaders = @{ token = $adminToken }
    $applyPage = Invoke-Api -Method POST -Path "/admin/merchant/apply/page" -Headers $adminHeaders -Body @{
        page        = 1
        limit       = 10
        auditStatus = 0
    }
    if (Require-Ok $applyPage "POST /admin/merchant/apply/page") {
        $applyId = $null
        if ($applyPage.Json.data.records.Count -gt 0) {
            $applyId = $applyPage.Json.data.records[0].id
        }
        if ($applyId) {
            $audit = Invoke-Api -Method POST -Path "/admin/merchant/audit" -Headers $adminHeaders -Body @{
                id     = $applyId
                action = "pass"
            }
            Require-Ok $audit "POST /admin/merchant/audit pass" | Out-Null
        }
    }
    if ($appToken) {
        $dash = Invoke-Api -Method GET -Path "/app/merchant/dashboard" -Headers @{ token = $appToken }
        Require-Ok $dash "GET /app/merchant/dashboard" | Out-Null
        $mOrdersPath = "/app/merchant/orders?tab=pending" + "&page=1" + "&limit=10"
        $mOrders = Invoke-Api -Method GET -Path $mOrdersPath -Headers @{ token = $appToken }
        Require-Ok $mOrders "GET /app/merchant/orders" | Out-Null
    }
}

} # end live API block

Write-Step "summary"
$passCount = @($results | Where-Object { $_.ok }).Count
$failCount = @($results | Where-Object { -not $_.ok }).Count
Write-Host ("API calls: {0} pass, {1} fail" -f $passCount, $failCount)
foreach ($r in $results) {
    $mark = if ($r.ok) { "OK" } else { "FAIL" }
    Write-Host ("  [{0}] {1} {2} status={3} code={4}" -f $mark, $r.method, $r.path, $r.status, $r.code)
}

$repoRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$outDir = Join-Path $repoRoot "docs/project-extension"
$outFile = Join-Path $outDir "release-qa-hotel-merchant-verify.json"
$report = @{
    taskId      = "release-qa-001"
    verifiedAt  = (Get-Date).ToUniversalTime().ToString("o")
    baseUrl     = $Base
    passCount   = $passCount
    failCount   = $failCount
    results     = $results
    failures    = $failures
    allPassed   = ($failCount -eq 0 -and $failures.Count -eq 0)
}
$report | ConvertTo-Json -Depth 6 | Set-Content -Path $outFile -Encoding UTF8
Write-Host ("Report: " + $outFile)

if ($failCount -gt 0 -or $failures.Count -gt 0) { exit 1 }
exit 0
