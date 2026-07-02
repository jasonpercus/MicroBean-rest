# 📄 RouteDefinition [Type: Class]

## 🎯 Description
`RouteDefinition` représente une route HTTP compilée par l'infrastructure REST.

Cette classe centralise les informations de routage: méthode HTTP, chemin, méthode controller cible, paramètres attendus et métadonnées de matching.

## 🧠 Rôle dans l'architecture MicroBean
- Pourquoi ça existe: encapsuler la définition d'une route dans un objet immuable et réutilisable.
- Quel problème ça résout: éviter de recalculer à chaque requête les segments, index de variables et score de priorité.
- Où cela s'intègre dans le conteneur IoC: elle est construite pendant le scan des controllers puis utilisée par le routeur REST pour résoudre les requêtes entrantes.

## 🔗 Relations
- Dépend de:
  - `java.lang.reflect.Method`
  - `java.util.List`
  - `java.util.Map`
  - `RouteParam`
- Utilisé par:
  - Service d'exposition REST (sélection de route et invocation de méthode)
- Concepts liés:
  - `RouteParam`
  - `ParameterType`
  - matching de chemin HTTP

## ⚙️ Attributs
| Name                 | Type                   | Visibility      | Description                         |
|----------------------|------------------------|-----------------|-------------------------------------|
| `httpMethod`         | `String`               | `private final` | Méthode HTTP de la route.           |
| `pathPattern`        | `String`               | `private final` | Pattern de chemin normalisé.        |
| `controllerInstance` | `Object`               | `private final` | Instance controller à invoquer.     |
| `method`             | `Method`               | `private final` | Méthode Java cible.                 |
| `params`             | `List<RouteParam>`     | `private final` | Paramètres attendus par la méthode. |
| `score`              | `int`                  | `private final` | Score de spécificité de la route.   |
| `patternSegments`    | `List<String>`         | `private final` | Segments normalisés du chemin.      |
| `pathIndexes`        | `Map<String, Integer>` | `private final` | Positions des variables de chemin.  |

## 🔧 Méthodes (le cas échéant)
| Method                                                              | Return type            | Visibility       | Description                                                    |
|---------------------------------------------------------------------|------------------------|------------------|----------------------------------------------------------------|
| `RouteDefinition(String, String, Object, Method, List<RouteParam>)` | `void`                 | `public`         | Construit une définition de route immuable.                    |
| `getHttpMethod()`                                                   | `String`               | `public`         | Retourne la méthode HTTP.                                      |
| `getPathPattern()`                                                  | `String`               | `public`         | Retourne le pattern de chemin.                                 |
| `getControllerInstance()`                                           | `Object`               | `public`         | Retourne l'instance controller.                                |
| `getMethod()`                                                       | `Method`               | `public`         | Retourne la méthode Java cible.                                |
| `getParams()`                                                       | `List<RouteParam>`     | `public`         | Retourne les paramètres de route.                              |
| `getScore()`                                                        | `int`                  | `public`         | Retourne le score de spécificité.                              |
| `getPatternSegments()`                                              | `List<String>`         | `public`         | Retourne les segments du pattern.                              |
| `getPathIndexes()`                                                  | `Map<String, Integer>` | `public`         | Retourne l'index des variables de chemin.                      |
| `matchesPath(String)`                                               | `boolean`              | `public`         | Vérifie la correspondance d'un chemin entrant avec le pattern. |
| `extractPathParam(String, String)`                                  | `String`               | `public`         | Extrait la valeur d'une variable de chemin.                    |
| `buildPathIndexes(List<String>)`                                    | `Map<String, Integer>` | `private static` | Construit l'index des variables de chemin.                     |
| `computeScore(List<String>)`                                        | `int`                  | `private static` | Calcule le score de priorité de route.                         |
| `isPathVariable(String)`                                            | `boolean`              | `private static` | Identifie un segment variable (`{name}`).                      |
| `splitSegments(String)`                                             | `List<String>`         | `private static` | Découpe un chemin en segments normalisés.                      |
| `normalizePath(String)`                                             | `String`               | `private static` | Normalise le chemin HTTP.                                      |

## 💡 Exemple d'utilisation
```java
RouteDefinition route = new RouteDefinition(
        "GET",
        "/users/{id}",
        userController,
        userMethod,
        List.of(new RouteParam(ParameterType.PATH, "id", long.class))
);

boolean matches = route.matchesPath("/users/42");
String idValue = route.extractPathParam("id", "/users/42");
```

## 🔄 Comportement lié au cycle de vie (le cas échéant)
`RouteDefinition` est créée pendant le scan des controllers et reste utilisée en mémoire pendant la durée de vie du serveur REST pour le matching des requêtes.

## ⚠️ Limites / cas particuliers
- La normalisation retire le slash terminal (`/users/` devient `/users`).
- Le matching exige le même nombre de segments entre route et requête.
- Les variables de chemin sont reconnues uniquement au format `{name}`.
- `extractPathParam` retourne `null` si le nom n'existe pas dans la route.

## 📍 Notes internes sur MicroBean
- La classe est immuable pour éviter les effets de bord pendant le routage concurrent.
- Le score privilégie les chemins statiques (10 points) par rapport aux segments variables (1 point).
- L'index des variables de chemin est pré-calculé pour accélérer l'extraction des paramètres.
