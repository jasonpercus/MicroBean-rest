# 📄 HttpClientService [Type: Classe]

## 🎯 Description
`HttpClientService` est un client HTTP fluide destiné à consommer des endpoints REST exposés par MicroBean ou par toute API JSON compatible.

Il permet de construire une requête de manière progressive en ajoutant la méthode HTTP, des paramètres de chemin, des paramètres de query, des headers et un corps JSON. La réponse est encapsulée dans un objet typé qui expose à la fois le code HTTP, les headers, le corps brut et, si possible, le corps désérialisé.

## 🧠 Rôle dans l'architecture MicroBean
- Pourquoi ça existe: fournir un consommateur HTTP simple, cohérent avec les conventions du serveur REST MicroBean.
- Quel problème cela résoud: éviter l'écriture répétitive de code d'appel HTTP, de sérialisation JSON et de gestion basique des réponses.

## 🔗 Relations
- Dépend de :
  - `java.net.HttpURLConnection`
  - `java.net.URL`
  - `java.net.URLEncoder`
  - `com.fasterxml.jackson.databind.ObjectMapper`
  - `java.nio.charset.StandardCharsets`
- Utilisé par :
  - Code applicatif consommant des routes REST MicroBean
  - Tests d'intégration côté client
- Concepts liés :
  - `RestServerService`
  - routes annotées avec `@Get`, `@Post`, `@Put`, `@Delete`, `@Patch`, `@Head`, `@Options`
  - `HttpResponse`

## ⚙️ Attributs
| Name               | Type           | Visibility             | Description                                                     |
|--------------------|----------------|------------------------|-----------------------------------------------------------------|
| `APPLICATION_JSON` | `String`       | `private static final` | Type MIME utilisé pour les échanges JSON.                       |
| `METHOD_GET`       | `String`       | `private static final` | Nom de la méthode HTTP `GET`.                                   |
| `METHOD_POST`      | `String`       | `private static final` | Nom de la méthode HTTP `POST`.                                  |
| `METHOD_PUT`       | `String`       | `private static final` | Nom de la méthode HTTP `PUT`.                                   |
| `METHOD_PATCH`     | `String`       | `private static final` | Nom de la méthode HTTP `PATCH`.                                 |
| `METHOD_DELETE`    | `String`       | `private static final` | Nom de la méthode HTTP `DELETE`.                                |
| `METHOD_HEAD`      | `String`       | `private static final` | Nom de la méthode HTTP `HEAD`.                                  |
| `METHOD_OPTIONS`   | `String`       | `private static final` | Nom de la méthode HTTP `OPTIONS`.                               |
| `SEPARATOR`        | `String`       | `private static final` | Séparateur de chemin utilisé pour normaliser les URLs.          |
| `TIMEOUT_MS`       | `int`          | `private static final` | Timeout par défaut pour la connexion et la lecture.             |
| `baseUrl`          | `String`       | `private final`        | URL de base normalisée du client.                               |
| `objectMapper`     | `ObjectMapper` | `private final`        | Moteur Jackson utilisé pour sérialiser et désérialiser le JSON. |

