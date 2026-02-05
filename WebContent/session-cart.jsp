<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Panier</title></head>
<body>
    <h1>🛒 Panier mis à jour</h1>
    
    <p>Article ajouté : <strong>${item}</strong></p>
    <p>Quantité : ${quantity}</p>
    <p>Taille du panier : ${cartSize} articles</p>
    
    <br>
    <a href="view-cart">Voir panier complet</a> | 
    <a href="../session-test">← Retour</a>
</body>
</html>