# 📄 RouteParam [Type: Class]

## 🎯 Description
`RouteParam` représente un paramètre attendu par une route HTTP.

Il capture le type de résolution du paramètre (`PATH`, `QUERY`, `HEADER`, `BODY`, `AUTO`), son nom logique et son type Java cible.

## 🧠 Rôle dans l'architecture MicroBean
- Pourquoi ça existe: standardiser la description des paramètres de méthode controller pour le routeur REST.
- Quel problème ça résout: éviter la logique conditionnelle dispersée lors de la construction des arguments d'invocation.
- Où cela s'intègre dans le conteneur IoC: il est produit pendant l'analyse des signatures de méthodes et consommé lors de la résolution d'arguments HTTP.

## 🔗 Relations
- Dépend de:
  - `ParameterType`
  - `java.lang.Class`
  - `java.util.Objects`
- Utilisé par:
  - `RouteDefinition`
  - Infrastructure REST (binding des arguments)
- Concepts liés:
  - `@Path`, `@Query`, `@Header`, `@Body`
  - conversion de types HTTP -> Java

## ⚙️ Attributs
| Name    | Type            | Visibility           | Description                                       |
|---------|-----------------|----------------------|---------------------------------------------------|
| `type`  | `ParameterType` | `public` (component) | Type de résolution du paramètre HTTP.             |
| `name`  | `String`        | `public` (component) | Nom logique du paramètre (clé query/path/header). |
| `clazz` | `Class<?>`      | `public` (component) | Type Java cible.                                  |

## 🔧 Méthodes (le cas échéant)
| Method                                        | Return type     | Visibility | Description                                   |
|-----------------------------------------------|-----------------|------------|-----------------------------------------------|
| `RouteParam(ParameterType, String, Class<?>)` | `void`          | `public`   | Construit et valide les composants du record. |
| `type()`                                      | `ParameterType` | `public`   | Retourne le type de paramètre.                |
| `name()`                                      | `String`        | `public`   | Retourne le nom logique du paramètre.         |
| `clazz()`                                     | `Class<?>`      | `public`   | Retourne le type Java cible.                  |

## 💡 Exemple d'utilisation
```java
RouteParam idParam = new RouteParam(ParameterType.PATH, "id", long.class);
RouteParam nameParam = new RouteParam(ParameterType.QUERY, "name", String.class);

ParameterType type = idParam.type();
String name = idParam.name();
Class<?> clazz = idParam.clazz();
```

## 🔄 Comportement lié au cycle de vie (le cas échéant)
`RouteParam` est instancié pendant l'analyse des méthodes controller puis réutilisé pendant le traitement des requêtes pour construire les arguments de méthode.

## ⚠️ Limites / cas particuliers
- `type` et `clazz` sont obligatoires (`NullPointerException` si absents).
- `name` peut être `null` selon le type de paramètre (par exemple `BODY`).
- La conversion effective de valeur est réalisée en dehors de ce record.

## 📍 Notes internes sur MicroBean
- Le choix d'un `record` garantit l'immutabilité et simplifie le transport de métadonnées.
- Les validations minimales sont centralisées dans le constructeur compact.
