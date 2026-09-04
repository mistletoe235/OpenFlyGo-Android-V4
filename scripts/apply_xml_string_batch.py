#!/usr/bin/env python3
"""Move exact visible XML literals into Android string resources.

Reads UTF-8 TSV rows from stdin: resource_key, source text, English text.
Use a single dash as the English text for a non-translatable resource.
"""

from __future__ import annotations

import argparse
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from xml.sax.saxutils import escape

ATTRIBUTE = re.compile(r'android:(text|hint|contentDescription|title|summary)="([^"]*)"')


def resources(path: Path) -> dict[str, str]:
    return {
        node.attrib["name"]: "".join(node.itertext())
        for node in ET.parse(path).getroot()
        if node.tag == "string" and "name" in node.attrib
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--res-dir", required=True, type=Path)
    parser.add_argument("--allow-missing", action="store_true", help="append resources even when no XML literal matches")
    args = parser.parse_args()
    rows: list[tuple[str, str, str | None]] = []
    for line_number, raw in enumerate(sys.stdin, 1):
        line = raw.rstrip("\n")
        if not line or line.startswith("#"):
            continue
        fields = line.split("\t")
        if len(fields) != 3:
            raise SystemExit(f"line {line_number}: expected 3 TSV fields")
        key, source, english = fields
        rows.append((key, source, None if english == "-" else english))

    zh_path = args.res_dir / "values" / "strings.xml"
    en_path = args.res_dir / "values-en" / "strings.xml"
    zh = resources(zh_path)
    en = resources(en_path)
    by_source = {value: key for key, value in zh.items()}
    mapping: dict[str, tuple[str, str | None]] = {}
    for proposed_key, source, english in rows:
        key = by_source.get(source, proposed_key)
        if key in zh and zh[key] != source:
            raise SystemExit(f"resource key collision: {key}")
        if source in mapping:
            existing_key, existing_english = mapping[source]
            if existing_english != english:
                raise SystemExit(f"source text collision: {source}")
            # Identical visible text must resolve to one resource key even when
            # callers propose different semantic names for duplicate XML uses.
            continue
        mapping[source] = (key, english)

    seen: set[str] = set()
    changed_files = 0
    candidates = sorted(args.res_dir.glob("layout*/**/*.xml")) + sorted(args.res_dir.glob("menu*/**/*.xml"))
    for path in candidates:
        original = path.read_text(encoding="utf-8")

        def replace(match: re.Match[str]) -> str:
            source = match.group(2)
            if source not in mapping:
                return match.group(0)
            seen.add(source)
            return f'android:{match.group(1)}="@string/{mapping[source][0]}"'

        updated = ATTRIBUTE.sub(replace, original)
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            changed_files += 1

    missing = sorted(source for source in mapping if source not in seen and source not in by_source)
    if missing and not args.allow_missing:
        for source in missing:
            print(f"literal not found: {source}", file=sys.stderr)
        return 1

    add_zh: list[str] = []
    add_en: list[str] = []
    for source, (key, english) in mapping.items():
        if key not in zh:
            marker = ' translatable="false"' if english is None else ""
            add_zh.append(f'    <string name="{key}"{marker}>{escape(source)}</string>')
        if english is not None and key not in en:
            add_en.append(f'    <string name="{key}">{escape(english)}</string>')

    for path, lines in ((zh_path, add_zh), (en_path, add_en)):
        if lines:
            original = path.read_text(encoding="utf-8")
            path.write_text(original.replace("</resources>", "\n".join(lines) + "\n</resources>"), encoding="utf-8")
    print(f"mapped={len(mapping)} changed_files={changed_files} added_zh={len(add_zh)} added_en={len(add_en)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
