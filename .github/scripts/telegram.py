#!/usr/bin/env python3
import hashlib
import html
import json
import os
import subprocess
import sys
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def send_document(token, chat_id, path, caption=""):
    digest = hashlib.sha256(path.read_bytes()).hexdigest()
    boundary = os.urandom(16).hex()

    body = (
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="chat_id"\r\n\r\n'
        f"{chat_id}\r\n"
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="document"; filename="{path.name}"\r\n'
        "Content-Type: application/vnd.android.package-archive\r\n\r\n"
    ).encode() + path.read_bytes()

    if caption:
        body += (
            f"\r\n--{boundary}\r\n"
            'Content-Disposition: form-data; name="caption"\r\n\r\n'
            f"{caption}\r\n"
            f"--{boundary}\r\n"
            'Content-Disposition: form-data; name="parse_mode"\r\n\r\n'
            "HTML\r\n"
        ).encode()

    body += f"\r\n--{boundary}--\r\n".encode()

    request = urllib.request.Request(
        f"https://api.telegram.org/bot{token}/sendDocument",
        data=body,
        headers={"Content-Type": f"multipart/form-data; boundary={boundary}"},
    )

    with urllib.request.urlopen(request, timeout=900) as response:
        result = json.loads(response.read())

    if not result.get("ok"):
        raise SystemExit(json.dumps(result))

    print(f"Sent {path.name} ({path.stat().st_size} bytes, SHA-256 {digest})")


def release_caption(path):
    current = path.stem.removeprefix("Wallora_v")
    tags = subprocess.run(
        ["git", "tag", "--list", "v*", "--sort=-version:refname"],
        cwd=ROOT,
        check=True,
        text=True,
        capture_output=True,
    ).stdout.splitlines()
    previous = next(
        (tag.removeprefix("v") for tag in tags if tag != f"v{current}"),
        "",
    )

    version_line = (
        f"🚀 Version: {previous} → {current}"
        if previous
        else f"🚀 Version: {current}"
    )
    release_url = (
        f"https://github.com/{os.environ['GITHUB_REPOSITORY']}/releases/tag/v{current}"
    )

    return (
        "🖼️ <b>Wallora</b>\n"
        "An open-source Android wallpaper app focused on simplicity, beauty, and a clean experience\n"
        f"{version_line}\n\n"
        f'Changelog: <a href="{html.escape(release_url, quote=True)}">Open</a>'
    )


def main():
    if len(sys.argv) not in (2, 3):
        raise SystemExit("Usage: telegram.py <file> [release]")

    path = Path(sys.argv[1])
    if not path.is_file() or path.stat().st_size == 0:
        raise SystemExit("APK is missing or empty.")

    mode = sys.argv[2] if len(sys.argv) == 3 else "debug"
    token = os.environ.get("TELEGRAM_BOT_TOKEN")
    chat_id = os.environ.get(
        "RELEASE_CHAT_ID" if mode == "release" else "TELEGRAM_CHAT_ID"
    )
    if not token or not chat_id:
        raise SystemExit("Telegram credentials are missing.")

    caption = release_caption(path) if mode == "release" else ""
    send_document(token, chat_id, path, caption)


if __name__ == "__main__":
    main()
