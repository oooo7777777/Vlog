#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION_FILE="$ROOT_DIR/gradle.properties"
README_FILE="$ROOT_DIR/README.md"
OWNER="oooo7777777"
REPO="Vlog"
MAX_ATTEMPTS=24
SLEEP_SECONDS=5

if [[ $# -gt 1 ]]; then
  echo "Usage: ./scripts/snapshot.sh [version]"
  exit 1
fi

if [[ $# -eq 1 ]]; then
  VERSION="$1"
else
  VERSION="$(grep '^VLOG_VERSION=' "$VERSION_FILE" | cut -d'=' -f2- | tr -d '[:space:]')"
fi

if [[ -z "${VERSION:-}" ]]; then
  echo "Unable to determine version."
  echo "Pass it explicitly, or set VLOG_VERSION in gradle.properties."
  exit 1
fi

if [[ ! "$VERSION" =~ ^[0-9]+(\.[0-9]+){1,2}([-.][A-Za-z0-9]+)?-SNAPSHOT$ ]]; then
  echo "Invalid snapshot version: $VERSION"
  echo "Expected examples: 2.0.7-SNAPSHOT, 2.1.0-beta1-SNAPSHOT"
  exit 1
fi

cd "$ROOT_DIR"

wait_for_jitpack() {
  local version="$1"
  local pom_url="https://jitpack.io/com/github/${OWNER}/${REPO}/${version}/${REPO}-${version}.pom"
  local log_url="https://jitpack.io/com/github/${OWNER}/${REPO}/${version}/build.log"
  local tmp_pom tmp_log
  tmp_pom="$(mktemp)"
  tmp_log="$(mktemp)"

  echo "Waiting for JitPack build: ${version}"
  for ((attempt=1; attempt<=MAX_ATTEMPTS; attempt++)); do
    if curl --fail --location --silent --show-error "$pom_url" >"$tmp_pom" 2>/dev/null; then
      echo "JitPack build succeeded for ${version}"
      rm -f "$tmp_pom" "$tmp_log"
      return 0
    fi

    if curl --location --silent --show-error "$log_url" >"$tmp_log" 2>/dev/null; then
      if grep -q "BUILD SUCCESSFUL" "$tmp_log"; then
        echo "JitPack build succeeded for ${version}"
        rm -f "$tmp_pom" "$tmp_log"
        return 0
      fi
      if grep -Eq "BUILD FAILED|FAILURE: Build failed|Task .* FAILED" "$tmp_log"; then
        echo "JitPack build failed for ${version}. Log:"
        cat "$tmp_log"
        rm -f "$tmp_pom" "$tmp_log"
        return 1
      fi
    fi

    echo "JitPack still building (${attempt}/${MAX_ATTEMPTS})..."
    sleep "$SLEEP_SECONDS"
  done

  echo "Timed out waiting for JitPack build. Check:"
  echo "$log_url"
  rm -f "$tmp_pom" "$tmp_log"
  return 1
}

BRANCH="$(git rev-parse --abbrev-ref HEAD)"
if [[ "$BRANCH" == "HEAD" ]]; then
  echo "Detached HEAD is not supported for snapshot publishing."
  exit 1
fi

DIRTY_FILES=()
while IFS= read -r file; do
  [[ -n "$file" ]] && DIRTY_FILES+=("$file")
done < <(git status --porcelain | sed -E 's/^.{3}//' | sed '/^$/d')
if [[ ${#DIRTY_FILES[@]} -gt 0 ]]; then
  for file in "${DIRTY_FILES[@]}"; do
    case "$file" in
      gradle.properties|README.md)
        ;;
      *)
        echo "Working tree contains non-snapshot changes: $file"
        echo "Commit or stash unrelated changes before publishing a snapshot."
        exit 1
        ;;
    esac
  done
fi

sed -i.bak "s/^VLOG_VERSION=.*/VLOG_VERSION=$VERSION/" "$VERSION_FILE"
sed -i.bak "s/com.github.oooo7777777:Vlog:[^']*/com.github.oooo7777777:Vlog:${BRANCH}-SNAPSHOT/" "$README_FILE"
rm -f "$VERSION_FILE.bak" "$README_FILE.bak"

git add "$VERSION_FILE" "$README_FILE" "$ROOT_DIR/library/build.gradle"
if git diff --cached --quiet; then
  echo "No snapshot metadata changes to commit."
else
  git commit -m "snapshot: $VERSION"
fi

git push origin "$BRANCH"

wait_for_jitpack "${BRANCH}-SNAPSHOT"
