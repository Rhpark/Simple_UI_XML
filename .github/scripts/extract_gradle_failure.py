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

def slice_from(index, max_lines=100):
    end = min(len(clean_lines), index + max_lines)
    return clean_lines[index:end]

def find_last_index(predicate):
    last = None
    for idx, line in enumerate(clean_lines):
        if predicate(line):
            last = idx
    return last

result_lines = []

idx = find_last_index(lambda line: "failed," in line.lower())
if idx is not None:
    result_lines = slice_from(idx)

if not result_lines:
    idx = find_last_index(lambda line: "> Task" in line and "FAILED" in line)
    if idx is not None:
        result_lines = slice_from(idx)

if not result_lines:
    idx = find_last_index(lambda line: "FAILED" in line)
    if idx is not None:
        result_lines = slice_from(idx)

if not result_lines:
    idx = find_last_index(lambda line: "FAILURE:" in line)
    if idx is not None:
        result_lines = slice_from(idx)

if not result_lines:
    idx = find_last_index(lambda line: "BUILD FAILED" in line)
    if idx is not None:
        result_lines = slice_from(idx)

if not result_lines:
    start = max(0, len(clean_lines) - 100)
    result_lines = clean_lines[start:]

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
