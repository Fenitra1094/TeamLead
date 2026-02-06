<!DOCTYPE html>
<html>
<head>
    <title>Debug Objects</title>
    <style>
        body { font-family: monospace; margin: 40px; font-size: 14px; }
        form { border: 1px solid #ccc; padding: 20px; margin: 20px 0; }
        input { margin: 5px; padding: 5px; }
        .test { background: #f0f0f0; padding: 10px; margin: 10px 0; }
    </style>
</head>
<body>
    <h1>🧪 Debug Binding Objets</h1>
    
    <div class="test">
        <h2>Test 1: Objet simple</h2>
        <form method="POST" action="${pageContext.request.contextPath}/test-simple/save">
            <strong>Paramètres envoyés:</strong> nom, prenom, age<br>
            <input type="text" name="nom" value="Dupont">
            <input type="text" name="prenom" value="Jean">
            <input type="number" name="age" value="30">
            <button>Test simple</button>
        </form>
    </div>
    
    <h3>📝 À observer dans la console:</h3>
    <pre>
✅ Débogage attendu:
1. Le framework détecte le paramètre "nom" 
2. Il trouve que c'est un objet Emp
3. Il crée l'objet Emp
4. Il recherche les paramètres qui commencent par "emp." ou "emp["
5. Il remplit les propriétés via les setters
    </pre>
</body>
</html>