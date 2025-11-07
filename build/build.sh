#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CLIENT_DIR="$ROOT_DIR/client"
SERVER_DIR="$ROOT_DIR/server"
STATIC_DIR="$SERVER_DIR/src/main/resources/static"
OUTPUT_DIR="$ROOT_DIR/build/output"

mkdir -p "$OUTPUT_DIR"

pushd "$CLIENT_DIR" >/dev/null
if [ ! -f package-lock.json ]; then
  npm install
else
  npm ci
fi
npm run lint
npm run build
popd >/dev/null

find "$STATIC_DIR" -type f ! -name '.gitkeep' -delete
if compgen -G "$CLIENT_DIR/dist/*" > /dev/null; then
  cp -r "$CLIENT_DIR/dist"/* "$STATIC_DIR"
fi

pushd "$SERVER_DIR" >/dev/null
mvn -B clean verify
popd >/dev/null

FINAL_JAR="$OUTPUT_DIR/pompot.jar"
cp "$SERVER_DIR/target/pompot-0.1.0.jar" "$FINAL_JAR"

echo "Build complete. Run the UI with: java -jar $FINAL_JAR"
echo "Run the CLI with: java -jar $FINAL_JAR --mode=cli"
