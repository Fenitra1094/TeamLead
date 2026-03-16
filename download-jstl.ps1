# Télécharger les fichiers JSTL nécessaires pour Jakarta EE
$libPath = ".\src\main\webapp\WEB-INF\lib"

# Créer le dossier lib s'il n'existe pas
if (!(Test-Path $libPath)) {
    New-Item -ItemType Directory -Path $libPath -Force
}

# URLs des fichiers JAR depuis Maven Central (version 3.0.1)
$jstlApiUrl = "https://repo1.maven.org/maven2/jakarta/servlet/jsp/jstl/jakarta.servlet.jsp.jstl-api/3.0.1/jakarta.servlet.jsp.jstl-api-3.0.1.jar"
$jstlImplUrl = "https://repo1.maven.org/maven2/jakarta/servlet/jsp/jstl/jakarta.servlet.jsp.jstl/3.0.1/jakarta.servlet.jsp.jstl-3.0.1.jar"

# Télécharger les fichiers
Write-Host "Téléchargement de jakarta.servlet.jsp.jstl-api-3.0.1.jar..."
Invoke-WebRequest -Uri $jstlApiUrl -OutFile "$libPath\jakarta.servlet.jsp.jstl-api-3.0.1.jar"
Write-Host "✓ Téléchargé"

Write-Host "Téléchargement de jakarta.servlet.jsp.jstl-3.0.1.jar..."
Invoke-WebRequest -Uri $jstlImplUrl -OutFile "$libPath\jakarta.servlet.jsp.jstl-3.0.1.jar"
Write-Host "✓ Téléchargé"

Write-Host "Tous les fichiers JSTL ont été téléchargés avec succès!"
