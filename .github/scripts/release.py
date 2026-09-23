#!/usr/bin/env python3
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
VERSION_FILE = ROOT / "version.properties"


def run(*args, capture=True):
    result = subprocess.run(args, cwd=ROOT, check=False, text=True, capture_output=capture)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or result.stdout.strip())
    return result.stdout.strip()


def version():
    text = VERSION_FILE.read_text(encoding="utf-8")
    name = re.search(r"^versionName\s*=\s*(.+)$", text, re.M)
    code = re.search(r"^versionCode\s*=\s*(\d+)$", text, re.M)
    if not name or not code:
        raise SystemExit("Invalid version.properties.")
    return name.group(1).strip(), int(code.group(1))


def latest_tag():
    tags = run("git", "tag", "--list", "v*", "--sort=-version:refname").splitlines()
    return tags[0] if tags else ""


def validate():
    name, code = version()
    tag = latest_tag()
    output = Path(os.environ["GITHUB_OUTPUT"]) if os.environ.get("GITHUB_OUTPUT") else None

    def result(should_release):
        if output:
            output.write_text(
                f"release={'true' if should_release else 'false'}\n",
                encoding="utf-8",
            )
        return should_release

    if tag == f"v{name}":
        print(f"Version {name} is already released; skipping release.")
        return result(False)

    if not tag:
        print(f"Version v{name} is ready for the first release.")
        return result(True)

    previous_code = None
    try:
        previous_code = int(
            run("git", "show", f"{tag}:version.properties")
            .split("versionCode=", 1)[1]
            .splitlines()[0]
        )
    except (IndexError, ValueError, RuntimeError):
        pass

    diff = run("git", "diff", "--unified=0", tag, "HEAD", "--", "version.properties")
    if not re.search(r"^[+-](versionName|versionCode)\s*=", diff, re.M):
        print("version.properties was not changed since the latest release; skipping release.")
        return result(False)

    if previous_code is not None and code <= previous_code:
        print(f"versionCode {code} is not greater than {previous_code}; skipping release.")
        return result(False)

    print(f"Version {name} (code {code}) is ready for release.")
    return result(True)


def notes():
    tag = latest_tag()
    commit_range = f"{tag}..HEAD" if tag else "HEAD"
    subjects = run("git", "log", commit_range, "--no-merges", "--format=%s").splitlines()

    skip = (
        "ci:", "ci(", "docs:", "docs(", "chore:", "chore(",
        "test:", "test(", "style:", "style(", "release:", "release(",
    )
    points = []

    for subject in subjects:
        lower = subject.lower()
        if lower.startswith(skip):
            continue

        subject = re.sub(r"^[a-zA-Z]+(?:\([^)]*\))?!:\s*", "", subject).strip()
        subject = re.sub(r"\s+\(#[0-9]+\)$", "", subject).strip()
        if subject and subject not in points:
            points.append(subject)

    (ROOT / "RELEASE_NOTES.md").write_text(
        "".join(f"- {point}\n" for point in points),
        encoding="utf-8",
    )

    if not points:
        raise SystemExit("No user-facing commits found for release notes.")

    print(f"Generated {len(points)} release notes.")


def prepare():
    name, _ = version()
    output_dir = ROOT / "app/build/outputs/apk/release"
    apks = list(output_dir.glob("*.apk"))
    if len(apks) != 1:
        raise SystemExit(f"Expected exactly one release APK, found {len(apks)}.")

    apk = apks[0]
    subprocess.run(["apksigner", "verify", "--verbose", str(apk)], check=True)
    target = ROOT / f"Wallora_v{name}.apk"
    shutil.copy2(apk, target)
    print(target.name)


def publish():
    name, _ = version()
    tag = f"v{name}"
    apk = ROOT / f"Wallora_v{name}.apk"
    notes_file = ROOT / "RELEASE_NOTES.md"

    existing = subprocess.run(
        ["gh", "release", "view", tag],
        cwd=ROOT,
        text=True,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )
    if existing.returncode == 0:
        print(f"GitHub release {tag} already exists; skipping release.")
        return

    run(
        "gh", "release", "create", tag, str(apk),
        "--repo", "Darkstar085/Wallora",
        "--title", f"Wallora {tag}",
        "--notes-file", str(notes_file),
    )


def main():
    if len(sys.argv) != 2:
        raise SystemExit("Usage: release.py validate|notes|prepare|publish")
    command = sys.argv[1]
    if command == "validate":
        if not validate():
            sys.exit(0)
        return
    {"notes": notes, "prepare": prepare, "publish": publish}[command]()


if __name__ == "__main__":
    main()
