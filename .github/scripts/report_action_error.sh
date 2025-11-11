#!/usr/bin/env bash

set -euo pipefail

STAGE_NAME_INPUT="${1:-${STAGE_NAME:-}}"
if [[ -z "${STAGE_NAME_INPUT}" ]]; then
  echo "[report_action_error] Stage name is required via argument or STAGE_NAME env." >&2
  exit 1
fi

GITHUB_TOKEN="${GITHUB_TOKEN:-}"
if [[ -z "${GITHUB_TOKEN}" ]]; then
  echo "[report_action_error] GITHUB_TOKEN is required." >&2
  exit 1
fi

REPO="${GITHUB_REPOSITORY:-}"
if [[ -z "${REPO}" ]]; then
  echo "[report_action_error] GITHUB_REPOSITORY is not set." >&2
  exit 1
fi

RUN_ID="${GITHUB_RUN_ID:-}"
SHA="${GITHUB_SHA:-unknown}"
SHORT_SHA="${SHA:0:7}"
RUN_URL="https://github.com/${REPO}/actions/runs/${RUN_ID}"
WORKFLOW_NAME="${GITHUB_WORKFLOW:-unknown workflow}"
JOB_NAME="${GITHUB_JOB:-unknown job}"
EVENT_NAME="${GITHUB_EVENT_NAME:-unknown}"
ACTOR="${GITHUB_ACTOR:-unknown}"
API_URL="${GITHUB_API_URL:-https://api.github.com}"

FAILURE_MESSAGE_INPUT="${FAILURE_MESSAGE:-No additional summary provided.}"
FAILURE_LOG_INPUT="${FAILURE_LOG:-}"
if [[ -z "${FAILURE_LOG_INPUT}" && -n "${FAILURE_LOG_PATH:-}" && -f "${FAILURE_LOG_PATH}" ]]; then
    FAILURE_LOG_INPUT="$(cat "${FAILURE_LOG_PATH}")"
fi
if [[ -z "${FAILURE_LOG_INPUT}" ]]; then
    FAILURE_LOG_INPUT="Logs available at ${RUN_URL}"
fi
FAILURE_ENVIRONMENT_INPUT="${FAILURE_ENVIRONMENT:-Runner: ${RUNNER_NAME:-unknown} | Event: ${EVENT_NAME}}"
APP_VERSION_INPUT="${APPLICATION_VERSION:-}"
if [[ -z "${APP_VERSION_INPUT}" ]]; then
    VERSION_FILE="${APPLICATION_VERSION_FILE:-simple_ui/build.gradle.kts}"
    if [[ -f "${VERSION_FILE}" ]]; then
        APP_VERSION_INPUT="$(python3 - "${VERSION_FILE}" <<'PY'
import pathlib
import re
import sys

path = pathlib.Path(sys.argv[1])
pattern = re.compile(r'version\s*=\s*"([^"]+)"')
for line in path.read_text(encoding="utf-8", errors="ignore").splitlines():
    stripped = line.strip()
    if stripped.startswith("//") or not stripped:
        continue
    match = pattern.search(stripped)
    if match:
        print(match.group(1))
        break
PY
)"
        APP_VERSION_INPUT="$(echo "${APP_VERSION_INPUT}" | head -n 1 | tr -d '\r')"
    fi
fi
if [[ -z "${APP_VERSION_INPUT}" ]]; then
    APP_VERSION_INPUT="_Unknown_"
fi

declare -a ISSUE_LABELS
case "${STAGE_NAME_INPUT}" in
  "Initialize Check")
    ISSUE_LABELS=("CI-Initialize")
    ;;
  "Unit Tests")
    ISSUE_LABELS=("CI-UnitTest")
    ;;
  "Robolectric Tests")
    ISSUE_LABELS=("CI-Robolectric")
    ;;
  "Build and Lint Check")
    ISSUE_LABELS=("CI-Build")
    ;;
  "Release from Commit Message")
    ISSUE_LABELS=("CI-Release")
    ;;
  *)
    ISSUE_LABELS=("ci" "needs-triage")
    ;;
esac

ISSUE_LABELS_JSON="$(printf '%s\n' "${ISSUE_LABELS[@]}" | python3 <<'PY'
import json, sys
labels = [line.strip() for line in sys.stdin if line.strip()]
if not labels:
    labels = ["ci", "needs-triage"]
print(json.dumps(labels, ensure_ascii=False))
PY
)"
export ISSUE_LABELS_JSON

