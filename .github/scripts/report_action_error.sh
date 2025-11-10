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
FAILURE_ATTEMPTS_INPUT="${FAILURE_ATTEMPTS:-_Not provided_}"
FAILURE_ENVIRONMENT_INPUT="${FAILURE_ENVIRONMENT:-Runner: ${RUNNER_NAME:-unknown} | Event: ${EVENT_NAME}}"

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
- **Job**: ${JOB_NAME}
- **Commit**: ${SHA}
- **Actor**: ${ACTOR}

### Failure summary / 실패 요약
${FAILURE_MESSAGE_INPUT}

### Relevant log excerpt / 관련 로그
${FAILURE_LOG_INPUT}

### Steps already tried / 시도해본 조치
${FAILURE_ATTEMPTS_INPUT}

### Extra context / 추가 정보
${FAILURE_ENVIRONMENT_INPUT}
EOF
)

export ISSUE_BODY
LIST_URL="${API_URL}/repos/${REPO}/issues?state=open&per_page=100"
ISSUE_NUMBER=$(curl -sSf \
  -H "Authorization: Bearer ${GITHUB_TOKEN}" \
  -H "Accept: application/vnd.github+json" \
  "${LIST_URL}" | python3 - <<'PY'
import json, os, sys
title = os.environ["ISSUE_TITLE"]
try:
    issues = json.load(sys.stdin)
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
payload = {
    "title": os.environ["ISSUE_TITLE"],
    "body": os.environ["ISSUE_BODY"],
    "labels": ["ci", "needs-triage"]
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
