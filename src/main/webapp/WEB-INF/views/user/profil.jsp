<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profil Utilisateur - Framework Maison</title>

</head>
<body>
    <div class="profile-container">
        <div class="profile-header">
            <div class="profile-avatar">
                👤
            </div>
            <h1>${username}</h1>
            <p>Profil Utilisateur</p>
        </div>
        
        <div class="profile-content">
            <div class="info-group">
                <div class="info-label">Nom d'utilisateur</div>
                <div class="info-value">
                    ${username}
                    <span class="status-badge">Actif</span>
                </div>
            </div>
            
            <div class="info-group">
                <div class="info-label">Adresse Email</div>
                <div class="info-value">${email}</div>
            </div>
            
            <div class="info-group">
                <div class="info-label">Membre depuis</div>
                <div class="info-value"><%= new java.util.Date() %></div>
            </div>
            
            <div class="framework-info">
                <h3>🚀 Framework Maison</h3>
                <p>Cette page est rendue grâce à votre framework personnalisé ! Les données sont passées via ModelView.</p>
            </div>
            
            <a href="${pageContext.request.contextPath}/user/list" class="back-link">
                ← Retour à la liste des utilisateurs
            </a>
        </div>
    </div>

    <script>
        // Animation simple au chargement
        document.addEventListener('DOMContentLoaded', function() {
            const container = document.querySelector('.profile-container');
            container.style.opacity = '0';
            container.style.transform = 'translateY(20px)';
            
            setTimeout(() => {
                container.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                container.style.opacity = '1';
                container.style.transform = 'translateY(0)';
            }, 100);
        });
    </script>
</body>
</html>