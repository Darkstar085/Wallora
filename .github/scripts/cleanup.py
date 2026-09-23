#!/usr/bin/env python3
import json
import os
import subprocess

REPO = os.environ["GITHUB_REPOSITORY"]

CACHE_TYPES = (
    "wallora-debug-keystore-",
    "setup-java-Linux-x64-gradle-wrapper-",
    "setup-java-Linux-x64-gradle-",
)


def gh(path):
    return json.loads(
        subprocess.check_output(
            ["gh", "api", "--paginate", "--slurp", path],
            text=True,
        )
    )


def flatten(payload, key):
    return [item for page in payload for item in page.get(key, [])]


def delete(path):
    subprocess.run(["gh", "api", "--method", "DELETE", path], check=True)


def cache_type(key):
    for prefix in CACHE_TYPES:
        if key.startswith(prefix):
            return prefix
    return key


def main():
    runs = flatten(
        gh(f"/repos/{REPO}/actions/runs?per_page=100"),
        "workflow_runs",
    )
    latest = {}
    for run in runs:
        workflow_id = run["workflow_id"]
        if workflow_id not in latest or run["created_at"] > latest[workflow_id]["created_at"]:
            latest[workflow_id] = run

    keep_runs = {run["id"] for run in latest.values()}
    for run in runs:
        if run["id"] not in keep_runs:
            delete(f"/repos/{REPO}/actions/runs/{run['id']}")

    artifacts = flatten(
        gh(f"/repos/{REPO}/actions/artifacts?per_page=100"),
        "artifacts",
    )
    for artifact in artifacts:
        run_id = (artifact.get("workflow_run") or {}).get("id")
        if run_id not in keep_runs:
            delete(f"/repos/{REPO}/actions/artifacts/{artifact['id']}")

    caches = flatten(
        gh(f"/repos/{REPO}/actions/caches?per_page=100"),
        "actions_caches",
    )
    latest_cache = {}
    for cache in caches:
        kind = cache_type(cache["key"])
        if kind not in latest_cache or cache["created_at"] > latest_cache[kind]["created_at"]:
            latest_cache[kind] = cache

    for cache in caches:
        kind = cache_type(cache["key"])
        if cache["id"] != latest_cache[kind]["id"]:
            delete(f"/repos/{REPO}/actions/caches/{cache['id']}")

    print("Cleanup complete.")


if __name__ == "__main__":
    main()
