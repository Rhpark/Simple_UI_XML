#!/usr/bin/env python3
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

patterns_start = (
    "FAILURE:",
    "FAILURE: Build failed",
    "BUILD FAILED in",
    "Execution failed for task",
)
patterns_alt = (
    "> Task :",
)
patterns_end = ("Error:", "BUILD FAILED", "FAILURE:", "Caused by:", "BUILD SUCCESSFUL")

lines = input_path.read_text(encoding="utf-8", errors="ignore").splitlines()

snippet_lines = []
capture = False

for line in lines:
    if not capture and (any(pat in line for pat in patterns_start) or (snippet_lines == [] and any(pat in line for pat in patterns_alt))):
        capture = True

    if capture:
        snippet_lines.append(line)
        if any(pat in line for pat in patterns_end):
            break

if not snippet_lines:
    snippet_lines = lines[-200:]

output_path.write_text("\n".join(snippet_lines), encoding="utf-8", errors="ignore")
