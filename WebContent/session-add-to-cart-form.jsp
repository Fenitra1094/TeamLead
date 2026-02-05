<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Ajouter au Panier</title>
</head>
<body>
    <h2>Ajouter un article au panier</h2>
    <form action="add-to-cart" method="post">
        <label for="item">Article :</label>
        <input type="text" id="item" name="item" required><br><br>
        <label for="quantity">Quantité :</label>
        <input type="number" id="quantity" name="quantity" min="1" value="1" required><br><br>
        <button type="submit">Ajouter au panier</button>
    </form>
    <br>
    <a href="session-test">Retour à l'accueil session</a>
</body>
</html>
