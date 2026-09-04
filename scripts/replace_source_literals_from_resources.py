#!/usr/bin/env python3
"""Replace exact Java Activity UI literals with existing string resources."""

from __future__ import annotations

import argparse
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

CJK = re.compile(r"[\u3400-\u9fff]")
JAVA_STRING = re.compile(r'"(?:\\.|[^"\\])*"')
SINKS = (
    "setText", "setHint", "setTitle", "setMessage", "setContentDescription",
    "Toast.makeText", "showBanner", "renderSurveyStatus", "showSurveyGenerationFailure",
    "appendLog",
)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--apply", action="store_true")
    parser.add_argument("--activity", required=True, type=Path)
    parser.add_argument("--strings", required=True, type=Path)
    args = parser.parse_args()
    root = ET.parse(args.strings).getroot()
    by_value = {
        "".join(node.itertext()): node.attrib["name"]
        for node in root
        if node.tag == "string" and "name" in node.attrib
    }
    original = args.activity.read_text(encoding="utf-8")
    changed = 0
    output: list[str] = []
    for line in original.splitlines(keepends=True):
        if not CJK.search(line) or not any(sink in line for sink in SINKS):
            output.append(line)
            continue

        def replace(match: re.Match[str]) -> str:
            nonlocal changed
            try:
                value = json.loads(match.group(0))
            except json.JSONDecodeError:
                return match.group(0)
            key = by_value.get(value)
            if not key:
                return match.group(0)
            changed += 1
            return f"getString(R.string.{key})"

        output.append(JAVA_STRING.sub(replace, line))
    updated = "".join(output)
    if args.apply and updated != original:
        args.activity.write_text(updated, encoding="utf-8")
    print(f"exact source literal replacements: {changed} ({'applied' if args.apply else 'dry run'})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
