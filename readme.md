# 🏙️ Smart City - Gestion des Incidents Urbains

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg?logo=docker&logoColor=white)](https://www.docker.com/)

## 📋 1. Présentation du projet

**Smart City** est une application web conçue pour améliorer la gestion des incidents urbains dans une ville intelligente. Elle permet aux citoyens de signaler en temps réel des problèmes quotidiens (nids de poule, lampadaires défectueux, déchets, fuites d'eau, signalisation défectueuse, etc.) et aux services municipaux de les traiter efficacement.

### 🎯 Objectifs
- ✅ Renforcer la réactivité des autorités locales.
- ✅ Améliorer la qualité de vie des citoyens.
- ✅ Collecter des données pour des analyses urbaines futures.
- ✅ Assurer un suivi transparent des interventions.

### 🛠️ Technologies Utilisées
#### Backend
- **Langage** : Java 21
- **Framework** : Spring Boot 4.0.0
- **Sécurité** : Spring Security (BCrypt, CSRF protection)
- **Base de données** : PostgreSQL 15
- **Email** : Spring Mail (Gmail SMTP)

#### Frontend
- **Moteur de templates** : Thymeleaf
- **Cartographie** : Leaflet.js / Google Maps API
- **Design** : CSS personnalisé

#### DevOps & Outils
- **Conteneurisation** : Docker, Docker Compose
- **CI/CD** : GitHub Actions
- **Build** : Maven

---

## ⚙️ 2. Prérequis techniques

Pour exécuter ce projet, vous devez disposer des outils suivants :

* **Git** : Pour le versioning.
* **Java 21 (JDK)** : Version requise pour le backend.
* **Maven 3.8+** : Pour la gestion des dépendances et le build.
* **Docker & Docker Compose** : Pour l'environnement conteneurisé et la base de données.

---

## 💻 3. Instructions pour les développeurs

### Installation et Lancement Local (Sans Docker)

1.  **Cloner le projet**
    ```bash
    git clone [https://github.com/votre-username/smart-city.git](https://github.com/votre-username/smart-city.git)
    cd smart-city
    ```

2.  **Configurer la Base de Données**
    Assurez-vous d'avoir PostgreSQL 15 installé localement et créez la base :
    ```sql
    CREATE DATABASE incidentDB;
    CREATE USER postgres WITH PASSWORD 'fredj123';
    GRANT ALL PRIVILEGES ON DATABASE incidentDB TO postgres;
    ```

3.  **Configurer les Variables d'Environnement**
    Modifiez `application.properties` ou définissez les variables :
    * `DB_NAME`: incidentDB
    * `DB_USER`: postgres
    * `DB_PASS`: fredj123
    * `G_EMAIL`: votre.email@gmail.com
    * `G_PASS`: votre_mot_de_passe_app

4.  **Lancer l'application**
    ```bash
    mvn spring-boot:run
    ```
    L'application sera accessible sur : `http://localhost:8082`

5.  **Exécuter les Tests**
    Pour lancer les tests unitaires :
    ```bash
    mvn test
    ```

---

## 🐳 4. Instructions pour les DevOps

### Construction et Lancement avec Docker

Le projet est entièrement conteneurisé. Le fichier `docker-compose.yml` orchestre l'application Spring Boot et la base de données PostgreSQL.

1.  **Construire et démarrer les conteneurs**
    À la racine du projet :
    ```bash
    docker-compose up -d --build
    ```
    *Cette commande va :*
    * *Puller l'image PostgreSQL 15.*
    * *Construire l'image de l'application basée sur `eclipse-temurin:21-jdk-jammy`.*
    * *Lancer les services sur le port 8082.*

2.  **Vérifier le statut**
    ```bash
    docker ps
    ```

3.  **Arrêter les services**
    ```bash
    docker-compose down
    ```

### 🔄 Pipeline CI/CD

Le projet utilise **GitHub Actions** pour l'intégration et le déploiement continu.

#### Pipeline d'Intégration Continue (CI)
* **Fichier** : `.github/workflows/ci.yml` (nommé "Java CI")
* **Déclencheur** : Push ou Pull Request sur les branches `dev` et `main`.
* **Étapes** :
    1.  Checkout du code.
    2.  Installation de **Java 21**.
    3.  Mise en cache des dépendances Maven.
    4.  Compilation (`mvn clean package`).
    5.  Exécution des tests unitaires (`mvn test`).
    6.  Upload de l'artefact JAR (`smartcity-app`).

#### Pipeline de Déploiement Continu (CD)
* **Fichier** : `.github/workflows/cd.yml` (nommé "Java CD")
* **Déclencheur** : Push sur la branche `main` uniquement.
* **Étapes** :
    1.  Checkout du code et setup Java 21.
    2.  Build de l'application (sans les tests).
    3.  Construction de l'image Docker taguée avec le numéro de build (`1.0.x`).
    4.  Déploiement via le script `./deploy.sh` qui met à jour la stack Docker en production.

---

## 🚀 Fonctionnalités Détaillées

### 👤 Gestion des Utilisateurs
- **Inscription sécurisée** avec vérification d'email.
- **Trois rôles** : Citoyen, Agent Municipal, Administrateur.
- **Authentification** : Spring Security.

### 📢 Déclaration d'Incidents
- Upload de photos (jusqu'à 10MB).
- Géolocalisation automatique via Google Maps.
- Catégorisation (Infrastructure, Propreté, Sécurité, etc.).
- Priorisation (Basse, Moyenne, Élevée, Urgente).

### 🔄 Workflow des Incidents
1.  **Signalé** - Incident déclaré par un citoyen.
2.  **Pris en charge** - Assigné à un agent municipal.
3.  **En résolution** - Intervention en cours.
4.  **Résolu** - Travaux terminés.
5.  **Clôturé** - Dossier fermé après feedback.

### 📊 Tableaux de Bord
- **Citoyen** : Suivi de ses signalements.
- **Agent** : Gestion des tâches assignées.
- **Admin** : Statistiques globales et supervision.

---

## 👥 Comptes par Défaut

Une fois l'application lancée (Docker ou Local), vous pouvez utiliser les comptes suivants :

### Administrateur
- **Email** : `admin@smartcity.tn`
- **Mot de passe** : `admin123`

---

## 🎨 Captures d'Écran

| Dashboard Admin | Dashboard Citoyen |
|:---:|:---:|
| ![Dashboard Admin](screenshots/dashAdmin.png) | ![Dashboard Citoyen](screenshots/dashCitoyen.png) |

| Dashboard Agent | Déclaration Incident |
|:---:|:---:|
| ![Dashboard Agent](screenshots/dashAgent.png) | ![Déclarer Incident](screenshots/declare.png) |

---

## 📜 License

Ce projet a été réalisé dans le cadre de la matière **Développement Web Avancé**.

---

## 👨‍💻 Auteurs

**Développeurs principaux**
- **Chiraz Ben Boubaker** 
- **Oumayma El Heni** 

**Équipe Testeurs (QA)**
- **Imen Fredj**
- **Mohamed Aziz Rezgui**
- **Mekni Ali**

---

## 🙏 Remerciements

Nous tenons à remercier notre enseignant pour son encadrement, ses conseils et son soutien tout au long de la réalisation de ce projet.

**Fait avec ❤️ pour rendre nos villes plus intelligentes et réactives.**