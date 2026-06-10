#!/usr/bin/env python3
"""Parse and update CHANGELOG.md for Play Store release notes."""
from __future__ import annotations

import argparse
import re
import sys
from datetime import date
from pathlib import Path

ROOT: Path = Path(__file__).resolve().parents[1]
CHANGELOG: Path = ROOT / "CHANGELOG.md"
MAX_LENGTH: int = 500
DEFAULT_RELEASE_NOTES: dict[str, str] = {
    "ru-RU": "Правка багов и улучшения",
    "en-US": "Bug fixes and improvements",
}
VERSION_HEADER: re.Pattern[str] = re.compile(
    r"^## \[(?P<version>[^\]]+)\](?:\s*-\s*(?P<date>\d{4}-\d{2}-\d{2}))?\s*$",
    re.MULTILINE,
)
LOCALE_HEADER: re.Pattern[str] = re.compile(r"^### (RU|EN)\s*$", re.MULTILINE)


def read_changelog() -> str:
    if not CHANGELOG.is_file():
        raise FileNotFoundError(f"Missing {CHANGELOG}")
    return CHANGELOG.read_text(encoding="utf-8")


def write_changelog(text: str) -> None:
    CHANGELOG.write_text(text, encoding="utf-8")


def split_sections(text: str) -> list[tuple[str, str, str | None]]:
    matches: list[re.Match[str]] = list(VERSION_HEADER.finditer(text))
    sections: list[tuple[str, str, str | None]] = []
    for index, match in enumerate(matches):
        start: int = match.end()
        end: int = matches[index + 1].start() if index + 1 < len(matches) else len(text)
        sections.append((match.group("version"), match.group("date"), text[start:end]))
    return sections


def find_section(text: str, version: str) -> tuple[str, str | None, str] | None:
    for section_version, section_date, body in split_sections(text):
        if section_version == version:
            return section_version, section_date, body
    return None


def default_release_notes(version: str, reason: str) -> dict[str, str]:
    print(f"Version [{version}] {reason}, using default release notes")
    return dict(DEFAULT_RELEASE_NOTES)


def extract_locale(body: str, locale: str) -> str:
    lines: list[str] = body.splitlines()
    collecting: bool = False
    collected: list[str] = []
    for line in lines:
        locale_match: re.Match[str] | None = LOCALE_HEADER.match(line)
        if locale_match is not None:
            collecting = locale_match.group(1) == locale
            continue
        if line.startswith("## "):
            break
        if collecting:
            stripped: str = line.strip()
            if not stripped:
                continue
            if stripped.startswith("- "):
                collected.append(f"• {stripped[2:].strip()}")
            else:
                collected.append(stripped)
    return "\n".join(collected).strip()


def trim_notes(notes: str) -> str:
    if len(notes) <= MAX_LENGTH:
        return notes
    return notes[: MAX_LENGTH - 1] + "…"


def extract_release_notes(version: str) -> dict[str, str]:
    text: str = read_changelog()
    section: tuple[str, str | None, str] | None = find_section(text, version)
    if section is None:
        return default_release_notes(version, "not found in CHANGELOG.md")
    _, _, body = section
    ru_notes: str = extract_locale(body, "RU")
    en_notes: str = extract_locale(body, "EN")
    if not ru_notes and not en_notes:
        return default_release_notes(version, "has empty RU and EN release notes in CHANGELOG.md")
    if not ru_notes:
        ru_notes = en_notes
    if not en_notes:
        en_notes = ru_notes
    return {
        "ru-RU": trim_notes(ru_notes),
        "en-US": trim_notes(en_notes),
    }


def bump_application_version(version: str) -> None:
    if not re.fullmatch(r"\d+\.\d+\.\d+", version):
        raise ValueError(f"Invalid version: '{version}' (expected X.Y.Z)")
    major, minor, patch = version.split(".")
    application_file: Path = ROOT / "buildSrc" / "src" / "main" / "kotlin" / "Application.kt"
    text: str = application_file.read_text(encoding="utf-8")
    updated, count = re.subn(
        r"Version\(\d+, \d+, \d+\)",
        f"Version({major}, {minor}, {patch})",
        text,
        count=1,
    )
    if count != 1:
        raise ValueError(f"Failed to update version in {application_file}")
    application_file.write_text(updated, encoding="utf-8")
    print(f"Bumped version to {major}.{minor}.{patch}")


def finalize_version(version: str, release_date: str | None = None) -> bool:
    text: str = read_changelog()
    section: tuple[str, str | None, str] | None = find_section(text, version)
    if section is None:
        print(f"Version [{version}] not found in CHANGELOG.md, skipping finalize")
        return False
    _, section_date, _ = section
    if section_date:
        print(f"Version [{version}] already has release date {section_date}")
        return False
    dated_header: str = f"## [{version}] - {release_date or date.today().isoformat()}"

    def replacer(match: re.Match[str]) -> str:
        if match.group("version") != version:
            return match.group(0)
        return dated_header

    updated: str = VERSION_HEADER.sub(replacer, text)
    if updated == text:
        raise ValueError(f"Failed to finalize version [{version}] in CHANGELOG.md")
    write_changelog(updated)
    return True


def write_play_release_notes(version: str, track: str, output_dir: Path) -> None:
    notes_by_locale: dict[str, str] = extract_release_notes(version)
    for locale, notes in notes_by_locale.items():
        locale_dir: Path = output_dir / locale
        locale_dir.mkdir(parents=True, exist_ok=True)
        (locale_dir / f"{track}.txt").write_text(notes + "\n", encoding="utf-8")
        (locale_dir / "default.txt").write_text(notes + "\n", encoding="utf-8")
        print(f"Wrote {locale}/{track}.txt ({len(notes)} chars)")


def main() -> int:
    parser: argparse.ArgumentParser = argparse.ArgumentParser(description="CHANGELOG helper for Play releases")
    subparsers = parser.add_subparsers(dest="command", required=True)
    extract_parser = subparsers.add_parser("extract", help="Print release notes for a version")
    extract_parser.add_argument("--version", required=True)
    extract_parser.add_argument("--locale", choices=["ru-RU", "en-US"], required=True)
    bump_parser = subparsers.add_parser("bump-version", help="Update Application.kt version")
    bump_parser.add_argument("--version", required=True)
    finalize_parser = subparsers.add_parser("finalize", help="Add release date to a version section")
    finalize_parser.add_argument("--version", required=True)
    finalize_parser.add_argument("--date")
    play_parser = subparsers.add_parser("write-play-notes", help="Write GPP release note files")
    play_parser.add_argument("--version", required=True)
    play_parser.add_argument("--track", default="alpha")
    play_parser.add_argument(
        "--output-dir",
        default=str(ROOT / "androidApp" / "src" / "main" / "play" / "release-notes"),
    )
    args: argparse.Namespace = parser.parse_args()
    if args.command == "extract":
        notes: dict[str, str] = extract_release_notes(args.version)
        sys.stdout.write(notes[args.locale])
        return 0
    if args.command == "bump-version":
        bump_application_version(args.version)
        return 0
    if args.command == "finalize":
        finalized: bool = finalize_version(args.version, args.date)
        if finalized:
            print(f"Finalized [{args.version}] in CHANGELOG.md")
        return 0
    if args.command == "write-play-notes":
        write_play_release_notes(args.version, args.track, Path(args.output_dir))
        return 0
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
