#!/usr/bin/env bash
# ============================================================
#  build.sh - compila o plugin SeasonRPG Demo em Linux (VPS).
#  Equivalente ao build.ps1. Requer JDK 21 e curl.
#  Baixa o Paper 1.21.4, extrai a API (patchonly), compila e
#  empacota build/SeasonRPGDemo-0.1.0.jar.
# ============================================================
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
PAPER_VERSION="1.21.4"
PAPER_JAR="$ROOT/server/paper-$PAPER_VERSION.jar"

# --- localizar java 21 ----------------------------------------------------
if ! command -v javac >/dev/null 2>&1; then
  echo "ERRO: JDK 21 nao encontrado. Instale, por ex.: sudo apt install openjdk-21-jdk"
  exit 1
fi
echo "Usando: $(javac -version 2>&1)"

# --- baixar o Paper -------------------------------------------------------
if [ ! -f "$PAPER_JAR" ]; then
  mkdir -p "$ROOT/server"
  echo "Consultando ultimo build do Paper $PAPER_VERSION (API v3)..."
  UA="SeasonRPGDemo/1.0"
  URL=$(curl -s -A "$UA" "https://fill.papermc.io/v3/projects/paper/versions/$PAPER_VERSION/builds/latest" \
        | grep -o '"url"[^,]*server[^"]*paper[^"]*\.jar' | head -1 | sed 's/.*"url":\s*"//;s/".*//')
  # fallback direto para o build estavel conhecido, se o parse falhar
  if [ -z "${URL:-}" ]; then
    URL="https://fill-data.papermc.io/v1/objects/5ee4f542f628a14c644410b08c94ea42e772ef4d29fe92973636b6813d4eaffc/paper-1.21.4-232.jar"
  fi
  echo "Baixando Paper..."
  curl -L --retry 3 -A "$UA" -o "$PAPER_JAR" "$URL"
fi

# --- extrair a API (paperclip patchonly) ----------------------------------
API_DIR="$ROOT/server/libraries"
if [ ! -d "$API_DIR" ]; then
  echo "Extraindo a API do Paper (patchonly)..."
  ( cd "$ROOT/server" && java -Dpaperclip.patchonly=true -jar "$PAPER_JAR" >/dev/null 2>&1 || true )
fi
CLASSPATH=$(find "$ROOT/server/libraries" -name '*.jar' | tr '\n' ':')

# --- compilar -------------------------------------------------------------
OUT="$ROOT/build/classes"
rm -rf "$OUT"; mkdir -p "$OUT"
mapfile -t SOURCES < <(find "$ROOT/src/main/java" -name '*.java')
echo "Compilando ${#SOURCES[@]} arquivos..."
javac -encoding UTF-8 -proc:none -cp "$CLASSPATH" -d "$OUT" "${SOURCES[@]}"

# --- recursos + empacotar -------------------------------------------------
cp -r "$ROOT/src/main/resources/." "$OUT/"
JAR="$ROOT/build/SeasonRPGDemo-0.1.0.jar"
rm -f "$JAR"
jar -c -f "$JAR" -C "$OUT" .
echo ""
echo "OK -> $JAR"
echo "Copie para server/plugins/ e inicie o servidor."
