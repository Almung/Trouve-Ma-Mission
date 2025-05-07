# Plateforme de gestion de projet

## Description
Ce projet est une plateforme de gestion de projet et collaborateur qui permet de gérer les missions et les ressources humaines. Il est composé d'une partie frontend et d'une partie backend.

## Architecture Technique

### Backend (trouve-ma-mission)
Le backend est développé en Java avec Spring Boot.

#### Technologies utilisées
- Java 17
- Spring Boot
- Maven
- PostgreSQL
- JPA/Hibernate
- Spring Security

#### Configuration de la Base de Données
1. Installer PostgreSQL sur votre machine
2. Créer une base de données nommée `mission_db`
3. Configurer les variables d'environnement ou modifier le fichier `application.properties` :
   ```properties
   # Configuration de la base de données
   spring.datasource.url=jdbc:postgresql://localhost:5432/mission_db
   spring.datasource.username=postgres
   spring.datasource.password=votre_mot_de_passe
   ```
   
   Vous pouvez aussi utiliser des variables d'environnement :
   - `DB_HOST` : l'hôte de la base de données (par défaut : localhost)
   - `DB_PORT` : le port de la base de données (par défaut : 5432)
   - `DB_NAME` : le nom de la base de données (par défaut : mission_db)
   - `DB_USERNAME` : le nom d'utilisateur PostgreSQL
   - `DB_PASSWORD` : le mot de passe PostgreSQL

4. Vérifier que la base de données est accessible avec les identifiants configurés

#### Scripts SQL de Création de la Base de Données
Les scripts SQL nécessaires à la création des tables de la base de données sont fournis avec l'application dans le dossier

#### Premier Lancement et Administrateur par Défaut
Lors du premier lancement de l'application, un compte administrateur est automatiquement créé. Pour l'utiliser :

1. Lancer l'application pour la première fois
2. Arrêter le serveur
3. Relancer le serveur
4. Utiliser les identifiants suivants pour vous connecter :
   - Email : admin@admin.com
   - Mot de passe : admin123

⚠️ Important : Pour des raisons de sécurité, il est fortement recommandé de changer le mot de passe de l'administrateur après la première connexion.

#### Prérequis pour le backend
- JDK 17 ou supérieur
- Maven
- PostgreSQL

#### Installation et démarrage du backend
1. Cloner le repository
2. Configurer la base de données dans `application.properties`
3. Exécuter la commande : `mvn spring-boot:run`

### Frontend (projet-tic-frontend)
Le frontend est développé avec React et Typescript.

#### Technologies utilisées
- React
- Typescript
- Node.js
- npm/yarn
- Material-UI (MUI)
- Redux

#### Prérequis pour le frontend
- Node.js (version LTS recommandée)
- npm ou yarn

#### Installation et démarrage du frontend
1. Cloner le repository
2. Installer les dépendances : `npm install` ou `yarn install`
3. Démarrer l'application : `npm start` ou `yarn start`

## Fonctionnalités principales
- Gestion des missions
- Gestion des ressources humaines
- Interface d'administration
- Tableau de bord
- Système d'authentification
- Gestion des profils


## Sécurité
- Authentification JWT
- Gestion des rôles et permissions
- Protection CSRF
- Validation des entrées


## Contact
Alain Tambwe alainmungal@gmail.com
Jean-Christ Adjovi 