extract_title() {
    python3 - "$FAILURE_MESSAGE_INPUT" "$FAILURE_LOG_INPUT" <<'PY'
import sys, re

message = sys.argv[1]
log = sys.argv[2]

def find_line_with_extension(text):
    for line in text.splitlines():
        if "." in line and ":" in line:
            return line.strip()
    for line in text.splitlines():
        if "." in line:
            return line.strip()
    return None

for source in (log, message):
    if not source:
        continue
    line = find_line_with_extension(source)
    if line:
        print(line)
        sys.exit(0)

fallback = (log or message or "Failure detected").splitlines()
print(fallback[0].strip() if fallback else "Failure detected")
PY
}
TITLE_SUFFIX="$(extract_title)"
ISSUE_TITLE="[CI][${STAGE_NAME_INPUT}] ${TITLE_SUFFIX}"

export REPO ISSUE_TITLE
ISSUE_BODY=$(cat <<EOF
- **Failed stage**: ${STAGE_NAME_INPUT}
- **Workflow run**: ${RUN_URL}
- **Workflow**: ${WORKFLOW_NAME}
- **Application Version**: ${APP_VERSION_INPUT}
- **Job**: ${JOB_NAME}
- **Commit**: ${SHA}
- **Actor**: ${ACTOR}

### Failure summary / 실패 요약
${FAILURE_MESSAGE_INPUT}

### Relevant log excerpt / 관련 로그
${FAILURE_LOG_INPUT}

### Extra context / 추가 정보
${FAILURE_ENVIRONMENT_INPUT}
EOF
)

export ISSUE_BODY
LIST_URL="${API_URL}/repos/${REPO}/issues?state=open&per_page=100"
LIST_RESPONSE=$(curl -sSf \
  -H "Authorization: Bearer ${GITHUB_TOKEN}" \
  -H "Accept: application/vnd.github+json" \
  "${LIST_URL}")
export LIST_RESPONSE
ISSUE_NUMBER=$(python3 - <<'PY'
import json, os, sys
title = os.environ["ISSUE_TITLE"]
try:
    issues = json.loads(os.environ["LIST_RESPONSE"])
    for issue in issues:
        if issue.get("title") == title:
            print(issue.get("number"))
            break
except Exception:
    pass
PY
)

create_issue() {
  export ISSUE_PAYLOAD
  ISSUE_PAYLOAD=$(python3 - <<'PY'
import json, os
labels = json.loads(os.environ.get("ISSUE_LABELS_JSON", "[]"))
if not labels:
    labels = ["ci", "needs-triage"]
payload = {
    "title": os.environ["ISSUE_TITLE"],
    "body": os.environ["ISSUE_BODY"],
    "labels": labels
}
print(json.dumps(payload))
PY
)

  curl -sSf \
    -X POST \
    -H "Authorization: Bearer ${GITHUB_TOKEN}" \
    -H "Accept: application/vnd.github+json" \
    -d "${ISSUE_PAYLOAD}" \
    "${API_URL}/repos/${REPO}/issues" >/dev/null
  echo "[report_action_error] Created new issue: ${ISSUE_TITLE}"
}

comment_issue() {
  export COMMENT_BODY
  COMMENT_BODY=$(cat <<EOF
Another failure detected for this stage.
- Workflow run: ${RUN_URL}
- Job: ${WORKFLOW_NAME} / ${JOB_NAME}
- Commit: ${SHA}
- Application Version: ${APP_VERSION_INPUT}

Summary:
${FAILURE_MESSAGE_INPUT}

Logs:
```
${FAILURE_LOG_INPUT}
```
EOF
)

  export COMMENT_PAYLOAD
  COMMENT_PAYLOAD=$(python3 - <<'PY'
import json, os
print(json.dumps({"body": os.environ["COMMENT_BODY"]}))
PY
)

  curl -sSf \
    -X POST \
    -H "Authorization: Bearer ${GITHUB_TOKEN}" \
    -H "Accept: application/vnd.github+json" \
    -d "${COMMENT_PAYLOAD}" \
    "${API_URL}/repos/${REPO}/issues/${ISSUE_NUMBER}/comments" >/dev/null
  echo "[report_action_error] Commented on existing issue #${ISSUE_NUMBER}"
}

if [[ -n "${ISSUE_NUMBER}" ]]; then
  comment_issue
else
  create_issue
fi
