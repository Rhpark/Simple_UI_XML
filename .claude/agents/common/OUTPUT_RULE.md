# OUTPUT_RULE — 보고서 파일 저장 규칙

이 파일은 analysis·review 에이전트가 보고서를 파일로 저장할 때 따르는 공통 규칙이다.

## 출력 디렉터리 확인

- Glob 패턴으로 출력 디렉터리를 확인할 때 **빈 폴더는 Glob에 걸리지 않는다.**
  Glob은 파일을 찾는 도구이므로 폴더 안에 파일이 없으면 결과가 0건으로 나온다.
  폴더 존재 확인이 목적이라면 `Bash: ls` 또는 `Bash: test -d`를 사용한다.
- 출력 경로가 이미 존재하는데 mkdir을 실행해도 무해하다. 불확실하면 `mkdir -p`를 사용한다.

## 파일 쓰기 방법

- **Write 도구를 최우선으로 사용한다.** bash heredoc·python -c 방식보다 안전하다.
- bash heredoc(`cat << 'EOF' > file`) 또는 `python3 -c "..."` 인라인 방식은 보고서 본문에
  작은따옴표(`'`), 백틱(`` ` ``), 달러(`$`) 등 셸 특수문자가 포함되면 파싱 오류로 실패한다.
  실제 오류 사례: `/usr/bin/bash: eval: line N: unexpected EOF while looking for matching '`

## 실패 시 대응 순서

1. Write 도구로 전체 내용을 한 번에 쓴다. (최우선)
2. Write 도구가 불가하면 내용을 논리 단위로 분할해 Bash + python3 스크립트 파일 방식으로 쓴다.
3. heredoc·인라인 python3 `-c` 방식은 최후 수단으로만 사용하고, 사용 전 본문 내 특수문자를
   이스케이프 처리했는지 확인한다.
4. 분할 쓰기 시 각 조각의 저장 성공 여부를 Read 도구로 확인한 뒤 다음 조각을 작성한다.
