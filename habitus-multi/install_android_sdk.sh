#!/bin/bash
set -e

SDK_DIR="/home/leyzar/android-sdk"
ZIP_FILE="/home/leyzar/school/habitus/habitus-multi/commandlinetools.zip"

echo "=== Creando directorio para Android SDK ==="
mkdir -p "$SDK_DIR/cmdline-tools"

echo "=== Descargando Android Command Line Tools ==="
wget -q --show-progress "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" -O "$ZIP_FILE"

echo "=== Descomprimiendo herramientas ==="
unzip -q "$ZIP_FILE" -d "$SDK_DIR/cmdline-tools"

# Estructura requerida por sdkmanager: cmdline-tools/latest/bin/...
echo "=== Configurando estructura de directorios ==="
rm -rf "$SDK_DIR/cmdline-tools/latest"
mv "$SDK_DIR/cmdline-tools/cmdline-tools" "$SDK_DIR/cmdline-tools/latest"

echo "=== Limpiando archivo zip ==="
rm -f "$ZIP_FILE"

echo "=== Aceptando Licencias de Android SDK ==="
# Acepta automáticamente todas las licencias del SDK de Android
yes | "$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" --licenses

echo "=== Instalando Platform Tools ==="
"$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" "platform-tools"

echo "=== Android SDK instalado exitosamente en $SDK_DIR ==="
