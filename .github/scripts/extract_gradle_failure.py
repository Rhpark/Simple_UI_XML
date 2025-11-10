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

def find_first_index(predicate):
    for idx, line in enumerate(clean_lines):
        if predicate(line):
            return idx
    return None

result_lines = []

idx_failed_comma = find_first_index(lambda line: "failed," in line.lower())
idx_failed = find_first_index(lambda line: "FAILED" in line)

selected_idx = None
if idx_failed_comma is not None and idx_failed is not None:
    selected_idx = idx_failed_comma if idx_failed_comma <= idx_failed else idx_failed
else:
    selected_idx = idx_failed_comma if idx_failed_comma is not None else idx_failed

if selected_idx is not None:
    result_lines = slice_from(selected_idx)

if not result_lines:
    idx = find_first_index(lambda line: "FAILURE:" in line)
    if idx is not None:
        result_lines = slice_from(idx)

if not result_lines:
    idx = find_first_index(lambda line: "BUILD FAILED" in line)
    if idx is not None:
        result_lines = slice_from(idx)

if not result_lines:
    start = max(0, len(clean_lines) - 100)
    result_lines = clean_lines[start:]

while result_lines and result_lines[0] == "":
    result_lines = result_lines[1:]
while result_lines and result_lines[-1] == "":
    result_lines = result_lines[:-1]

output_path.write_text("\n".join(result_lines), encoding="utf-8", errors="ignore")
