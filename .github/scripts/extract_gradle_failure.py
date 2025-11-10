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

def find_index(pred):
    for idx, line in enumerate(clean_lines):
        if pred(line):
            return idx
    return -1

failure_block = []
start_idx = find_index(lambda s: "FAILURE:" in s)
if start_idx == -1:
    start_idx = find_index(lambda s: "BUILD FAILED" in s)

if start_idx != -1:
    i = start_idx
    while i < len(clean_lines):
        failure_block.append(clean_lines[i])
        if "BUILD FAILED" in clean_lines[i]:
            if i + 1 < len(clean_lines) and "actionable tasks" in clean_lines[i + 1]:
                failure_block.append(clean_lines[i + 1])
            break
        i += 1

task_block = []
task_idx = find_index(lambda s: s.startswith("> Task ") and "FAILED" in s)
if task_idx != -1:
    j = task_idx
    while j < len(clean_lines):
        if j > task_idx and clean_lines[j].startswith("FAILURE:"):
            break
        task_block.append(clean_lines[j])
        j += 1

result_lines = failure_block[:]
if task_block:
    if result_lines and result_lines[-1].strip():
        result_lines.append("")
    result_lines.extend(task_block)

if not result_lines:
    result_lines = clean_lines[-200:]

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
