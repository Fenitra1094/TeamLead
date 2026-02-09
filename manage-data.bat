@echo off
REM ====================================================
REM Menu de Gestion des Donnees de Test
REM Sprint 1 - BackOffice
REM ====================================================

setlocal enabledelayedexpansion

:menu
cls
echo ====================================================
echo Menu de Gestion des Donnees
echo ====================================================
echo.
echo Choisissez une option:
echo.
echo 1. Reinitialiser les donnees de test
echo    (Supprime et reinsere 4 hotels + 4 reservations)
echo.
echo 2. Nettoyer TOUTES les donnees
echo    (Supprime TOUS les hotels et reservations)
echo.
echo 3. Afficher les donnees actuelles
echo    (Interroge les tables)
echo.
echo 4. Quitter
echo.
set /p choice="Entrez votre choix (1-4): "

if "%choice%"=="1" (
    call :reset_data
    goto menu
)
if "%choice%"=="2" (
    call :clean_all
    goto menu
)
if "%choice%"=="3" (
    call :show_data
    goto menu
)
if "%choice%"=="4" (
    exit /b 0
)

echo Choix invalide!
timeout /t 2 >nul
goto menu

REM ====================================================
REM Sous-routines
REM ====================================================

:reset_data
echo.
echo Reinitialisation des donnees...
echo.
call reset-data.bat
goto :eof

:clean_all
echo.
echo ATTENTION: Vous allez supprimer TOUTES les donnees!
echo.
set /p confirm="Confirmez (oui/non): "
if /i "%confirm%"=="oui" (
    echo Nettoyage en cours...
    set PGPASSWORD=postgres
    psql -h localhost -p 5432 -U postgres -d reservation_voiture -f "base\migrations\sprint1\V1.3__2026-02-09__clean_all_data.sql"
    if errorlevel 1 (
        echo ERREUR lors du nettoyage!
    ) else (
        echo Nettoyage complete!
    )
) else (
    echo Annule.
)
pause
goto :eof

:show_data
echo.
echo Affichage des donnees actuelles...
echo.
set PGPASSWORD=postgres
psql -h localhost -p 5432 -U postgres -d reservation_voiture -c "SELECT 'Hotels:' as Info; SELECT * FROM staging.hotel; SELECT 'Reservations:' as Info; SELECT * FROM staging.reservation;"
echo.
pause
goto :eof
