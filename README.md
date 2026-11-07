# TP 10 — Formulaire de connexion personnalisé

Médiathèque USMBA : une application Spring Boot qui remplace la page de connexion par défaut de Spring Security par un formulaire maison, entièrement en français, et qui aiguille chaque utilisateur vers l'espace correspondant à son rôle dès l'authentification réussie.

Ce TP reprend le mécanisme demandé dans l'énoncé — page de connexion personnalisée, gestion des erreurs, redirection après succès, déconnexion et contrôle d'accès par rôle — mais le transpose dans un domaine métier différent et remplace la page `home` commune par un aiguillage conditionnel.

## Ce que fait l'application

La page d'accueil et le formulaire de connexion sont publics. Toute tentative d'ouverture d'un espace métier sans session valide est interceptée par la chaîne de filtres et renvoie vers `/connexion`. Le formulaire poste sur `/authentification` avec deux champs francisés, `identifiant` et `motDePasse`.

En cas d'échec, l'utilisateur revient sur `/connexion?erreur=true` avec un message générique. En cas de succès, un `AuthenticationSuccessHandler` maison lit les autorités du compte et le dirige directement vers son tableau de bord, sans page intermédiaire. La déconnexion poste sur `/deconnexion`, invalide la session, supprime le cookie `JSESSIONID` et ramène sur `/connexion?deconnexion=true`.

Quatre rôles sont définis : `LECTEUR`, `BIBLIOTHECAIRE`, `CONSERVATEUR` et `ADMIN`. Ils sont cumulatifs : un conservateur possède aussi les droits bibliothécaire et lecteur.

## Comptes de démonstration

- `lecteur` / `4321` — Yasmine Berrada, étudiante en master informatique, rôle LECTEUR
- `lecteur2` / `9753` — Salma Idrissi, étudiante en licence biologie, rôle LECTEUR
- `biblio` / `8765` — Rachid Ouhadi, bibliothécaire au service du prêt, rôle BIBLIOTHECAIRE
- `conservateur` / `2468` — Latifa Aït Ali, conservatrice du fonds ancien, rôle CONSERVATEUR
- `admin` / `1357` — Mounir Sbai, administrateur du système, rôle ADMIN

Les secrets n'existent en clair que dans l'annuaire de déclaration : au démarrage, ils sont encodés en BCrypt et seul le hash est conservé en mémoire par le `UserDetailsService`.

## Prérequis

JDK 21, Maven 3.9 ou supérieur. Aucune base de données n'est nécessaire, tout tient en mémoire et se réinitialise à chaque redémarrage.

Pour lancer l'application :

```
mvn spring-boot:run
```

L'application écoute ensuite sur `http://localhost:8080`.

## Résultats obtenus

### Page d'accueil publique

