#!/usr/bin/env python3
"""
filter-sarif.py
===============
Repo   : core-cloud-workflow-maven-docker-actions
Owner  : Core Cloud Platform – Team Sauron

Sourced from the Core Cloud SAST workflow pattern.

Purpose:
  Filter SARIF results to exclude findings from test fixtures / generated folders
  before uploading to GitHub Advanced Security. This reduces noise from dummy
  services and build artifacts (e.g. tests/, target/, node_modules/).

Provenance:
  Pattern based on the Core Cloud SAST repo approach (Checkov SARIF filtering).

Usage:
    python3 scripts/filter-sarif.py <path-to-sarif-file>

Edits the SARIF file in-place.
"""

import json
import sys
from pathlib import Path
from typing import Any, Dict, List

# Paths whose findings should be suppressed before upload.
# Extend this list if additional test/fixture directories are added.
EXCLUDED_PATH_PREFIXES = [
    "tests/",
    "test/",
    "target/",
    "node_modules/",
    ".mvn/",
    ".github/",
]


def should_exclude(uri: str) -> bool:
    """
    Determine whether a SARIF artifact URI should be excluded.

    SARIF URIs can be:
      - relative: tests/dummy-service/...
      - absolute: /home/runner/work/repo/tests/...
      - file URI: file:///home/runner/work/repo/tests/...
    """
    normalized = uri.replace("file://", "").lstrip("/")
    return any(normalized.startswith(prefix) for prefix in EXCLUDED_PATH_PREFIXES)


def main() -> int:
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} <sarif-file>", file=sys.stderr)
        return 2

    sarif_path = Path(sys.argv[1])

    if not sarif_path.exists():
        print(f"ERROR: SARIF file not found: {sarif_path}", file=sys.stderr)
        return 1

    with sarif_path.open("r", encoding="utf-8") as f:
        sarif: Dict[str, Any] = json.load(f)

    runs: List[Dict[str, Any]] = sarif.get("runs", [])
    if not runs:
        print("No runs found in SARIF; nothing to filter.")
        return 0

    total_results = 0
    kept_results = 0

    for run in runs:
        results = run.get("results", [])
        total_results += len(results)

        filtered: List[Dict[str, Any]] = []

        for result in results:
            locations = result.get("locations", [])
            exclude = False

            for loc in locations:
                phys = loc.get("physicalLocation", {})
                artifact = phys.get("artifactLocation", {})
                uri = artifact.get("uri", "")
                if uri and should_exclude(uri):
                    exclude = True
                    break

            if not exclude:
                filtered.append(result)

        run["results"] = filtered
        kept_results += len(filtered)

    removed = total_results - kept_results
    print(f"SARIF filter: {total_results} findings → {kept_results} kept, {removed} removed")

    with sarif_path.open("w", encoding="utf-8") as f:
        json.dump(sarif, f, indent=2)

    print(f"Filtered SARIF written to: {sarif_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())