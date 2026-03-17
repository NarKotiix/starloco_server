#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

JAR_PATH="build/libs/Server-1.0.0.jar"
JAVA_OPTS=(-Dfile.encoding=UTF-8 -Xms512M -Xmx2G)

if ! command -v java >/dev/null 2>&1; then
  echo "[ERREUR] Java est introuvable dans le PATH."
  exit 1
fi

if [[ ! -f "$JAR_PATH" ]]; then
  echo "[ERREUR] JAR absent: $JAR_PATH"
  echo "Exemple: ./gradlew build"
  sleep 5
  exit 1
fi

echo "========================================"
echo "StarLoco Server Launcher"
echo "========================================"
echo "JAR: $JAR_PATH"
echo "UTF-8 | Couleurs ANSI | CTRL+C OK"
echo "========================================"

java "${JAVA_OPTS[@]}" -jar "$JAR_PATH"

echo
echo "[OK] Shutdown complete - Logs dans Logs/"
sleep 2

