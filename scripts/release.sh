#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION_FILE="$ROOT_DIR/gradle.properties"
README_FILE="$ROOT_DIR/README.md"

if [[ $# -gt 1 ]]; then
  echo "Usage: ./scripts/release.sh [version]"
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

if [[ ! "$VERSION" =~ ^[0-9]+(\.[0-9]+){1,2}([-.][A-Za-z0-9]+)?$ ]]; then
  echo "Invalid version: $VERSION"
  echo "Expected examples: 2.0.7, 2.1.0-beta1"
  exit 1
fi

cd "$ROOT_DIR"

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
        echo "Working tree contains non-release changes: $file"
        echo "Commit or stash unrelated changes before releasing."
        exit 1
        ;;
    esac
  done
fi

if git rev-parse -q --verify "refs/tags/$VERSION" >/dev/null; then
  echo "Tag $VERSION already exists."
  exit 1
fi

sed -i.bak "s/^VLOG_VERSION=.*/VLOG_VERSION=$VERSION/" "$VERSION_FILE"
sed -i.bak "s/com.github.oooo7777777:Vlog:[^']*/com.github.oooo7777777:Vlog:$VERSION/" "$README_FILE"
rm -f "$VERSION_FILE.bak" "$README_FILE.bak"

git add "$VERSION_FILE" "$README_FILE" "$ROOT_DIR/library/build.gradle"
git commit -m "release: $VERSION"
git tag "$VERSION"
git push origin HEAD
git push origin "$VERSION"

echo "Release $VERSION pushed."
echo "GitHub Actions will warm JitPack for tag $VERSION."
