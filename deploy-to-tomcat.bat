@echo off
REM ====================================================
REM Script de Déploiement BackOffice Sprint 1 sur Tomcat
REM ====================================================

setlocal enabledelayedexpansion

set TOMCAT_PATH=C:\apache-tomcat-10.1.28\apache-tomcat-10.1.28
set WEBAPPS=%TOMCAT_PATH%\webapps
set WAR_FILE=test.war

echo ====================================================
echo Deploiement BackOffice Sprint 1
echo ====================================================
echo.

REM Vérifier que Tomcat existe
if not exist "%TOMCAT_PATH%" (
    echo ERREUR: Tomcat n'existe pas a %TOMCAT_PATH%
    pause
    exit /b 1
)

echo ✓ Tomcat trouvé

REM Copier le WAR
if exist "%WAR_FILE%" (
    echo.
    echo Copie du WAR vers Tomcat...
    copy /y "%WAR_FILE%" "%WEBAPPS%\"
    if errorlevel 1 (
        echo ERREUR: Impossible de copier le WAR
        pause
        exit /b 1
    )
    echo ✓ WAR copié avec succes
) else (
    echo ERREUR: %WAR_FILE% n'existe pas
    echo Compilez d'abord avec: deploy.bat
    pause
    exit /b 1
)

echo.
echo ====================================================
echo DÉPLOIEMENT COMPLETE!
echo ====================================================
echo.
echo Prochaines étapes:
echo 1. Démarrer Tomcat:
echo    cd "%TOMCAT_PATH%\bin"
echo    startup.bat
echo.
echo 2. Accéder a l'application:
echo    http://localhost:8080/test/reservation/form
echo.
echo 3. Consulter le guide:
echo    GUIDE_TEST_SPRINT1.md
echo.

pause
