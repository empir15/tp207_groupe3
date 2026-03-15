# PlanifyEdu 🎓

> **Système de gestion de planification universitaire** — Application de bureau Java/JavaFX avec base de données SQLite.

---

## 📋 Description

PlanifyEdu est une application académique qui permet de gérer :
- Les cours, enseignants, salles de classe et groupes d'étudiants
- Les horaires hebdomadaires (vue calendrier personnalisée)
- La planification des examens
- L'authentification et la gestion des rôles (admin, enseignant, étudiant)
- La détection automatique des conflits de planning
- L'export du planning en HTML

---

## 🏗️ Architecture du projet

```
PlanifyEdu/
│
├── src/
│   ├── MainApp.java                    ← Point d'entrée JavaFX
│   ├── model/                         ← Modèles (Cours, Enseignant, Salle, Planning, Examen, Groupe, Conflit, Role)
│   ├── controller/                    ← Contrôleurs JavaFX (Dashboard, Cours, Planning, Examen, Calendrier, Login)
│   ├── service/                       ← Accès aux données (CRUD SQLite, Auth, Conflit, Export, Groupe)
│   ├── database/                      ← Connexion SQLite (Singleton)
│   ├── ui/                            ← Interfaces FXML (Dashboard, Login, Calendrier, etc.)
│   └── styles/                        ← Styles CSS (dark mode)
│   └── util/                          ← Gestion de session
│
├── lib/                              ← Dépendances externes (sqlite-jdbc, slf4j)
├── database/
│   └── planning.db                    ← Base SQLite (créée automatiquement)
│
├── pom.xml                            ← Configuration Maven
└── README.md
```

---

## ⚙️ Prérequis

| Outil         | Version minimale |
|---------------|-----------------|
| Java JDK      | 17+             |
| Apache Maven  | 3.8+            |
| JavaFX        | 21 (géré par Maven) |
| sqlite-jdbc   | inclus           |
| slf4j         | inclus           |

---

## 🚀 Installation & Lancement

1. **Cloner le projet**
   ```powershell
   git clone <url_du_repo>
   cd tp207_groupe3
   ```

2. **Exécuter l'application (Windows)**
   ```powershell
   .\build-and-run.bat
   ```

3. **Créer un JAR exécutable (optionnel)**
   ```powershell
   mvn clean package
   java -jar target/planifyedu-1.0.0.jar
   ```

---

## 🗄️ Base de données

- La base SQLite est créée automatiquement au premier lancement dans `database/planning.db`.
- Aucun paramétrage manuel requis.

**Schéma des tables :**
```sql
CREATE TABLE courses (id, nom, description);
CREATE TABLE teachers (id, nom, prenom, email);
CREATE TABLE classrooms (id, nom, capacite);
CREATE TABLE planning (id, course_id, teacher_id, classroom_id, jour, heure);
CREATE TABLE exams (id, course_id, date, heure, classroom_id);
CREATE TABLE groupes (id, nom, niveau, anneeScolaire);
CREATE TABLE users (id, username, password, role);
CREATE TABLE conflits (id, type, message, ressource, creneau);
```

---

## 🎨 Interface utilisateur

- Design dark mode moderne (indigo/violet sur fond sombre)
- Tableaux stylisés, cartes avec effets de survol, animations CSS
- Formulaires intuitifs avec validation
- Interface de connexion sécurisée
- Vue calendrier personnalisée par groupe ou enseignant

---

## 📦 Modules de l'application

| Module         | Fonctionnalité                                 |
|----------------|------------------------------------------------|
| Dashboard      | Vue d'ensemble, statistiques temps réel         |
| Cours          | Ajouter, lister, supprimer des cours            |
| Enseignants    | Gérer le corps enseignant                       |
| Salles         | Gérer les salles et leur capacité               |
| Groupes        | Gérer les groupes d'étudiants                   |
| Horaires       | Créer des créneaux cours (jour + heure)         |
| Examens        | Planifier des examens (date et salle)           |
| Calendrier     | Vue hebdomadaire personnalisée                  |
| Authentification | Connexion, gestion des rôles                   |
| Conflits       | Détection automatique des conflits de planning   |
| Export         | Export du planning en HTML                      |

---

## 🏛️ Pattern MVC

- **Model** : POJOs Java (Cours, Enseignant, Salle, Planning, Examen, Groupe, Conflit, Role)
- **View** : Fichiers FXML + CSS (`src/ui/`, `src/styles/`)
- **Controller** : Contrôleurs JavaFX (`src/controller/`)
- **Service** : Accès aux données et logique métier (`src/service/`)
- **Util** : Gestion de session utilisateur (`src/util/`)

---

## 💡 Conseils

- Utilisez Java 17+ et Maven 3.8+ pour éviter les problèmes de compatibilité.
- L’application est prête à l’emploi : lancez simplement le script ou le JAR.
- La base de données se crée automatiquement, aucune configuration manuelle.
- Pour exporter le planning, utilisez la fonction dédiée dans l’interface.
- La détection des conflits est automatique lors de la création/modification d’un créneau.

---

*Projet académique — Gestion de planification universitaire*
```
