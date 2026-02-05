<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Tests Upload</title></head>
<body>
    <h1>Tests Upload Fichiers</h1>
    
    <h2>Test 1: Upload Simple</h2>
    <form action="upload-test/simple" method="POST" enctype="multipart/form-data">
        Fichier: <input type="file" name="monfichier"><br>
        <input type="submit" value="Test Simple">
    </form>
    
    <h2>Test 2: Upload avec Paramètres</h2>
    <form action="upload-test/with-params" method="POST" enctype="multipart/form-data">
        Username: <input type="text" name="username"><br>
        Age: <input type="number" name="age"><br>
        Avatar: <input type="file" name="avatar"><br>
        <input type="submit" value="Test avec Params">
    </form>
    
    <h2>Test 3: Upload Multiple</h2>
    <form action="upload-test/multiple" method="POST" enctype="multipart/form-data">
        Photos: <input type="file" name="photos" multiple><br>
        <small>Astuce: Maintenez Ctrl (ou Cmd sur Mac) pour sélectionner plusieurs fichiers</small><br>
        <input type="submit" value="Test Multiple">
    </form>
    
    <h2>Test 4: Upload avec User Object</h2>
    <form action="upload-test/with-user" method="POST" enctype="multipart/form-data">
        Nom: <input type="text" name="name"><br>
        Email: <input type="email" name="email"><br>
        Document: <input type="file" name="document"><br>
        <input type="submit" value="Test avec User">
    </form>
    
    <h2>Test 5: Upload avec Map</h2>
    <form action="upload-test/with-map" method="POST" enctype="multipart/form-data">
        Ville: <input type="text" name="ville"><br>
        Pays: <input type="text" name="pays"><br>
        Fichier: <input type="file" name="fichier"><br>
        <input type="submit" value="Test avec Map">
    </form>
    
    <h2>Test 6: Upload Complet</h2>
    <form action="upload-test/complete" method="POST" enctype="multipart/form-data">
        Titre: <input type="text" name="titre"><br>
        Nom User: <input type="text" name="name"><br>
        Email: <input type="email" name="email"><br>
        CV: <input type="file" name="cv"><br>
        Certificats: <input type="file" name="certificats" multiple><br>
        Note: <input type="text" name="note"><br>
        <input type="submit" value="Test Complet">
    </form>
    
    <h2>Test 7: Upload JSON Response</h2>
    <form action="upload-test/json" method="POST" enctype="multipart/form-data">
        Nom: <input type="text" name="nom"><br>
        Fichier: <input type="file" name="fichier"><br>
        <input type="submit" value="Test JSON">
    </form>
</body>
</html>