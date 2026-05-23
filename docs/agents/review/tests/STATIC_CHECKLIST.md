# Review Static Checklist

목적: review 워크플로우 문서가 실행 전에 구조적으로 유효한지 정적 검증한다.

## 검증 대상

- [../step1/STEP1_FUNC.md](../step1/STEP1_FUNC.md)
- [../step1/STEP1_SAMPLES.md](../step1/STEP1_SAMPLES.md)
- [../step2/STEP2_LOGIC.md](../step2/STEP2_LOGIC.md)
- [../step2/STEP2_SAMPLES.md](../step2/STEP2_SAMPLES.md)
- [../step3/STEP3_ARCH.md](../step3/STEP3_ARCH.md)
- [../step3/STEP3_SAMPLES.md](../step3/STEP3_SAMPLES.md)
- [../step4/STEP4_QUALITY.md](../step4/STEP4_QUALITY.md)
- [../step4/STEP4_SAMPLES.md](../step4/STEP4_SAMPLES.md)
- [../step5/STEP5_REPORT.md](../step5/STEP5_REPORT.md)
- [../step5/STEP5_SAMPLES.md](../step5/STEP5_SAMPLES.md)
- [../common/GLOSSARY.md](../common/GLOSSARY.md)
- [.claude/agents/review.md](../../../../.claude/agents/review.md)
- [../../common/SEVERITY_RULE.md](../../common/SEVERITY_RULE.md)
- [../../common/STEP_ROLLBACK.md](../../common/STEP_ROLLBACK.md)
- [../../common/STEP_FORCE_STOP.md](../../common/STEP_FORCE_STOP.md)
- [../../common/STEP_HARNESS.md](../../common/STEP_HARNESS.md)
- [../../common/STEP_EXECUTION_RULE.md](../../common/STEP_EXECUTION_RULE.md)

## 정적 체크

| ID | 검증 항목 | 통과 기준 | 결과 |
| --- | --- | --- | --- |
| S1 | SAMPLES 파일 존재 | STEP1~5 모두 `STEP*_SAMPLES.md`가 있다 | PASS |
| S2 | 링크 유효성 | 모든 Markdown 상대 링크가 실제 파일을 가리킨다 | PASS |
| S3 | 한자 없음 | 분析 등 CJK 한자 문자(U+4E00–U+9FFF)가 없다 | PASS |
| S4 | 크로스 스텝 경로 | 타 STEP 파일 참조 시 `../stepX/` 접두사를 사용한다 | PASS |
| S5 | analysis 의존성 명시 | review.md 분기 B에 analysis STEP4 산출물 없을 시 강제 중단 조건이 있다 | PASS |
| S6 | 하네스 게이트 존재 | STEP1~5 각 파일에 `## 하네스 (통과 기준)` 섹션이 있다 | PASS |
| S7 | 실패 규격 존재 | STEP1~5 각 파일에 `## 실패 규격` 섹션이 있다 | PASS |
| S8 | 심각도 기준 SSOT | CRITICAL/HIGH/MEDIUM/LOW 정의가 `common/SEVERITY_RULE.md`에만 있고 STEP 파일은 링크만 있다 | - |
| S9 | 루프 판정 SSOT | 루프 판정 기준이 `common/STEP_ROLLBACK.md`에만 있고 STEP 파일은 링크만 있다 | - |

## 재실행 명령 예시

### S3 — 한자 검사

```powershell
$files = Get-ChildItem -Path '.\review' -Recurse -Filter '*.md'
$found = @()
foreach ($file in $files) {
  $content = Get-Content -LiteralPath $file.FullName -Encoding UTF8 -Raw
  if ($content -match '[一-鿿]') {
    $found += $file.FullName
  }
}
if ($found.Count -eq 0) { 'NO_HANJA_FOUND' } else { $found }
```

### S2 — 링크 유효성 검사

```powershell
$files = Get-ChildItem -Path '.\review' -Recurse -Filter '*.md'
$missing = @()
foreach ($file in $files) {
  $content = Get-Content -LiteralPath $file.FullName -Encoding UTF8
  foreach ($line in $content) {
    foreach ($m in [regex]::Matches($line, '\[[^\]]+\]\(([^\)]+\.md)\)')) {
      $target = $m.Groups[1].Value
      if ($target.StartsWith('http')) { continue }
      $base = Split-Path -Parent $file.FullName
      $full = [System.IO.Path]::GetFullPath((Join-Path $base $target))
      if (-not (Test-Path -LiteralPath $full)) {
        $missing += "$($file.FullName): $target -> MISSING"
      }
    }
  }
}
if ($missing.Count -eq 0) { 'ALL_LINKS_OK' } else { $missing }
```

### S4 — 크로스 스텝 경로 검사 (잘못된 단순 파일명 참조 탐지)

```powershell
# 다른 STEP 파일을 ../stepX/ 없이 직접 참조하는 경우를 탐지한다
Select-String -Path '.\review\step*\*.md' -Encoding UTF8 `
  -Pattern '\(STEP[1-5]_(FUNC|LOGIC|ARCH|QUALITY|REPORT|SAMPLES)\.md\)'
```
