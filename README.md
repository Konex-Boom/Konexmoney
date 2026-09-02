# KonexMoney

Application Android de gestion de finances personnelles et de dettes intelligentes, conçue pour la devise Ariary (Ar).

## Fonctionnalités

- **Tableau de bord** — Vue d'ensemble du solde actuel, des entrées et sorties du mois en cours, avec des alertes intelligentes pour les remboursements possibles.
- **Transactions** — Enregistrement des entrées (revenus) et sorties (dépenses) avec catégories (Salaire, Alimentation, Transport, Loisirs, Santé, etc.), modes de paiement (Espèces, Mobile Money, Carte, Virement), et filtres avancés par type, catégorie et période.
- **Dettes** — Suivi des dettes actives et réglées : créances (« On me doit ») et emprunts (« Je dois »), avec gestion des échéances, reports et motifs.
- **Statistiques** — Analyse des tendances financières.
- **Onboarding** — Création de profil utilisateur avec photo, téléphone, email et date de naissance.
- **Intelligence artificielle** — Suggestions et alertes basées sur l'API Gemini pour optimiser la gestion financière.

## Technologies

- Kotlin / Jetpack Compose
- Room (base de données locale)
- Firebase AI (Gemini)
- Architecture MVVM
- Min SDK : 24 (Android 7.0+)

## Mise en route

1. Ouvrir le projet dans **Android Studio**
2. Créer un fichier `.env` à la racine du projet avec votre clé API Gemini :
   ```
   GEMINI_API_KEY=votre_cle_api
   ```
   (voir `.env.example` pour un exemple)
3. Supprimer la ligne `signingConfig = signingConfigs.getByName("debugConfig")` du fichier `build.gradle.kts` de l'app
4. Lancer l'application sur un émulateur ou un appareil physique
