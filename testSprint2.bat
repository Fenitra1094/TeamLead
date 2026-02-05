@echo off
title Test Framework Controller
echo ============================================
echo     TEST DU CONTROLLER AVEC HandleUrl
echo ============================================

REM === Définition des chemins ===
set SRC=src
set BIN=bin
set LIB=WEB-INF\lib
set FRAMEWORK_JAR=%LIB%\framework.jar

REM === Nettoyage de l'ancien dossier bin ===
if exist %BIN% (
    echo Suppression de l'ancien dossier bin...
    rmdir /s /q %BIN%
)
mkdir %BIN%

REM === Compilation des fichiers Java ===
echo Compilation des classes...
javac -d %BIN% -cp %FRAMEWORK_JAR% %SRC%\com\app\Main.java %SRC%\com\app\controllers\UserController.java

if %errorlevel% neq 0 (
    echo.
    echo ❌ Erreur de compilation !
    pause
    exit /b
)

REM === Exécution du test ===
echo.
echo ============================================
echo        EXECUTION DU SCANNER DE CONTROLLER
echo ============================================
echo.

java -cp %BIN%;%FRAMEWORK_JAR% com.app.Main

echo.
echo ============================================
echo              TEST TERMINE
echo ============================================
pause
