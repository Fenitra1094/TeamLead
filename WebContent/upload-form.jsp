<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>Upload Fichiers</title></head>
<body>
    <h1>Upload de Fichiers - Sprint 10</h1>
    
    <p><strong>Note :</strong> Tous les formulaires doivent avoir enctype="multipart/form-data"</p>
    
    <h2>1. Upload Fichier Unique</h2>
    <form action="upload/single" method="POST" enctype="multipart/form-data">
        Nom: <input type="text" name="nom"><br>
        Email: <input type="email" name="email"><br>
        Fichier: <input type="file" name="fichier"><br>
        <input type="submit" value="Upload Fichier Unique">
    </form>
    
    <hr>
    
    <h2>2. Upload Multiple Fichiers</h2>
    <form action="upload/multiple" method="POST" enctype="multipart/form-data">
        Titre: <input type="text" name="titre"><br>
        Documents: <input type="file" name="documents" multiple><br>
        <input type="submit" value="Upload Multiple">
    </form>
    
    <hr>
    
    <h2>3. Upload Complet</h2>
    <form action="upload/complete" method="POST" enctype="multipart/form-data">
        Projet: <input type="text" name="projet"><br>
        Description: <textarea name="description"></textarea><br>
        Rapport: <input type="file" name="rapport"><br>
        Images: <input type="file" name="images" multiple><br>
        Autres: <input type="file" name="autres"><br>
        <input type="submit" value="Upload Complet">
    </form>
    
    <br>
    <a href="/">← Retour accueil</a>
</body>
</html>