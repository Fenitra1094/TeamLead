@echo off
REM Script pour installer Java 21 automatiquement
REM Utilise Microsoft.PowerShell pour télécharger et installer Java 21

echo ========================================
echo Installation de Java 21 LTS
echo ========================================

REM Chemin d'installation
set JAVA21_PATH=C:\Program Files\Java\jdk-21

REM Vérifier si Java 21 existe déjà
if exist "%JAVA21_PATH%" (
    echo Java 21 est déjà installé!
    echo Chemin: %JAVA21_PATH%
    goto setenv
)

echo.
echo Téléchargement et installation de Java 21...
echo.

REM Utiliser PowerShell pour télécharger Java 21 depuis Eclipse Temurin (gratuit et open-source)
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
"^
$url = 'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.2%2B13/OpenJDK21U-jdk_x64_windows_hotspot_21.0.2_13.zip'^
$zipPath = 'C:\temp\java21.zip'^
$extractPath = 'C:\Program Files\Java\'^
^
if (-not (Test-Path 'C:\temp')) { New-Item -ItemType Directory -Path 'C:\temp' -Force | Out-Null }^
^
Write-Host 'Téléchargement de Java 21...' -ForegroundColor Green^
try {^
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor [System.Net.SecurityProtocolType]::Tls12^
    (New-Object System.Net.WebClient).DownloadFile($url, $zipPath)^
    Write-Host 'Téléchargement réussi!' -ForegroundColor Green^
    ^
    Write-Host 'Extraction de Java 21...' -ForegroundColor Green^
    Expand-Archive -Path $zipPath -DestinationPath $extractPath -Force^
    Write-Host 'Java 21 installé avec succès!' -ForegroundColor Green^
    ^
    Remove-Item $zipPath -Force^
} catch {^
    Write-Host 'Erreur: $_' -ForegroundColor Red^
    exit 1^
}^
"

:setenv
REM Définir les variables d'environnement
setx JAVA_HOME %JAVA21_PATH%
setx PATH "%JAVA21_PATH%\bin;%PATH%"

echo.
echo ========================================
echo Java 21 est maintenant configuré!
echo ========================================
echo.
echo Vérification:
%JAVA21_PATH%\bin\java.exe -version

pause
