@echo off
setlocal enabledelayedexpansion

set APP_NAME=test
set SRC=src
set BUILD=build
set LIB_WEBINF=WEB-INF\lib
set CLASSES_WEBINF=WEB-INF\classes
set TOMCAT_LIB=C:\tomcat10\apache-tomcat-10.1.28\lib
set FRAMEWORK_JAR=..\Framework\framework.jar
set WAR_NAME=%APP_NAME%.war
set TOMCAT_PATH=C:\tomcat10\apache-tomcat-10.1.28
set WEBAPP_SOURCE=%SRC%\main\webapp
set APP_LIB=%WEBAPP_SOURCE%\WEB-INF\lib

rem Nettoyage
echo Nettoyage du build...
if exist %BUILD% rmdir /s /q %BUILD%
mkdir %BUILD%
mkdir %BUILD%\WEB-INF\classes
mkdir %BUILD%\WEB-INF\lib

rem Compilation
echo Compilation des fichiers source...

rem Créer la liste des fichiers .java avec chemins relatifs
cd %SRC%
for /r %%i in (*.java) do (
    echo %%~fi >> ..\sources_abs.tmp
)
cd ..

rem Convertir les chemins absolus en relatifs
set "CURRENT_DIR=%CD%"
(for /f "usebackq delims=" %%a in ("sources_abs.tmp") do (
    set "abs_path=%%a"
    set "rel_path=!abs_path:%CURRENT_DIR%\=!"
    echo !rel_path!
)) > sources.txt

del sources_abs.tmp 2>nul

javac -classpath "%TOMCAT_LIB%\servlet-api.jar;%FRAMEWORK_JAR%;%APP_LIB%\*" -parameters -d %BUILD%\WEB-INF\classes @"sources.txt"
if errorlevel 1 (
    echo Erreur de compilation!
    del sources.txt 2>nul
    pause
    exit /b 1
)
del sources.txt 2>nul


rem Copier framework.jar
copy /y %FRAMEWORK_JAR% %BUILD%\WEB-INF\lib\

rem Copier les libs de l'application (postgresql, etc.)
if exist "%APP_LIB%" (
    xcopy /s /y /i "%APP_LIB%\*.jar" "%BUILD%\WEB-INF\lib\" >nul
)

rem =====================================
rem COPIE COMPLETE DE WEB-INF
rem =====================================
echo Copie de tous les fichiers et dossiers WEB-INF...

rem Copier tout le contenu de WEB-INF depuis webapp
if exist "%WEBAPP_SOURCE%\WEB-INF" (
    rem Copier tous les fichiers à la racine de WEB-INF
    if exist "%WEBAPP_SOURCE%\WEB-INF\*.*" (
        for %%F in ("%WEBAPP_SOURCE%\WEB-INF\*.*") do (
            copy /y "%%F" "%BUILD%\WEB-INF\" >nul 2>&1
            echo ✅ %%~nxF copié
        )
    )
    
    rem Copier tous les sous-dossiers et leur contenu (sauf classes et lib qui sont gérés autrement)
    for /d %%D in (%WEBAPP_SOURCE%\WEB-INF\*) do (
        if /i not "%%~nxD"=="classes" (
            if /i not "%%~nxD"=="lib" (
                xcopy /s /y /i "%%D" "%BUILD%\WEB-INF\%%~nxD\" >nul
                echo ✅ WEB-INF\%%~nxD copié
            )
        )
    )
    
    echo ✅ Contenu complet de WEB-INF copié
) else (
    echo ⚠️ ATTENTION: %WEBAPP_SOURCE%\WEB-INF n'existe pas!
)

rem =====================================
rem COPIE DES RESSOURCES PUBLIQUES
rem =====================================
echo Copie des ressources publiques...

rem Copier les fichiers publics à la racine de webapp
if exist "%WEBAPP_SOURCE%\*.jsp" (
    copy /y "%WEBAPP_SOURCE%\*.jsp" %BUILD%\
    echo ✅ Fichiers JSP publics copiés
)

if exist "%WEBAPP_SOURCE%\*.html" (
    copy /y "%WEBAPP_SOURCE%\*.html" %BUILD%\
    echo ✅ Fichiers HTML publics copiés
)

if exist "%WEBAPP_SOURCE%\*.css" (
    copy /y "%WEBAPP_SOURCE%\*.css" %BUILD%\
    echo ✅ Fichiers CSS copiés
)

if exist "%WEBAPP_SOURCE%\*.js" (
    copy /y "%WEBAPP_SOURCE%\*.js" %BUILD%\
    echo ✅ Fichiers JS copiés
)

rem Copier tous les dossiers publics à la racine de webapp (css, js, images, etc.)
for /d %%D in (%WEBAPP_SOURCE%\*) do (
    if /i not "%%~nxD"=="WEB-INF" (
        if /i not "%%~nxD"=="META-INF" (
            xcopy /s /y /i "%%D" "%BUILD%\%%~nxD\" >nul
            echo ✅ %%~nxD copié
        )
    )
)

rem Copier META-INF
if exist "%WEBAPP_SOURCE%\META-INF" (
    xcopy /s /y "%WEBAPP_SOURCE%\META-INF" "%BUILD%\META-INF\" >nul
    echo ✅ META-INF copié
)

rem Vérifier la structure
echo.
echo 📁 Structure créée dans %BUILD%:
dir %BUILD% /b
echo.
echo 📁 Contenu de WEB-INF:
dir "%BUILD%\WEB-INF" /b
echo.
if exist "%BUILD%\WEB-INF\views" (
    echo 📁 Contenu de WEB-INF\views:
    dir "%BUILD%\WEB-INF\views" /s /b
    echo.
)

rem Créer le WAR
echo Création du WAR...
cd %BUILD%
jar cvf ..\%WAR_NAME% . >nul
cd ..

echo ✅ %WAR_NAME% genere avec succes

rem Déployer sur Tomcat
echo Déploiement sur Tomcat...
copy /y "%WAR_NAME%" "%TOMCAT_PATH%\webapps\"

echo.
echo 🎯 DEPLOIEMENT TERMINE!
echo.

