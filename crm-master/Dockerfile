# Utiliser l'image officielle de MySQL 8
FROM mysql:8.0

# Définir les variables d'environnement pour MySQL
ENV MYSQL_ROOT_PASSWORD=rootpassword
ENV MYSQL_DATABASE=crm
ENV MYSQL_USER=myuser
ENV MYSQL_PASSWORD=mypassword

# Exposer le port MySQL par défaut
EXPOSE 3306

# Commande par défaut pour démarrer MySQL
CMD ["mysqld"]