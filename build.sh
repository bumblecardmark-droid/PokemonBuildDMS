#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes dist
javac -source 8 -target 8 -encoding UTF-8 -d build/classes src/dms/*.java
jar cfm dist/PokemonBuildDMS.jar META-INF/MANIFEST.MF -C build/classes .
printf '%s\n' 'Build complete: dist/PokemonBuildDMS.jar'
