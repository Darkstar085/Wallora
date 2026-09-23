#!/usr/bin/env python3
import argparse
import os
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def run(*args, check=True, capture=True):
    return subprocess.run(
        args,
        cwd=ROOT,
        check=check,
        text=True,
        capture_output=capture,
    )


def set_output(name, value):
    output = os.environ.get("GITHUB_OUTPUT")
    if output:
        with open(output, "a", encoding="utf-8") as file:
            file.write(f"{name}={value}\n")


def version():
    text = (ROOT / "version.properties").read_text(encoding="utf-8")
    name = re.search(r"^versionName\s*=\s*(.+)$", text, re.M)
    code = re.search(r"^versionCode\s*=\s*(\d+)$", text, re.M)
    if not name or not code:
        raise SystemExit("Could not determine app version.")
    return name.group(1).strip(), int(code.group(1))


def version_changed():
    before = os.environ.get("GITHUB_EVENT_BEFORE", "")
    if not before or set(before) == {"0"}:
        return True
    try:
        run("git", "cat-file", "-e", f"{before}^{{commit}}")
        diff = run("git", "diff", "--unified=0", before, "HEAD", "--", "version.properties").stdout
    except subprocess.CalledProcessError:
        try:
            diff = run("git", "diff", "--unified=0", "HEAD^", "HEAD", "--", "version.properties").stdout
        except subprocess.CalledProcessError:
            print(f"Could not compare {before} with HEAD; assuming a release is needed.")
            return True
    return bool(re.search(r"^[+-](versionName|versionCode)\s*=", diff, re.M))


def release_exists(tag):
    repo = os.environ.get("GITHUB_REPOSITORY", "")
    result = run("gh", "release", "view", tag, "--repo", repo, check=False)
    return result.returncode == 0


def changelog_section(version_name):
    text = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")
    pattern = rf"^## \[{re.escape(version_name)}\][^\n]*\n.*?(?=^## \[|\Z)"
    match = re.search(pattern, text, re.M | re.S)
    if not match:
        raise SystemExit(f"No changelog entry found for {version_name}.")
    return match.group(0).strip()


def release_notes(version_name):
    section = changelog_section(version_name)
    points = []
    for line in section.splitlines():
        if not line.startswith("- "):
            continue
        point = line[2:].strip()
        point = re.sub(r"\s+\(\[[0-9a-fA-F]{7}\]\([^)]*\)\)$", "", point)
        point = re.sub(r"\s+\([0-9a-fA-F]{7}\)$", "", point)
        if point and point not in points:
            points.append(point)
    if not points:
        raise SystemExit(f"No release points found for {version_name}.")
    return "\n".join(f"- {point}" for point in points) + "\n"


def main():
    p = argparse.ArgumentParser(description="Prepare and publish a Wallora release.")
    p.add_argument("--prepare", action="store_true")
    p.add_argument("--publish", action="store_true")
    args = p.parse_args()
    name, _ = version()
    tag = f"v{name}"
    changed = version_changed()
    set_output("release_needed", "true" if changed else "false")
    if not changed:
        print("Version metadata did not change; nothing to release.")
        return
    if args.prepare:
        changelog = ROOT / "CHANGELOG.md"
        marker = f"## [{name}]"
        if changelog.exists() and marker in changelog.read_text(encoding="utf-8"):
            print(f"Changelog entry for {name} already exists; keeping it.")
        else:
            subprocess.run(["python", ".github/scripts/generate_changelog.py", "--version", name], cwd=ROOT, check=True)
    if args.publish:
        if not (ROOT / "CHANGELOG.md").exists():
            raise SystemExit("CHANGELOG.md is missing. Run .github/generate_changelog.py first.")
        notes_file = ROOT / "RELEASE_NOTES.md"
        notes_file.write_text(release_notes(name), encoding="utf-8")
        repo = os.environ["GITHUB_REPOSITORY"]
        if release_exists(tag):
            print(f"Release {tag} already exists; updating its notes.")
            subprocess.run(["gh", "release", "edit", tag, "--repo", repo, "--title", f"Wallora v{name}", "--notes-file", str(notes_file)], cwd=ROOT, check=True)
            return
        apk_dir = ROOT / "app/build/outputs/apk/release"
        apk = next(apk_dir.glob("*.apk"), None) if apk_dir.exists() else None
        if not apk:
            raise SystemExit("No release APK was produced. Build the release APK before publishing.")
        release_apk = ROOT / f"Wallora_v{name}.apk"
        release_apk.write_bytes(apk.read_bytes())
        subprocess.run(["gh", "release", "create", tag, str(release_apk), "--repo", repo, "--title", f"Wallora v{name}", "--notes-file", str(notes_file)], cwd=ROOT, check=True)


if __name__ == "__main__":
    main()