![Page d'accueil publique](docs/screenshots/01-accueil-public.png)

La racine du site est accessible sans authentification. Un visiteur anonyme ne voit que les liens « Accueil » et « Se connecter » : les entrées des espaces métier et le bouton de déconnexion n'apparaissent qu'une fois la session ouverte.

### Redirection vers la connexion

![Redirection vers la page de connexion](docs/screenshots/02-redirection-connexion.png)

Une requête sur `/espace/lecteur/tableau-de-bord` sans session valide est interceptée par le filtre de sécurité et renvoyée vers le formulaire personnalisé. Le visiteur ne voit jamais la page de connexion générée par Spring.

### Refus d'un mot de passe erroné

![Échec de connexion](docs/screenshots/03-echec-connexion.png)

L'identifiant `lecteur` accompagné du mot de passe `mauvais` renvoie sur `/connexion?erreur=true`. Le message reste volontairement générique : il ne précise jamais si c'est l'identifiant ou le mot de passe qui est en cause.

### Espace lecteur après connexion

![Espace lecteur](docs/screenshots/04-espace-lecteur.png)

La connexion de `lecteur` aboutit directement sur `/espace/lecteur/tableau-de-bord` grâce au gestionnaire de succès. La page salue la lectrice par son nom, affiche ses seuls prêts et rappelle le périmètre réel du profil, à savoir que les trois autres espaces lui sont fermés.

### Accès refusé pour un rôle insuffisant

![Page 403](docs/screenshots/05-acces-refuse-403.png)

Toujours connecté en tant que lecteur, l'appel à `/espace/administration/tableau-de-bord` est bloqué par la chaîne de filtres avant même d'atteindre le contrôleur. La réponse HTTP est bien un 403, pas une redirection, et la page d'erreur affichée est une template Thymeleaf déclarée via `accessDeniedPage`.

### Déconnexion

![Déconnexion](docs/screenshots/06-deconnexion.png)

Le bouton de déconnexion de la barre de navigation envoie un POST sur `/deconnexion`. La session est invalidée, le cookie supprimé, puis l'utilisateur revient sur le formulaire avec le message de confirmation.

### Espace bibliothécaire

![Espace bibliothécaire](docs/screenshots/07-espace-bibliothecaire.png)

Le compte `biblio` est aiguillé vers le service du prêt, où il retrouve l'ensemble des emprunts avec les retards à relancer. La barre de navigation s'adapte au rôle : les entrées « Prêt » et « Lecteur » apparaissent, les autres non.

### Espace conservation

![Espace conservation](docs/screenshots/08-espace-conservation.png)

Le compte `conservateur` arrive sur l'état du fonds documentaire. Son menu cumule les trois niveaux de droits, puisque le rôle CONSERVATEUR hérite de BIBLIOTHECAIRE et de LECTEUR.

### Espace administration

![Espace administration](docs/screenshots/09-espace-administration.png)

Le compte `admin` voit en plus la liste des comptes réellement déclarés dans le `UserDetailsService`, avec leur rôle principal. Aucun mot de passe n'y figure : seul le hash BCrypt est conservé côté serveur.

## Choix d'implémentation

Le contrôle d'accès tient dans une `SecurityFilterChain` unique, avec des règles ordonnées du plus spécifique au plus général et `anyRequest().authenticated()` en dernier recours. Les URL de connexion, de traitement et de déconnexion sont toutes personnalisées, ainsi que les noms des champs du formulaire : `identifiant` et `motDePasse` remplacent les `username` et `password` par défaut.

Les comptes sont décrits dans un annuaire métier (`AnnuaireLecteurs`) puis convertis en objets `User` par le `UserDetailsService`. Les rôles sont attribués de façon cumulative à partir d'une liste ordonnée, ce qui garde la hiérarchie lisible et évite de répéter les habilitations pour chaque compte.

La partie la plus intéressante du TP est la redirection après succès. L'énoncé utilise `defaultSuccessUrl("/home", true)` avec une page commune, ce qui oblige la vue à décider quoi montrer selon le rôle. Ici, un `AuthenticationSuccessHandler` dédié (`RedirectionParRole`) inspecte les autorités du compte avant de construire la redirection : administration, conservation, prêt ou lecteur, dans cet ordre de priorité. Aucune page intermédiaire n'est nécessaire et la logique reste dans la couche de sécurité.

Le formulaire lui-même ne contient aucune logique : il se contente d'afficher trois messages possibles (erreur, déconnexion, session expirée) et de poster les deux champs attendus. Le jeton CSRF est injecté automatiquement par Spring Security, sans champ caché à écrire à la main.

## Personnalisation par rapport à l'énoncé

L'énoncé construit un projet minimal dans le paquetage `ma.fstg.security`, avec deux comptes `admin` / `{noop}1234` et `user` / `{noop}1111`, une page `home` commune et des vues de démonstration sans contenu. La présente version s'en écarte volontairement :

- le paquetage et l'artefact sont propres au projet (`ma.usmba.mediatheque` et `mediatheque-securisee`) ;
- le domaine est une médiathèque universitaire réelle, avec un catalogue, des prêts et quatre espaces métier, plutôt que des pages vides ;
- cinq comptes et quatre rôles hiérarchisés remplacent les deux comptes de l'énoncé ;
- les URL et les noms des champs du formulaire sont francisés (`/connexion`, `/authentification`, `/deconnexion`, `identifiant`, `motDePasse`) ;
- la page `home` commune est remplacée par un aiguillage automatique par rôle, ce qui illustre concrètement le point « flexibilité » de l'énoncé ;
- BCrypt est utilisé dès le départ, sans passer par les mots de passe `{noop}` de la version intermédiaire ;
- une page 403 dédiée a été ajoutée pour les rôles insuffisants.

## Structure du projet

Le code se répartit en trois paquetages. `modele` contient l'annuaire des comptes et le catalogue avec ses prêts. `config` contient la chaîne de filtres, le `UserDetailsService` et le gestionnaire de redirection. `web` contient les contrôleurs d'accueil et des quatre espaces.

Les vues Thymeleaf sont dans `src/main/resources/templates`, avec un fragment partagé pour l'en-tête, la navigation et le pied de page. La feuille de style est dans `src/main/resources/static/css`.
