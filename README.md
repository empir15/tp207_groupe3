# PlanifyEdu 🎓

> **Système de gestion de planification universitaire** — Application de bureau Java/JavaFX avec base de données SQLite.

---

## 📋 Description

PlanifyEdu est une application académique qui permet de gérer :
- Les cours, enseignants et salles de classe
- Les horaires hebdomadaires
- La planification des examens

---

## 🏗️ Architecture du projet

```
PlanifyEdu/
│
├── src/
│   ├── MainApp.java                    ← Point d'entrée JavaFX
│   ├── model/                         ← Modèles (Cours, Enseignant, Salle, Planning, Examen)
│   ├── controller/                    ← Contrôleurs JavaFX
│   ├── service/                       ← Accès aux données (CRUD SQLite)
│   ├── database/                      ← Connexion SQLite (Singleton)
│   ├── ui/                            ← Interfaces FXML
│   └── styles/                        ← Styles CSS (dark mode)
│
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

---

## 🚀 Installation & Lancement

1. **Cloner le projet**
   ```bash
   git clone <url_du_repo>
   cd tp207_groupe3
   ```

2. **Exécuter l'application (Windows)**
   ```bash
   .\build-and-run.bat
   ```

3. **Créer un JAR exécutable (optionnel)**
   ```bash
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
```

---

## 🎨 Interface utilisateur

- Design dark mode moderne (indigo/violet sur fond sombre)
- Tableaux stylisés, cartes avec effets de survol, animations CSS
- Formulaires intuitifs avec validation

---

## 📦 Modules de l'application

| Module         | Fonctionnalité                                 |
|----------------|------------------------------------------------|
| Dashboard      | Vue d'ensemble, statistiques temps réel         |
| Cours          | Ajouter, lister, supprimer des cours            |
| Enseignants    | Gérer le corps enseignant                       |
| Salles         | Gérer les salles et leur capacité               |
| Horaires       | Créer des créneaux cours (jour + heure)         |
| Examens        | Planifier des examens (date et salle)           |

---

## 🏛️ Pattern MVC

- **Model** : POJOs Java (Cours, Enseignant, Salle, Planning, Examen)
- **View** : Fichiers FXML + CSS (`src/ui/`, `src/styles/`)
- **Controller** : Contrôleurs JavaFX (`src/controller/`)
- **Service** : Accès aux données (`src/service/`)

---

## 💡 Conseils

- Utilisez Java 17+ et Maven 3.8+ pour éviter les problèmes de compatibilité.
- L’application est prête à l’emploi : lancez simplement le script ou le JAR.
- La base de données se crée automatiquement, aucune configuration manuelle.

---

*Projet académique — Gestion de planification universitaire*
"# tp_207_groupe3"  
"# tp207_groupe3"  
