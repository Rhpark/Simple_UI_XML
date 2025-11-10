#!/usr/bin/env python3
import re
import sys
from pathlib import Path

if len(sys.argv) < 3:
    print("Usage: extract_gradle_failure.py <input_log> <output_snippet>", file=sys.stderr)
    sys.exit(1)

input_path = Path(sys.argv[1])
output_path = Path(sys.argv[2])

if not input_path.exists():
    print(f"[extract_gradle_failure] Input log not found: {input_path}", file=sys.stderr)
    sys.exit(0)

ansi_re = re.compile(r"\x1B\[[0-9;]*[A-Za-z]")

raw_lines = input_path.read_text(encoding="utf-8", errors="ignore").splitlines()
clean_lines = [ansi_re.sub("", line) for line in raw_lines]

def last_failed_block(max_lines=100):
    failed_pattern = re.compile(r"failed,", re.IGNORECASE)
    last_idx = None

    for idx, line in enumerate(clean_lines):
        if failed_pattern.search(line):
            last_idx = idx

    if last_idx is None:
        return []

    end = min(len(clean_lines), last_idx + max_lines)
    block = clean_lines[last_idx:end]
    return block

result_lines = last_failed_block()
if not result_lines:
    failure_block = []
    for idx, line in enumerate(clean_lines):
        if "FAILURE:" in line or "BUILD FAILED" in line:
            failure_block = clean_lines[idx: idx + 200]
            break
    result_lines = failure_block or clean_lines[-200:]

exclude_substrings = (
    "Run with --stacktrace",
    "--info or --debug option",
    "--debug option",
    "* Get more help",
)

filtered = []
for line in result_lines:
    stripped = line.strip()
    if stripped and any(sub in stripped for sub in exclude_substrings):
        continue
    filtered.append(line)

while filtered and filtered[0] == "":
    filtered = filtered[1:]
while filtered and filtered[-1] == "":
    filtered = filtered[:-1]

output_path.write_text("\n".join(filtered), encoding="utf-8", errors="ignore")
