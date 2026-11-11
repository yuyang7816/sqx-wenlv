# 空库核心接口冒烟：migration 后公开/读接口不应 500
# 用法：先启动 server（9135），再执行 .\scripts\empty-db-core-api-verify.ps1
# 前置：已导入 sql/migration/V20260909_lvyou_core.sql 及后续增量

$Base = "http://127.0.0.1:9135/sqx_fast"
$Endpoints = @(
    @{ Method = "POST"; Path = "/app/base/banner/page"; Body = '{"page":1,"limit":10}' },
    @{ Method = "GET";  Path = "/app/config/info" },
    @{ Method = "GET";  Path = "/app/agreement/user" },
    @{ Method = "GET";  Path = "/app/home/recommend" },
    @{ Method = "GET";  Path = "/app/notice/marquee" },
    @{ Method = "GET";  Path = "/app/destination/list" },
    @{ Method = "GET";  Path = "/app/scenic/list?page=1&limit=10" },
    @{ Method = "GET";  Path = "/app/help/faq" },
    @{ Method = "GET";  Path = "/app/base/user/info"; ExpectStatus = 401 }
)

$results = @()
$fail = 0
foreach ($ep in $Endpoints) {
    $url = $Base + $ep.Path
    try {
        if ($ep.Method -eq "GET") {
            $resp = Invoke-WebRequest -Uri $url -Method GET -UseBasicParsing -TimeoutSec 10
        } else {
            $resp = Invoke-WebRequest -Uri $url -Method POST -Body $ep.Body -ContentType "application/json" -UseBasicParsing -TimeoutSec 10
        }
        $code = $resp.StatusCode
        $ok = ($code -lt 500)
        if ($ep.ExpectStatus -and $code -ne $ep.ExpectStatus) { $ok = $false }
    } catch {
        $code = $_.Exception.Response.StatusCode.value__
        if (-not $code) { $code = 0 }
        $ok = ($code -lt 500)
        if ($ep.ExpectStatus -and $code -eq $ep.ExpectStatus) { $ok = $true }
    }
    if (-not $ok) { $fail++ }
    $results += [PSCustomObject]@{ method = $ep.Method; path = $ep.Path; status = $code; ok = $ok }
    Write-Host ("[{0}] {1} {2}" -f ($(if ($ok) { "OK" } else { "FAIL" })), $ep.Method, $ep.Path)
}

Write-Host ""
Write-Host ("Total: {0}, Fail: {1}" -f $results.Count, $fail)
if ($fail -gt 0) { exit 1 }
exit 0
