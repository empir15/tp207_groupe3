@echo off
setlocal enabledelayedexpansion

REM ============================================================
REM  PlanifyEdu - Script de compilation et lancement (Windows)
REM  Ne necessite pas Maven. Telecharge JavaFX et SQLite JDBC.
REM ============================================================

REM --- Repertoires ---
set "PROJECT_DIR=%~dp0"
set "SRC_DIR=%PROJECT_DIR%src"
set "OUT_DIR=%PROJECT_DIR%out"
set "LIB_DIR=%PROJECT_DIR%lib"
set "CLASSES_DIR=%OUT_DIR%\classes"

REM --- Creer les dossiers necessaires ---
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"
if not exist "%CLASSES_DIR%" mkdir "%CLASSES_DIR%"
if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"
if not exist "%PROJECT_DIR%database" mkdir "%PROJECT_DIR%database"

echo.
echo  ==========================================
echo  PlanifyEdu - Build Script
echo  ==========================================
echo.

REM --- Telecharger JavaFX SDK si absent ---
set "JAVAFX_DIR=C:\javafx\javafx-sdk-21.0.10"
if not exist "%JAVAFX_DIR%" (
    echo [1/3] Telechargement JavaFX 21 SDK (180 Mo, patience...)
    set "JAVAFX_ZIP=%LIB_DIR%\javafx.zip"
    powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_windows-x64_bin-sdk.zip' -OutFile '!JAVAFX_ZIP!'"
    echo [1/3] Extraction JavaFX...
    powershell -NoProfile -Command "Expand-Archive -Path '!JAVAFX_ZIP!' -DestinationPath '!LIB_DIR!' -Force"
    for /d %%i in ("%LIB_DIR%\javafx-sdk-*") do rename "%%i" "javafx-sdk"
    del "!JAVAFX_ZIP!" 2>nul
    echo [1/3] JavaFX installe avec succes.
) else (
    echo [1/3] JavaFX SDK deja present - OK
)

REM --- Telecharger SQLite JDBC si absent ---
set "SQLITE_JAR=%LIB_DIR%\sqlite-jdbc.jar"
set "SLF4J_API=%LIB_DIR%\slf4j-api-2.0.9.jar"
set "SLF4J_SIMPLE=%LIB_DIR%\slf4j-simple-2.0.9.jar"
if not exist "%SQLITE_JAR%" (
    echo [2/3] Telechargement SQLite JDBC...
    powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://github.com/xerial/sqlite-jdbc/releases/download/3.44.1.0/sqlite-jdbc-3.44.1.0.jar' -OutFile '%SQLITE_JAR%'"
    echo [2/3] SQLite JDBC installe avec succes.
) else (
    echo [2/3] SQLite JDBC deja present - OK
)

REM --- Chemins JavaFX ---
set "JAVAFX_MODS=%JAVAFX_DIR%\lib"
set "JAVAFX_MODULES=javafx.controls,javafx.fxml"

REM --- Compiler ---
echo [3/3] Compilation du projet...

REM Collecter tous les fichiers .java
dir /s /b "%SRC_DIR%\*.java" > "%OUT_DIR%\sources.txt"

javac -encoding UTF-8 --module-path "%JAVAFX_MODS%" --add-modules %JAVAFX_MODULES% -cp "%SQLITE_JAR%;%SLF4J_API%;%SLF4J_SIMPLE%" -d "%CLASSES_DIR%" @"%OUT_DIR%\sources.txt"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo  ERREUR : La compilation a echoue !
    echo  Verifiez les messages d-erreur ci-dessus.
    pause
    exit /b 1
)

echo.
echo  ==========================================
echo  Compilation reussie !
echo  ==========================================
echo.

REM --- Copier ressources (FXML + CSS) ---
if not exist "%CLASSES_DIR%\ui" mkdir "%CLASSES_DIR%\ui"
if not exist "%CLASSES_DIR%\styles" mkdir "%CLASSES_DIR%\styles"
xcopy /s /y "%SRC_DIR%\ui" "%CLASSES_DIR%\ui\" >nul 2>&1
xcopy /s /y "%SRC_DIR%\styles" "%CLASSES_DIR%\styles\" >nul 2>&1

REM --- Lancer l'application ---
echo  Lancement de PlanifyEdu...
echo.

java --module-path "%JAVAFX_MODS%" --add-modules %JAVAFX_MODULES% -cp "%CLASSES_DIR%;%SQLITE_JAR%;%SLF4J_API%;%SLF4J_SIMPLE%" MainApp

pause
