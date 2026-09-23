#!/usr/bin/env python3
import argparse
import os
import uuid
from pathlib import Path
from urllib.parse import urlencode
from urllib.request import Request, urlopen


def post(token, method, fields, file_path=None, file_field="document", timeout=900):
    url = f"https://api.telegram.org/bot{token}/{method}"

    if not file_path:
        data = urlencode(fields).encode()
        request = Request(
            url,
            data=data,
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )
    else:
        boundary = f"----Wallora{uuid.uuid4().hex}"
        chunks = []

        for key, value in fields.items():
            chunks.append(
                (
                    f"--{boundary}\r\n"
                    f'Content-Disposition: form-data; name="{key}"\r\n'
                    f"\r\n{value}\r\n"
                ).encode()
            )

        path = Path(file_path)
        chunks.extend(
            [
                (
                    f"--{boundary}\r\n"
                    f'Content-Disposition: form-data; name="{file_field}"; '
                    f'filename="{path.name}"\r\n'
                    "Content-Type: application/octet-stream\r\n"
                    "\r\n"
                ).encode(),
                path.read_bytes(),
                b"\r\n",
                f"--{boundary}--\r\n".encode(),
            ]
        )

        request = Request(
            url,
            data=b"".join(chunks),
            headers={"Content-Type": f"multipart/form-data; boundary={boundary}"},
        )

    with urlopen(request, timeout=timeout) as response:
        body = response.read().decode()
        if '"ok":true' not in body:
            raise RuntimeError(body)


def main():
    parser = argparse.ArgumentParser(
        description="Send Wallora build notifications to Telegram."
    )
    parser.add_argument("action", choices=["message", "document"])
    parser.add_argument("--text")
    parser.add_argument("--file")
    parser.add_argument("--caption")
    parser.add_argument("--parse-mode")
    args = parser.parse_args()

    token = os.environ.get("TELEGRAM_BOT_TOKEN")
    chat_id = os.environ.get("TELEGRAM_CHAT_ID")
    if not token or not chat_id:
        print("Telegram credentials are not configured; skipping.")
        return

    if args.action == "message":
        fields = {"chat_id": chat_id, "text": args.text or ""}
        if args.parse_mode:
            fields["parse_mode"] = args.parse_mode
        post(token, "sendMessage", fields, timeout=60)
        return

    if not args.file:
        raise SystemExit("--file is required for document uploads.")

    fields = {"chat_id": chat_id, "caption": args.caption or ""}
    if args.parse_mode:
        fields["parse_mode"] = args.parse_mode
    post(token, "sendDocument", fields, args.file, timeout=900)


if __name__ == "__main__":
    main()
