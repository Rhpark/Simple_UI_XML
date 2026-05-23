# Analysis Static Checklist

목적: analysis 워크플로우 문서가 실행 전에 구조적으로 유효한지 정적 검증한다.

## 검증 대상

- [../step1/STEP1_SCOPE.md](../step1/STEP1_SCOPE.md)
- [../step1/STEP1_SAMPLES.md](../step1/STEP1_SAMPLES.md)
- [../step2/STEP2_READ.md](../step2/STEP2_READ.md)
- [../step2/STEP2_SAMPLES.md](../step2/STEP2_SAMPLES.md)
- [../step3/STEP3_IMPACT.md](../step3/STEP3_IMPACT.md)
- [../step3/STEP3_SAMPLES.md](../step3/STEP3_SAMPLES.md)
- [../step4/STEP4_REPORT.md](../step4/STEP4_REPORT.md)
- [../step4/STEP4_SAMPLES.md](../step4/STEP4_SAMPLES.md)
- [../../common/GLOSSARY.md](../../common/GLOSSARY.md)

## 정적 체크

| ID | 검증 항목 | 통과 기준 | 결과 |
| --- | --- | --- | --- |
| S1 | 샘플 문서 존재 | STEP1~STEP4 모두 `STEP*_SAMPLES.md`가 있다 | PASS |
| S2 | 링크 유효성 | 모든 Markdown 상대 링크가 실제 파일을 가리킨다 | PASS |
| S3 | 구버전 진행도 제거 | `(9/9)`, `(10/10)`, `N = 고정 항목`이 없다 | PASS |
| S4 | 구버전 타입 규칙 제거 | `해당하는 타입 하나만` 문구가 없다 | PASS |
| S5 | 구버전 용어 제거 | `직접 관련 파일`, `수정 대상`, `수정 시`, `직접 영향 대상`, `간접 영향 대상`이 없다 | PASS |
| S6 | Glossary 용어 정의 | `핵심 파일`, `근거 파일`, `직접 파일`, `간접 파일`, `공통 파일`, `영향 범위`가 정의돼 있다 | PASS |
| S7 | STEP2 직행 계약 | `STEP4 직행 보강`이 STEP2 산출물과 하네스에 있다 | PASS |
| S8 | STEP4 이중 진입 | STEP4가 `STEP3 통과`와 `STEP3 생략 직행` 경로를 모두 허용한다 | PASS |
| S9 | 루프 판정·심각도 정의 중복 금지 | 루프 판정 기준과 심각도 4단계 정의가 `common/` SSOT 파일에만 있고 STEP 파일은 링크만 있다 | -

## 재실행 명령 예시

```powershell
Select-String -Path .\docs\agents\analysis\step*\*.md,.\docs\agents\common\GLOSSARY.md -Encoding UTF8 -Pattern '직접 관련 파일|수정 대상|수정 시|이번 변경에서 코드를 수정|직접 영향 대상|간접 영향 대상|해당하는 타입 하나만|N = 고정 항목|\(9/9\)|\(10/10\)'
```

```powershell
$files = Get-ChildItem -LiteralPath '.\docs\agents\analysis' -Recurse -Filter '*.md'
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