## 🔧 Méthodes
| Method                                                                                                 | Return type                 | Visibility | Description                                                                                   |
|--------------------------------------------------------------------------------------------------------|-----------------------------|------------|-----------------------------------------------------------------------------------------------|
| `HttpClientService(String baseUrl)`                                                                    | `void`                      | `public`   | Construit un client à partir d'une URL de base normalisée.                                    |
| `get(String path)`                                                                                     | `HttpRequestBuilder`        | `public`   | Prépare une requête HTTP `GET`.                                                               |
| `post(String path)`                                                                                    | `HttpRequestBuilder`        | `public`   | Prépare une requête HTTP `POST`.                                                              |
| `put(String path)`                                                                                     | `HttpRequestBuilder`        | `public`   | Prépare une requête HTTP `PUT`.                                                               |
| `delete(String path)`                                                                                  | `HttpRequestBuilder`        | `public`   | Prépare une requête HTTP `DELETE`.                                                            |
| `patch(String path)`                                                                                   | `HttpRequestBuilder`        | `public`   | Prépare une requête HTTP `PATCH`.                                                             |
| `head(String path)`                                                                                    | `HttpRequestBuilder`        | `public`   | Prépare une requête HTTP `HEAD`.                                                              |
| `options(String path)`                                                                                 | `HttpRequestBuilder`        | `public`   | Prépare une requête HTTP `OPTIONS`.                                                           |
| `normalizeBaseUrl(String url)`                                                                         | `String`                    | `private`  | Normalise l'URL de base en ajoutant `http://` si nécessaire et en retirant le slash terminal. |
| `HttpRequestBuilder(String method, String path)`                                                       | `void`                      | `public`   | Construit un builder pour une requête donnée.                                                 |
| `pathParam(String name, Object value)`                                                                 | `HttpRequestBuilder`        | `public`   | Déclare un paramètre de chemin.                                                               |
| `queryParam(String name, Object value)`                                                                | `HttpRequestBuilder`        | `public`   | Déclare un paramètre de query.                                                                |
| `header(String name, String value)`                                                                    | `HttpRequestBuilder`        | `public`   | Déclare un header HTTP.                                                                       |
| `body(Object data)`                                                                                    | `HttpRequestBuilder`        | `public`   | Déclare un corps de requête à sérialiser en JSON.                                             |
| `timeout(int milliseconds)`                                                                            | `HttpRequestBuilder`        | `public`   | Définit les timeouts de connexion et de lecture.                                              |
| `execute(Class<T> responseType)`                                                                       | `HttpResponse<T>`           | `public`   | Exécute la requête et désérialise la réponse dans le type demandé.                            |
| `execute()`                                                                                            | `HttpResponse<Void>`        | `public`   | Exécute la requête sans type de réponse attendu.                                              |
| `openConnection()`                                                                                     | `HttpURLConnection`         | `private`  | Prépare la connexion HTTP sous-jacente.                                                       |
| `buildUrl()`                                                                                           | `String`                    | `private`  | Construit l'URL finale à appeler.                                                             |
| `buildPathWithParams(String path, Map<String, String> params)`                                         | `String`                    | `private`  | Remplace les variables de chemin par leurs valeurs encodées.                                  |
| `buildQueryString(Map<String, String> params)`                                                         | `String`                    | `private`  | Construit la query string encodée en UTF-8.                                                   |
| `encodePathSegment(String segment)`                                                                    | `String`                    | `private`  | Encode un segment de chemin.                                                                  |
| `addHeaders(HttpURLConnection connection)`                                                             | `void`                      | `private`  | Ajoute les headers à la connexion.                                                            |
| `sendRequest(HttpURLConnection connection)`                                                            | `void`                      | `private`  | Sérialise et envoie le corps de requête si nécessaire.                                        |
| `parseResponse(HttpURLConnection connection, Class<T> responseType)`                                   | `HttpResponse<T>`           | `private`  | Lit la réponse HTTP et tente une désérialisation JSON.                                        |
| `readResponseBody(HttpURLConnection connection)`                                                       | `byte[]`                    | `private`  | Lit le corps brut de la réponse.                                                              |
| `normalizePath(String path)`                                                                           | `String`                    | `private`  | Normalise le chemin fourni au builder.                                                        |
| `HttpResponse(int status, String contentType, Map<String, List<String>> headers, byte[] body, T data)` | `void`                      | `public`   | Construit un conteneur de réponse HTTP.                                                       |
| `getStatus()`                                                                                          | `int`                       | `public`   | Retourne le code HTTP.                                                                        |
| `getContentType()`                                                                                     | `String`                    | `public`   | Retourne le content-type.                                                                     |
| `getHeaders()`                                                                                         | `Map<String, List<String>>` | `public`   | Retourne les headers de la réponse.                                                           |
| `getRawBody()`                                                                                         | `byte[]`                    | `public`   | Retourne le corps brut.                                                                       |
| `getData()`                                                                                            | `T`                         | `public`   | Retourne l'objet désérialisé si disponible.                                                   |
| `getBodyAsString()`                                                                                    | `String`                    | `public`   | Retourne le corps brut sous forme de texte UTF-8.                                             |
| `isSuccess()`                                                                                          | `boolean`                   | `public`   | Indique si le statut est dans la plage 2xx.                                                   |
| `isClientError()`                                                                                      | `boolean`                   | `public`   | Indique si le statut est dans la plage 4xx.                                                   |
| `isServerError()`                                                                                      | `boolean`                   | `public`   | Indique si le statut est supérieur ou égal à 500.                                             |
| `toString()`                                                                                           | `String`                    | `public`   | Fournit une représentation textuelle de la réponse.                                           |

## 💡 Exemple d'utilisation
```java
HttpClientService client = new HttpClientService("http://localhost:80");

// GET /users?name=Alice
HttpClientService.HttpResponse<String> listResponse = client
		.get("/users")
		.queryParam("name", "Alice")
		.execute(String.class);

// GET /users/{id}
HttpClientService.HttpResponse<String> userResponse = client
		.get("/users/{id}")
		.pathParam("id", 42)
		.execute(String.class);

// POST /users avec body JSON
User user = new User();
user.setId(42);
user.setName("Bob");

HttpClientService.HttpResponse<String> createResponse = client
		.post("/users")
		.body(user)
		.execute(String.class);

// HEAD /users/all
HttpClientService.HttpResponse<Void> headResponse = client
		.head("/users/all")
		.execute();
```

## ⚠️ Limitations / cas particuliers
- `HttpURLConnection` est utilisé directement ; il n'y a pas de gestion avancée des connexions persistantes ou du pooling.
- La désérialisation de réponse n'est tentée que si le `Content-Type` contient `application/json`.
- Les méthodes `POST`, `PUT` et `PATCH` sérialisent le body uniquement si un objet a été fourni.
- En cas de corps vide ou de réponse non JSON, `getData()` peut retourner `null`.
- Les paramètres de chemin et de query sont encodés en UTF-8 ; une valeur `null` est ignorée pour les query params et remplacée par une chaîne vide pour les path params.

## 📍 Notes internes MicroBean
- La classe suit une logique de client fluent compatible avec les routes exposées par `RestServerService`.
- Le choix de `HttpURLConnection` maintient une dépendance minimale et convient à un module de base.
- Le builder interne permet de composer la requête sans exposer les détails de connexion à l'appelant.
- `HttpResponse` conserve à la fois le corps brut et la version désérialisée pour faciliter le diagnostic et les assertions de test.
