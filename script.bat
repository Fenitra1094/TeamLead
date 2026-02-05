@echo off
REM =====================================
REM Variables
REM =====================================
set PROJECT_DIR=%cd%
set WEB_CONTENT=%PROJECT_DIR%\WebContent
set WEB_INF=%WEB_CONTENT%\WEB-INF
set LIB=%WEB_INF%\lib
set JAR_DIR=D:\S5\Framework\Framework\Framework\lib

set TOMCAT_HOME=C:\xampp\tomcat
set WEBAPPS=%TOMCAT_HOME%\webapps
set WAR_NAME=TestApp.war

REM =====================================
REM Vérifier que WEB-INF et lib existent
REM =====================================
if not exist "%LIB%" (
    mkdir "%LIB%"
)

REM =====================================
REM Copier les JAR du framework dans WEB-INF/lib
REM =====================================
for %%f in ("%JAR_DIR%\*.jar") do (
    if exist "%%f" (
        copy /Y "%%f" "%LIB%\" >nul
        if %ERRORLEVEL% NEQ 0 (
            echo ❌ Erreur lors de la copie de %%f
            exit /b 1
        )
        echo ✅ %%f copié dans %LIB%
    )
)

REM Créer WEB-INF/classes si inexistant
if not exist "%WEB_INF%\classes" (
    mkdir "%WEB_INF%\classes"
)

REM =====================================
REM Compiler toutes les classes Java
REM =====================================
set SRC_DIR=%PROJECT_DIR%\src
set CLASS_DIR=%WEB_INF%\classes

REM Créer un fichier temporaire contenant tous les fichiers Java
if exist sources.txt del sources.txt
for /R "%SRC_DIR%" %%f in (*.java) do echo %%f >> sources.txt

REM Compiler tous les fichiers Java
javac -cp "%WEB_INF%\lib\*" -d "%CLASS_DIR%" @sources.txt
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Erreur lors de la compilation des classes Java!
    del sources.txt
    exit /b 1
)
echo ✅ Classes compilées avec succès
del sources.txt


REM =====================================
REM Créer le WAR
REM =====================================
if exist "%WAR_NAME%" del "%WAR_NAME%"

REM Inclure WebContent et WEB-INF
jar cvf "%WAR_NAME%" -C "%WEB_CONTENT%" . -C "%WEB_INF%" .

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Erreur lors de la création du WAR!
    exit /b 1
)
echo ✅ WAR créé avec succès : %WAR_NAME%

REM =====================================
REM Déployer dans Tomcat
REM =====================================
if exist "%WEBAPPS%\%WAR_NAME%" del "%WEBAPPS%\%WAR_NAME%"
copy /Y "%WAR_NAME%" "%WEBAPPS%\" >nul
echo ✅ Application %WAR_NAME% déployée dans Tomcat/webapps

REM =====================================
REM Redémarrer Tomcat
REM =====================================
echo 🔄 Redémarrage de Tomcat...
"%TOMCAT_HOME%\bin\shutdown.bat"
timeout /t 3 /nobreak >nul
"%TOMCAT_HOME%\bin\startup.bat"

echo =====================================
echo 🚀 Déploiement terminé!
echo URL: http://localhost:8080/TestApp
echo =====================================
pause
