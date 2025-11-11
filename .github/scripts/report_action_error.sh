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

create_tmp_file() {
    mktemp 2>/dev/null || mktemp -t ci_issue
}

CI_TEMPLATE_FILE="${CI_ISSUE_TEMPLATE_FILE:-.github/ISSUE_TEMPLATE/ci-issue.yml}"
ISSUE_BODY_FILE="$(create_tmp_file)"
COMMENT_BODY_FILE="$(create_tmp_file)"
LABELS_FILE="$(create_tmp_file)"

export CI_TEMPLATE_FILE ISSUE_BODY_FILE COMMENT_BODY_FILE LABELS_FILE \
    STAGE_NAME_INPUT RUN_URL WORKFLOW_NAME JOB_NAME APP_VERSION_INPUT \
    FAILURE_MESSAGE_INPUT FAILURE_LOG_INPUT FAILURE_ENVIRONMENT_INPUT SHA ACTOR

python3 <<'PY'
import os
import pathlib
import re
import textwrap

ci_template_path = pathlib.Path(os.environ["CI_TEMPLATE_FILE"])
issue_body_path = pathlib.Path(os.environ["ISSUE_BODY_FILE"])
comment_body_path = pathlib.Path(os.environ["COMMENT_BODY_FILE"])
labels_path = pathlib.Path(os.environ["LABELS_FILE"])

DEFAULT_LABELS = ["ci", "needs-triage"]
DEFAULT_ISSUE_TEMPLATE = textwrap.dedent("""\
- **Failed stage**: {{FAILED_STAGE}}
- **Workflow run**: {{WORKFLOW_URL}}
- **Workflow**: {{WORKFLOW_NAME}}
- **Application Version**: {{APPLICATION_VERSION}}
- **Job**: {{JOB_NAME}}
- **Commit**: {{COMMIT_SHA}}
- **Actor**: {{ACTOR}}

### Failure summary / 실패 요약
{{FAILURE_SUMMARY}}

### Relevant log excerpt / 관련 로그
{{FAILURE_LOG}}

### Extra context / 추가 정보
{{FAILURE_ENVIRONMENT}}""").strip("\n")

DEFAULT_COMMENT_TEMPLATE = textwrap.dedent("""\
Another failure detected for this stage.
- Workflow run: {{WORKFLOW_URL}}
- Job: {{WORKFLOW_NAME}} / {{JOB_NAME}}
- Commit: {{COMMIT_SHA}}
- Application Version: {{APPLICATION_VERSION}}

Summary:
{{FAILURE_SUMMARY}}

Logs:
```
{{FAILURE_LOG}}
```""").strip("\n")

def parse_template(path: pathlib.Path):
    data = {
        "labels": [],
        "issue_template": "",
        "comment_template": "",
    }
    if not path.exists():
        return data
    lines = path.read_text(encoding="utf-8").splitlines()
    key = None
    buffer = []

    def flush_block():
        nonlocal buffer, key
        if key in ("issue_template", "comment_template"):
            data[key] = textwrap.dedent("\n".join(buffer)).strip("\n")
            buffer = []
            key = None

    for line in lines:
        stripped = line.strip()
        if not stripped:
            if key in ("issue_template", "comment_template"):
                buffer.append("")
            elif key == "labels":
                key = None
            continue
        top_level = not line.startswith(" ")
        match = re.match(r"^([A-Za-z0-9_-]+):(?:\s*(\|-)?)?\s*$", stripped) if top_level else None
        if match:
            flush_block()
            key_name = match.group(1)
            if key_name == "labels":
                data["labels"] = []
                key = "labels"
            elif key_name in ("issue_template", "comment_template"):
                key = key_name
                buffer = []
            else:
                key = None
            continue
        if key == "labels" and stripped.startswith("- "):
            data["labels"].append(stripped[2:].strip())
        elif key in ("issue_template", "comment_template"):
            buffer.append(line)

    flush_block()
    return data

template_data = parse_template(ci_template_path)
labels = template_data["labels"] or DEFAULT_LABELS
issue_template = template_data["issue_template"] or DEFAULT_ISSUE_TEMPLATE
comment_template = template_data["comment_template"] or DEFAULT_COMMENT_TEMPLATE

context = {
    "FAILED_STAGE": os.environ.get("STAGE_NAME_INPUT", ""),
    "WORKFLOW_URL": os.environ.get("RUN_URL", ""),
    "WORKFLOW_NAME": os.environ.get("WORKFLOW_NAME", ""),
    "JOB_NAME": os.environ.get("JOB_NAME", ""),
    "APPLICATION_VERSION": os.environ.get("APP_VERSION_INPUT", ""),
    "COMMIT_SHA": os.environ.get("SHA", ""),
    "ACTOR": os.environ.get("ACTOR", ""),
    "FAILURE_SUMMARY": os.environ.get("FAILURE_MESSAGE_INPUT", ""),
    "FAILURE_LOG": os.environ.get("FAILURE_LOG_INPUT", ""),
    "FAILURE_ENVIRONMENT": os.environ.get("FAILURE_ENVIRONMENT_INPUT", ""),
}

def render(template: str) -> str:
    rendered = template
    for key, value in context.items():
        rendered = rendered.replace(f"{{{{{key}}}}}", value or "")
    return rendered.strip("\n")

issue_body_path.write_text(render(issue_template) + "\n", encoding="utf-8")
comment_body_path.write_text(render(comment_template) + "\n", encoding="utf-8")
labels_path.write_text("\n".join(labels), encoding="utf-8")
PY

ISSUE_BODY="$(cat "${ISSUE_BODY_FILE}")"
COMMENT_BODY="$(cat "${COMMENT_BODY_FILE}")"
TEMPLATE_LABELS=()
if [[ -s "${LABELS_FILE}" ]]; then
    while IFS= read -r line || [[ -n "${line}" ]]; do
        if [[ -n "${line}" ]]; then
            TEMPLATE_LABELS+=("${line}")
        fi
    done < "${LABELS_FILE}"
else
    TEMPLATE_LABELS=("ci" "needs-triage")
fi
rm -f "${ISSUE_BODY_FILE}" "${COMMENT_BODY_FILE}" "${LABELS_FILE}"

CI_TEMPLATE_LABELS_JSON="$(printf '%s\n' "${TEMPLATE_LABELS[@]}" | python3 <<'PY'
import json, sys
labels = [line.strip() for line in sys.stdin if line.strip()]
if not labels:
    labels = ["ci", "needs-triage"]
print(json.dumps(labels, ensure_ascii=False))
PY
)"

export ISSUE_BODY COMMENT_BODY CI_TEMPLATE_LABELS_JSON

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
)

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
labels = json.loads(os.environ.get("CI_TEMPLATE_LABELS_JSON", "[]"))
if not labels:
    labels = ["ci", "needs-triage"]
payload = {
    "title": os.environ["ISSUE_TITLE"],
    "body": os.environ["ISSUE_BODY"],
    "labels": labels
}
print(json.dumps(payload, ensure_ascii=False))
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
  local rendered_comment="${COMMENT_BODY}"
  if [[ -z "${rendered_comment}" ]]; then
    rendered_comment="Another failure detected for this stage.
- Workflow run: ${RUN_URL}
- Job: ${WORKFLOW_NAME} / ${JOB_NAME}
- Commit: ${SHA}
- Application Version: ${APP_VERSION_INPUT}

Summary:
${FAILURE_MESSAGE_INPUT}

Logs:
\`\`\`
${FAILURE_LOG_INPUT}
\`\`\`"
  fi
  export COMMENT_BODY="${rendered_comment}"

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
