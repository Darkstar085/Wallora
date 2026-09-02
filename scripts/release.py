#!/usr/bin/env python3
import argparse
import os
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def run(*args, check=True, capture=True):
    result = subprocess.run(args, cwd=ROOT, check=check, text=True, capture_output=capture)
    return result.stdout if capture else result

def version():
    text = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
    name = re.search(r'versionName\s*=\s*"([^"]+)"', text)
    code = re.search(r'versionCode\s*=\s*(\d+)', text)
    if not name or not code:
        raise SystemExit("Could not determine app version.")
    return name.group(1), int(code.group(1))

def version_changed():
    before = os.environ.get("GITHUB_EVENT_BEFORE", "")
    if not before or set(before) == {"0"}:
        return True
    diff = run("git", "diff", "--unified=0", before, "HEAD", "--", "app/build.gradle.kts")
    return bool(re.search(r"^[+-].*version(Name|Code)", diff, re.M))

def release_exists(tag):
    repo = os.environ.get("GITHUB_REPOSITORY", "")
    result = run("gh", "release", "view", tag, "--repo", repo, check=False)
    return result.returncode == 0

def previous_release_tag(version_name):
    tags = [t for t in run("git", "tag", "--list", "v*", "--sort=-version:refname").splitlines() if t]
    current = f"v{version_name}"
    if current in tags:
        index = tags.index(current)
        return tags[index + 1] if index + 1 < len(tags) else None
    return tags[0] if tags else None

def changelog_section(version_name):
    text = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")
    pattern = rf"^## \[{re.escape(version_name)}\].*$(.*?)(?=^## \[|\Z)"
    match = re.search(pattern, text, re.M | re.S)
    if not match:
        raise SystemExit(f"No changelog entry found for v{version_name}.")
    return match.group(0).strip()

def release_notes(version_name):
    section = changelog_section(version_name)
    previous = previous_release_tag(version_name)
    current = f"v{version_name}"
    if previous:
        full = f"**Full Changelog:** [{previous}...{current}](https://github.com/{os.environ['GITHUB_REPOSITORY']}/compare/{previous}...{current})"
    else:
        full = f"**Full Changelog:** [commits/{current}](https://github.com/{os.environ['GITHUB_REPOSITORY']}/commits/{current})"
    return section + "\n\n" + full + "\n"

def amend_changelog():
    result = run("git", "status", "--short", "--", "CHANGELOG.md")
    if not result.strip():
        print("Changelog is already committed; no amend needed.")
        return
    run("git", "add", "CHANGELOG.md")
    run(
        "git", "-c", "user.name=github-actions[bot]", "-c", "user.email=41898282+github-actions[bot]@users.noreply.github.com",
        "commit", "--amend", "--no-edit",
    )
    run("git", "push", "--force-with-lease", "origin", "HEAD:main")
    print("Amended the version commit with the generated CHANGELOG.md.")

def main():
    p = argparse.ArgumentParser(description="Prepare and publish a Wallora release.")
    p.add_argument("--prepare", action="store_true")
    p.add_argument("--publish", action="store_true")
    args = p.parse_args()
    name, _ = version()
    tag = f"v{name}"
    if not version_changed():
        print("Version metadata did not change; nothing to release.")
        return
    if args.prepare:
        changelog = ROOT / "CHANGELOG.md"
        marker = f"## [{name}]"
        if changelog.exists() and marker in changelog.read_text(encoding="utf-8"):
            print(f"Changelog entry for v{name} already exists; keeping it.")
        else:
            subprocess.run(["python", "scripts/generate_changelog.py", "--version", name], cwd=ROOT, check=True)
        amend_changelog()
    if args.publish:
        if not (ROOT / "CHANGELOG.md").exists():
            raise SystemExit("CHANGELOG.md is missing. Run scripts/generate_changelog.py first.")
        notes_file = ROOT / "RELEASE_NOTES.md"
        notes_file.write_text(release_notes(name), encoding="utf-8")
        repo = os.environ["GITHUB_REPOSITORY"]
        if release_exists(tag):
            print(f"Release {tag} already exists; updating its notes.")
            subprocess.run(["gh", "release", "edit", tag, "--repo", repo, "--title", f"Wallora v{name}", "--notes-file", str(notes_file)], cwd=ROOT, check=True)
            return
        apk_dir = ROOT / "app/build/outputs/apk/debug"
        apk = next(apk_dir.glob("*.apk"), None) if apk_dir.exists() else None
        if not apk:
            raise SystemExit("No APK was produced. Build the debug APK before publishing.")
        release_apk = ROOT / f"Wallora_v{name}.apk"
        release_apk.write_bytes(apk.read_bytes())
        subprocess.run(["gh", "release", "create", tag, str(release_apk), "--repo", repo, "--title", f"Wallora v{name}", "--notes-file", str(notes_file)], cwd=ROOT, check=True)

if __name__ == "__main__":
    main()
