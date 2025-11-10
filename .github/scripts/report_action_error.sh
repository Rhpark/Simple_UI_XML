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
FAILURE_LOG_INPUT="${FAILURE_LOG:-Logs available at ${RUN_URL}}"
FAILURE_ATTEMPTS_INPUT="${FAILURE_ATTEMPTS:-_Not provided_}"
FAILURE_ENVIRONMENT_INPUT="${FAILURE_ENVIRONMENT:-Runner: ${RUNNER_NAME:-unknown} | Event: ${EVENT_NAME}}"

ISSUE_TITLE="[CI][${STAGE_NAME_INPUT}] Failure @ ${SHORT_SHA}"

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
SEARCH_QUERY=$(python3 - <<'PY'
import os, urllib.parse
repo = os.environ["REPO"]
title = os.environ["ISSUE_TITLE"]
query = f'repo:{repo} state:open in:title "{title}"'
print(urllib.parse.quote(query, safe=''))
PY
)

SEARCH_URL="${API_URL}/search/issues?q=${SEARCH_QUERY}"
SEARCH_RESPONSE=$(curl -sSf \
  -H "Authorization: Bearer ${GITHUB_TOKEN}" \
  -H "Accept: application/vnd.github+json" \
  "${SEARCH_URL}")

ISSUE_NUMBER=$(python3 - <<'PY'
import json, sys
try:
    data = json.load(sys.stdin)
    items = data.get("items") or []
    if items:
        print(items[0]["number"])
except Exception:
    pass
PY <<< "${SEARCH_RESPONSE}" || true)

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
