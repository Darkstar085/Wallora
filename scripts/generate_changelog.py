#!/usr/bin/env python3
import argparse
import datetime as dt
import re
import subprocess
from pathlib import Path

GROUPS = {
    "feat": "Features", "fix": "Fixes", "ui": "UI / UX", "refactor": "Refactoring",
    "perf": "Performance", "build": "Build", "ci": "CI / Build", "docs": "Documentation",
    "test": "Testing", "chore": "Maintenance", "style": "Maintenance", "revert": "Reverted",
}
SKIP_PREFIXES = ("chore: bump version to ", "docs: update changelog for ")

def run(*args):
    return subprocess.check_output(args, text=True).strip()

def previous_tag():
    tags = [t for t in run("git", "tag", "--list", "v*", "--sort=-version:refname").splitlines() if t]
    return tags[0] if tags else None

def parse_commit(record):
    parts = record.split("\x1f")
    sha = parts[0] if parts else ""
    subject = parts[1].strip() if len(parts) > 1 else ""
    body = parts[2].strip() if len(parts) > 2 else ""
    if any(subject.lower().startswith(p) for p in SKIP_PREFIXES):
        return None
    m = re.match(r"^([a-zA-Z]+)(?:\([^)]*\))?!?:\s*(.+)$", subject)
    kind, title = (m.group(1).lower(), m.group(2).strip()) if m else ("other", subject)
    details = []
    for line in body.splitlines():
        line = line.strip()
        if not line or line.startswith("Signed-off-by:"):
            continue
        line = re.sub(r"^[-*+]\s+", "", line)
        if line:
            details.append(line)
    return GROUPS.get(kind, "Other"), title, details

def main():
    p = argparse.ArgumentParser(description="Generate a release changelog from Git history.")
    p.add_argument("--version", required=True)
    p.add_argument("--date", default=dt.date.today().isoformat())
    p.add_argument("--output", default="CHANGELOG.md")
    p.add_argument("--from-tag")
    args = p.parse_args()
    start = args.from_tag or previous_tag()
    rng = f"{start}..HEAD" if start else "HEAD"
    raw = run("git", "log", rng, "--no-merges", "--format=%H%x1f%s%x1f%b%x1e")
    grouped = {}
    for record in raw.split("\x1e"):
        if not record.strip():
            continue
        parsed = parse_commit(record)
        if parsed:
            grouped.setdefault(parsed[0], []).append(parsed[1:])

    path = Path(args.output)
    old = path.read_text(encoding="utf-8") if path.exists() else "# Changelog\n"
    heading = f"## [{args.version}] - {args.date}"
    if heading in old:
        raise SystemExit(f"Changelog entry already exists: {heading}")

    order = ["Features", "Fixes", "UI / UX", "Performance", "Refactoring", "CI / Build", "Build", "Documentation", "Testing", "Maintenance", "Reverted", "Other"]
    section = [heading, ""]
    for group in order:
        entries = grouped.get(group)
        if not entries:
            continue
        section += [f"### {group}", ""]
        for title, details in entries:
            section.append(f"- {title}")
            section.extend(f"  - {detail}" for detail in details)
        section.append("")
    if len(section) == 2:
        section += ["No user-facing changes.", ""]

    rest = old[len("# Changelog"):].lstrip("\n") if old.startswith("# Changelog") else old
    path.write_text("# Changelog\n\n" + "\n".join(section) + "\n" + rest, encoding="utf-8")

if __name__ == "__main__":
    main()
