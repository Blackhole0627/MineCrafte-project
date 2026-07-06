#!/usr/bin/env bash
# Inicia o servidor Paper com o plugin da demo (uso em VPS Linux).
# Rode ./build.sh primeiro e copie o jar para server/plugins/.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT/server"
mkdir -p plugins
cp -f "$ROOT/build/SeasonRPGDemo-0.1.0.jar" plugins/ 2>/dev/null || true
echo "eula=true" > eula.txt
exec java -Xmx2G -jar paper-1.21.4.jar nogui
