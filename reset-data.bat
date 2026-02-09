@echo off
REM ====================================================
REM Script de Réinitialisation des Données de Test
REM Sprint 1 - BackOffice
REM ====================================================

setlocal enabledelayedexpansion

REM Configuration
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=reservation_voiture
set DB_USER=postgres
set SCRIPT_PATH=base\migrations\sprint1\V1.2__2026-02-09__reset_test_data.sql

echo ====================================================
echo Reinitialisation des Donnees de Test
echo ====================================================
echo.
echo Base de donnees: %DB_NAME%
echo Host: %DB_HOST%:%DB_PORT%
echo User: %DB_USER%
echo.

REM Vérifier que psql est disponible
where psql >nul 2>&1
if errorlevel 1 (
    echo ERREUR: psql n'est pas trouve dans le PATH
    echo Installer PostgreSQL ou ajouter son chemin au PATH
    pause
    exit /b 1
)

REM Vérifier que le script existe
if not exist "%SCRIPT_PATH%" (
    echo ERREUR: Script non trouve: %SCRIPT_PATH%
    pause
    exit /b 1
)

echo Script trouve: %SCRIPT_PATH%
echo.
echo Execution du script...
echo.

REM Exécuter le script
set PGPASSWORD=%DB_USER%
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "%SCRIPT_PATH%"

if errorlevel 1 (
    echo.
    echo ERREUR: Echec de la reinitialisation
    echo Verifiez:
    echo - PostgreSQL est en execution
    echo - Le mot de passe PostgreSQL est correct
    echo - La base de donnees existe
    pause
    exit /b 1
)

echo.
echo ====================================================
echo SUCCESS: Donnees reinitializees!
echo ====================================================
echo.
echo Les donnees suivantes ont ete creees:
echo - 4 Hotels (Ivandry, Carlton, Louvre, Colbert)
echo - 4 Reservations de test
echo.
echo Vous pouvez maintenant tester l'application!
echo.
pause
