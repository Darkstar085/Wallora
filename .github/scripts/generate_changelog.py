#!/usr/bin/env python3
import argparse
import datetime as dt
import os
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

GROUPS = {
    "feat": "Features",
    "fix": "Fixes",
    "ui": "UI / UX",
    "refactor": "Refactoring",
    "perf": "Performance",
    "build": "Build",
    "ci": "CI / Build",
    "docs": "Documentation",
    "test": "Testing",
    "chore": "Maintenance",
    "style": "Maintenance",
    "revert": "Reverted",
}

SKIP_PREFIXES = (
    "chore: bump version to ",
    "release: bump app version to ",
    "docs: update changelog for ",
)

GROUP_ORDER = [
    "Features",
    "Fixes",
    "UI / UX",
    "Performance",
    "Refactoring",
    "CI / Build",
    "Build",
    "Documentation",
    "Testing",
    "Maintenance",
    "Reverted",
    "Other",
]


def run(*args):
    return subprocess.check_output(args, cwd=ROOT, text=True).strip()


def previous_tag():
    tags = run(
        "git",
        "tag",
        "--list",
        "v*",
        "--sort=-version:refname",
    ).splitlines()
    return tags[0] if tags else None


def parse_commit(record):
    parts = record.split("\x1f")
    sha = parts[0].strip() if parts else ""
    subject = parts[1].strip() if len(parts) > 1 else ""
    body = parts[2].strip() if len(parts) > 2 else ""

    if any(subject.lower().startswith(prefix) for prefix in SKIP_PREFIXES):
        return None

    match = re.match(r"^([a-zA-Z]+)(?:\([^)]*\))?!?:\s*(.+)$", subject)
    kind, title = (
        (match.group(1).lower(), match.group(2).strip())
        if match
        else ("other", subject)
    )

    details = []
    for line in body.splitlines():
        line = line.strip()
        if not line or line.startswith("Signed-off-by:"):
            continue
        line = re.sub(r"^[-*+]\s+", "", line)
        if line:
            details.append(line)

    return GROUPS.get(kind, "Other"), title, details, sha


def main():
    parser = argparse.ArgumentParser(
        description="Generate a release changelog from Git history."
    )
    parser.add_argument("--version", required=True)
    parser.add_argument("--date", default=dt.date.today().isoformat())
    parser.add_argument("--output", default="CHANGELOG.md")
    parser.add_argument("--from-tag")
    args = parser.parse_args()

    start = args.from_tag or previous_tag()
    commit_range = f"{start}..HEAD" if start else "HEAD"
    raw = run(
        "git",
        "log",
        commit_range,
        "--no-merges",
        "--format=%H%x1f%s%x1f%b%x1e",
    )

    grouped = {}
    for record in raw.split("\x1e"):
        if not record.strip():
            continue
        parsed = parse_commit(record)
        if parsed:
            grouped.setdefault(parsed[0], []).append(parsed[1:])

    repo = os.environ.get("GITHUB_REPOSITORY", "")
    heading = f"## [{args.version}] - {args.date}"
    section = [heading, ""]

    for group in GROUP_ORDER:
        entries = grouped.get(group)
        if not entries:
            continue

        section.extend([f"### {group}", ""])
        for title, details, sha in entries:
            short_sha = sha[:7]
            commit = (
                f"[{short_sha}](https://github.com/{repo}/commit/{sha})"
                if repo and sha
                else short_sha
            )
            section.append(f"- {title} ({commit})")
            section.extend(f"  - {detail}" for detail in details)
        section.append("")

    if len(section) == 2:
        section.extend(["No user-facing changes.", ""])

    Path(args.output).write_text(
        "# Changelog\n\n" + "\n".join(section) + "\n",
        encoding="utf-8",
    )


if __name__ == "__main__":
    main()
