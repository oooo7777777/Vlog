#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION_FILE="$ROOT_DIR/gradle.properties"
README_FILE="$ROOT_DIR/README.md"
OWNER="oooo7777777"
REPO="Vlog"

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

JITPACK_URL="https://jitpack.io/com/github/${OWNER}/${REPO}/${BRANCH}-SNAPSHOT/${REPO}-${BRANCH}-SNAPSHOT.pom"
echo "Warming JitPack snapshot at ${JITPACK_URL}"
curl --fail --location --retry 5 --retry-all-errors --silent --show-error "$JITPACK_URL" >/tmp/jitpack-snapshot-pom.xml || {
  echo "Snapshot warm failed. You can inspect:"
  echo "https://jitpack.io/com/github/${OWNER}/${REPO}/${BRANCH}-SNAPSHOT/build.log"
  exit 1
}

echo "Snapshot ${BRANCH}-SNAPSHOT pushed and warmed."
