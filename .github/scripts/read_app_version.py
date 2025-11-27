#!/usr/bin/env python3
"""Extract the library/app version used for release validation."""

from __future__ import annotations

import re
import sys
from pathlib import Path


def _read_version(path: str, pattern: str) -> str | None:
    file_path = Path(path)
    if not file_path.is_file():
        return None
    text = file_path.read_text(encoding="utf-8")
    match = re.search(pattern, text)
    return match.group(1) if match else None


def main() -> int:
    # Try simple_core first (main library module)
    version = _read_version(
        "simple_core/build.gradle.kts",
        r'version\s*=\s*"([^"]+)"',
    )
    # Fallback to simple_xml
    if version is None:
        version = _read_version(
            "simple_xml/build.gradle.kts",
            r'version\s*=\s*"([^"]+)"',
        )
    # Fallback to gradle/libs.versions.toml
    if version is None:
        version = _read_version(
            "gradle/libs.versions.toml",
            r'appVersion\s*=\s*"([^"]+)"',
        )
    if version is None:
        return 1
    print(version)
    return 0


if __name__ == "__main__":
    sys.exit(main())
