#!/usr/bin/env python3
"""Sync Google Play listing texts from docs/marketing/play-listing.md."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT: Path = Path(__file__).resolve().parents[1]
SOURCE: Path = ROOT / "docs" / "marketing" / "play-listing.md"
LISTINGS: Path = ROOT / "androidApp" / "src" / "main" / "play" / "listings"
LOCALE_HEADER: re.Pattern[str] = re.compile(r"^## ([a-z]{2}-[A-Z]{2})\s*$", re.MULTILINE)
FIELD_HEADER: re.Pattern[str] = re.compile(r"^### (title|short-description|full-description)\s*$", re.MULTILINE)
FIELD_FILES: dict[str, str] = {
    "title": "title.txt",
    "short-description": "short-description.txt",
    "full-description": "full-description.txt",
}
LIMITS: dict[str, int] = {
    "title": 30,
    "short-description": 80,
    "full-description": 4000,
}


def read_source() -> str:
    if not SOURCE.is_file():
        raise FileNotFoundError(f"Missing {SOURCE}")
    return SOURCE.read_text(encoding="utf-8")


def parse_listings(text: str) -> dict[str, dict[str, str]]:
    locales: list[tuple[str, int]] = [(match.group(1), match.start()) for match in LOCALE_HEADER.finditer(text)]
    if not locales:
        raise ValueError(f"No locale sections found in {SOURCE}")
    parsed: dict[str, dict[str, str]] = {}
    for index, (locale, start) in enumerate(locales):
        end: int = locales[index + 1][1] if index + 1 < len(locales) else len(text)
        section: str = text[start:end]
        fields: dict[str, str] = {}
        field_matches: list[re.Match[str]] = list(FIELD_HEADER.finditer(section))
        for field_index, field_match in enumerate(field_matches):
            field_name: str = field_match.group(1)
            field_start: int = field_match.end()
            field_end: int = (
                field_matches[field_index + 1].start()
                if field_index + 1 < len(field_matches)
                else len(section)
            )
            value: str = section[field_start:field_end].strip()
            if not value:
                raise ValueError(f"Empty {field_name} for locale {locale} in {SOURCE}")
            fields[field_name] = value
        missing: set[str] = set(FIELD_FILES) - set(fields)
        if missing:
            raise ValueError(f"Missing fields {sorted(missing)} for locale {locale} in {SOURCE}")
        parsed[locale] = fields
    return parsed


def validate_field(locale: str, field_name: str, value: str) -> None:
    limit: int = LIMITS[field_name]
    if len(value) > limit:
        print(
            f"Warning: {locale}/{field_name} is {len(value)} chars (Play limit {limit})",
            file=sys.stderr,
        )


def write_listings(listings: dict[str, dict[str, str]]) -> None:
    for locale, fields in listings.items():
        locale_dir: Path = LISTINGS / locale
        locale_dir.mkdir(parents=True, exist_ok=True)
        for field_name, file_name in FIELD_FILES.items():
            value: str = fields[field_name]
            validate_field(locale, field_name, value)
            target: Path = locale_dir / file_name
            target.write_text(value + "\n", encoding="utf-8")
            print(f"Wrote {target.relative_to(ROOT)} ({len(value)} chars)")


def main() -> int:
    listings: dict[str, dict[str, str]] = parse_listings(read_source())
    write_listings(listings)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
