@echo off
cd /d "%~dp0"
if not exist build\classes mkdir build\classes
if not exist dist mkdir dist
javac -source 8 -target 8 -encoding UTF-8 -d build\classes src\dms\*.java
if errorlevel 1 exit /b 1
jar cfm dist\PokemonBuildDMS.jar META-INF\MANIFEST.MF -C build\classes .
if errorlevel 1 exit /b 1
echo Build complete: dist\PokemonBuildDMS.jar
