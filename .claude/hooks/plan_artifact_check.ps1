# ==============================================================================
# plan_artifact_check.ps1
# ------------------------------------------------------------------------------
# 목적
#   plan 산출물(PRD/SPEC/PLAN)의 존재와 타임스탬프 형식을 검증한다.
#   검증 결과를 docs/agents/output/plan/{TS}/_verification.txt 에 기록한다.
#
#   규격 검증(필수 섹션 등)은 mainAgent 가 plan_format_check
#   SubAgent 를 통해 별도 수행한다.
#
# 인자
#   -TS    : 검증 대상 타임스탬프 폴더명 (yyMMdd_HHmmss). 필수.
#   -Stage : 검증 범위. 기본 'full'.
#            · prd_spec : PRD/SPEC 2개만 검증 (BeforePlan 끝 / MakePlan 시작 — PLAN 미생성 시점)
#            · full     : PRD/SPEC/PLAN 3개 검증 (PLAN 완료 후)
#
# 호출 환경
#   - plan_mode 1단계에서 mainAgent 가 직접 실행한다 (자동 hook 아님).
#   - 사용자 로컬 PowerShell 환경에서 실행됨
#
# 출력 계약
#   - 결과 메시지를 평문(plain text)으로 stdout 에 출력한다.
#     (과거 PostToolUse hook JSON 래핑은 제거됨 — 이 스크립트는 hook 이 아니라 CLI 다.)
#   - 산출물 폴더가 있으면 {TS}/_verification.txt 에도 동일 메시지를 기록한다.
#     폴더가 없는 실패 케이스(루트 없음/TS 미전달/폴더 없음)는 stdout 이 유일한 결과 채널이다.
#   - 종료 코드: PASS = 0 / FAIL = 1. (자동화·조건 분기에서 exit code 사용 가능)
#
# 다른 OS 에서 실행 시
#   - Linux/Mac 환경이라면 plan_artifact_check.sh 를 별도로 작성해야 한다.
#   - 현재 이 프로젝트는 Windows 환경 전용으로 .ps1 만 제공한다.
# ==============================================================================

param([string]$TS = '', [string]$Stage = 'full')

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$planRoot = Join-Path (Get-Location) 'docs/agents/output/plan'

function Emit-Result($message) {
    # 결과 메시지를 평문으로 stdout 에 출력한다.
    # mainAgent 가 직접 실행하는 CLI 스크립트이며, hook 이 아니다.
    Write-Output $message
}

function Write-VerificationFile($folder, $content) {
    if (-not [string]::IsNullOrEmpty($folder) -and (Test-Path $folder)) {
        $filePath = Join-Path $folder '_verification.txt'
        $content | Out-File -FilePath $filePath -Encoding utf8 -Force
    }
}

# 0. Stage 인자 검증 (허용: full | prd_spec)
if ($Stage -ne 'full' -and $Stage -ne 'prd_spec') {
    $msg = "[Plan 산출물 검증] FAIL`n사유: -Stage 값이 올바르지 않습니다 — '$Stage' (허용: full | prd_spec)`n권장 조치: -Stage full 또는 -Stage prd_spec 으로 재실행."
    Emit-Result $msg
    exit 1
}

# 1. 산출물 루트 폴더 존재 확인
if (-not (Test-Path $planRoot)) {
    $msg = "[Plan 산출물 검증] FAIL`n사유: 산출물 루트 폴더 없음 — $planRoot`n권장 조치: 사용자에게 산출물 폴더 부재 보고. Plan Mode 재진행 필요."
    Emit-Result $msg
    exit 1
}

# 2. 검증 대상 폴더 결정 ($TS 인자로 특정 — 최신 폴더 임의 스캔 금지)
if ([string]::IsNullOrWhiteSpace($TS)) {
    $msg = "[Plan 산출물 검증] FAIL`n사유: 검증 대상 타임스탬프(-TS) 인자가 전달되지 않았습니다.`n권장 조치: mainAgent가 승계한 {TS}를 -TS 인자로 전달하여 재실행. (최신 폴더 임의 스캔은 금지)"
    Emit-Result $msg
    exit 1
}

$tsName = $TS
$folder = Join-Path $planRoot $tsName

if (-not (Test-Path $folder)) {
    $msg = "[Plan 산출물 검증] FAIL`n사유: 지정한 산출물 폴더가 없습니다 — $folder`n권장 조치: 승계한 {TS}가 올바른지 확인 후 재실행. 사용자에게 보고."
    Emit-Result $msg
    exit 1
}

# 3. 타임스탬프 패턴 검증 (yyMMdd_HHmmss)
if ($tsName -notmatch '^[0-9]{6}_[0-9]{6}$') {
    $msg = "[Plan 산출물 검증] FAIL`n사유: 폴더명이 yyMMdd_HHmmss 형식 아님 — $tsName`n권장 조치: mainAgent가 1.4 보완 루프 진입하여 폴더명 수정 또는 사용자에게 보고."
    Emit-Result $msg
    Write-VerificationFile $folder $msg
    exit 1
}

# 4. 검증 대상 파일 존재 확인 (Stage 에 따라 PLAN 포함 여부 결정)
$expected = [ordered]@{
    'PRD'  = (Join-Path $folder "${tsName}_PRD.md")
    'SPEC' = (Join-Path $folder "${tsName}_SPEC.md")
}
if ($Stage -eq 'full') {
    $expected['PLAN'] = (Join-Path $folder "${tsName}_PLAN.md")
}

$missing = @()
foreach ($key in $expected.Keys) {
    if (-not (Test-Path $expected[$key])) {
        $missing += "${key}: $($expected[$key])"
    }
}

if ($missing.Count -gt 0) {
    $missingList = ($missing | ForEach-Object { "  - $_" }) -join "`n"
    $msg = "[Plan 산출물 검증] FAIL`n사유: 누락 파일`n$missingList`n타임스탬프: $tsName`n폴더: $folder`n권장 조치: mainAgent가 1.4 보완 루프 진입하여 누락 파일 생성."
    Emit-Result $msg
    Write-VerificationFile $folder $msg
    exit 1
}

# 5. 빈 파일(라인 수 임계값) 검증
$thresholds = @{ 'PRD' = 10; 'SPEC' = 8; 'PLAN' = 3 }
$lineFails = @()
foreach ($key in $expected.Keys) {
    $count = (Get-Content $expected[$key]).Count
    $min = $thresholds[$key]
    if ($count -lt $min) {
        $lineFails += "${key}: ${count}줄 (>=$min 필요)"
    }
}

if ($lineFails.Count -gt 0) {
    $failsList = ($lineFails | ForEach-Object { "  - $_" }) -join "`n"
    $msg = "[Plan 산출물 검증] FAIL`n사유: 라인 수 임계값 미만`n$failsList`n타임스탬프: $tsName`n폴더: $folder`n권장 조치: mainAgent가 1.4 보완 루프 진입하여 산출물 보강."
    Emit-Result $msg
    Write-VerificationFile $folder $msg
    exit 1
}

# 6. 모두 통과
$fileLabel = if ($Stage -eq 'full') { 'PRD/SPEC/PLAN 3개' } else { 'PRD/SPEC 2개' }
$msg = "[Plan 산출물 검증] PASS`n타임스탬프: $tsName`n검증 범위(Stage): $Stage`n폴더: $folder`n$fileLabel 파일 모두 존재 및 임계값 통과`n`n다음 단계: mainAgent는 plan_format_check SubAgent(-Stage $Stage)를 호출하여 섹션 검증을 수행한다."
Emit-Result $msg
Write-VerificationFile $folder $msg
exit 0
