#!/usr/bin/env python3
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GRADLE_FILE = ROOT / "app/build.gradle.kts"

def run(*args):
    result = subprocess.run(args, cwd=ROOT, text=True, capture_output=True)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or result.stdout.strip())
    return result.stdout.strip()

def version_from(text):
    name = re.search(r'^\s*versionName\s*=\s*"([^"]+)"', text, re.M)
    code = re.search(r"^\s*versionCode\s*=\s*(\d+)", text, re.M)
    if not name or not code:
        raise SystemExit("Could not determine app version from app/build.gradle.kts.")
    return name.group(1).strip(), int(code.group(1))

def version():
    return version_from(GRADLE_FILE.read_text(encoding="utf-8"))

def latest_tag():
    tags = run("git", "tag", "--list", "v*", "--sort=-version:refname").splitlines()
    return tags[0] if tags else ""

def output(value):
    path = os.environ.get("GITHUB_OUTPUT")
    if path:
        Path(path).write_text(value, encoding="utf-8")

def validate():
    name, code = version()
    tag = latest_tag()
    if tag == f"v{name}":
        print(f"Version {name} is already released; skipping release.")
        output("release=false\n")
        return False
    if not tag:
        output("release=true\n")
        return True
    previous_code = None
    try:
        previous = run("git", "show", f"{tag}:app/build.gradle.kts")
        previous_code = version_from(previous)[1]
    except (RuntimeError, SystemExit):
        pass
    diff = run("git", "diff", "--unified=0", tag, "HEAD", "--", "app/build.gradle.kts")
    if not re.search(r"^[+-]\s*(versionName|versionCode)\s*=", diff, re.M):
        print("App version was not changed since the latest release; skipping release.")
        output("release=false\n")
        return False
    if previous_code is not None and code <= previous_code:
        print(f"versionCode {code} is not greater than {previous_code}; skipping release.")
        output("release=false\n")
        return False
    print(f"Version {name} (code {code}) is ready for release.")
    output("release=true\n")
    return True

def notes():
    tag = latest_tag()
    commit_range = f"{tag}..HEAD" if tag else "HEAD"
    subjects = run("git", "log", commit_range, "--no-merges", "--format=%s").splitlines()
    skip = ("ci:", "ci(", "docs:", "docs(", "chore:", "chore(", "test:", "test(", "style:", "style(", "release:", "release(")
    points = []
    for subject in subjects:
        if subject.lower().startswith(skip):
            continue
        subject = re.sub(r"^[a-zA-Z]+(?:\([^)]*\))?!:\s*", "", subject).strip()
        subject = re.sub(r"\s+\(#[0-9]+\)$", "", subject).strip()
        if subject and subject not in points:
            points.append(subject)
    if not points:
        raise SystemExit("No user-facing commits found for release notes.")
    (ROOT / "RELEASE_NOTES.md").write_text("".join(f"- {point}\n" for point in points), encoding="utf-8")
    print(f"Generated {len(points)} release notes.")

def prepare():
    name, _ = version()
    apks = list((ROOT / "app/build/outputs/apk/release").glob("*.apk"))
    if len(apks) != 1:
        raise SystemExit(f"Expected exactly one release APK, found {len(apks)}.")
    apk = apks[0]
    subprocess.run(["apksigner", "verify", "--verbose", str(apk)], check=True)
    target = ROOT / f"Wallora_v{name}.apk"
    shutil.copy2(apk, target)
    output_path = os.environ.get("GITHUB_OUTPUT")
    if output_path:
        Path(output_path).write_text(f"apk={target.name}\n", encoding="utf-8")
    print(target.name)

def publish():
    name, _ = version()
    tag = f"v{name}"
    apk = ROOT / f"Wallora_v{name}.apk"
    notes_file = ROOT / "RELEASE_NOTES.md"
    existing = subprocess.run(["gh", "release", "view", tag], cwd=ROOT, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    if existing.returncode == 0:
        print(f"GitHub release {tag} already exists; skipping release.")
        return
    run("gh", "release", "create", tag, str(apk), "--repo", "Darkstar085/Wallora", "--title", f"Wallora {tag}", "--notes-file", str(notes_file))

def main():
    if len(sys.argv) != 2:
        raise SystemExit("Usage: release.py validate|notes|prepare|publish")
    command = sys.argv[1]
    if command == "validate":
        validate()
        return
    {"notes": notes, "prepare": prepare, "publish": publish}[command]()

if __name__ == "__main__":
    main()
