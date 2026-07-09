package com.jasonpercus.microbean.client;

/*
 * Copyright (c) 2026 JasonPercus
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for more information.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonpercus.microbean.api.ResponseEntity;
import com.jasonpercus.microbean.infrastructure.async.JobHandle;
import com.jasonpercus.microbean.infrastructure.async.JobResponse;
import com.jasonpercus.microbean.infrastructure.async.JobStatus;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

/**
 * Client HTTP fluent permettant de consommer facilement les routes exposées par
 * {@code RestServerService} ou toute autre API REST JSON.
 *
 * <p>Cette classe s'appuie sur {@link HttpURLConnection} pour éviter toute
 * dépendance supplémentaire et fournit une API lisible pour :
 * <ul>
 *   <li>définir la méthode HTTP ({@code GET}, {@code POST}, {@code PUT},
 *       {@code DELETE}, {@code PATCH}, {@code HEAD}, {@code OPTIONS}) ;</li>
 *   <li>renseigner des paramètres de chemin, de query string et des headers ;</li>
 *   <li>envoyer automatiquement un corps JSON via Jackson ;</li>
 *   <li>lire la réponse sous forme typée via désérialisation JSON.</li>
 * </ul>
 *
 * <p>Exemples :</p>
 * <pre>{@code
 * HttpClientService client = new HttpClientService("http://localhost:80");
 *
 * // GET /users?name=Alice
 * HttpClientService.HttpResponse<String> response = client
 *         .get("/users")
 *         .queryParam("name", "Alice")
 *         .execute(String.class);
 *
 * // POST /users avec un body JSON
 * User user = new User();
 * user.setId(42);
 * user.setName("Bob");
 * HttpClientService.HttpResponse<String> created = client
 *         .post("/users")
 *         .body(user)
 *         .execute(String.class);
 * }</pre>
 *
 * <p>Comportements notables :</p>
 * <ul>
 *   <li>une URL de base vide ou {@code null} devient {@code http://localhost} ;</li>
 *   <li>les chemins sont normalisés pour commencer par {@code /} ;</li>
 *   <li>les paramètres de chemin sont encodés en UTF-8 ;</li>
 *   <li>les réponses JSON sont désérialisées uniquement si le content-type est
 *       compatible avec {@code application/json}.</li>
 * </ul>
 *
 * @author JasonPercus
 */
@SuppressWarnings("unused")
public class HttpClientService {

    /** Type MIME attendu pour les échanges JSON. */
    private static final String APPLICATION_JSON = "application/json";
    /** Verbe HTTP {@code GET}. */
    private static final String METHOD_GET = "GET";
    /** Verbe HTTP {@code POST}. */
    private static final String METHOD_POST = "POST";
    /** Verbe HTTP {@code PUT}. */
    private static final String METHOD_PUT = "PUT";
    /** Verbe HTTP {@code PATCH}. */
    private static final String METHOD_PATCH = "PATCH";
    /** Verbe HTTP {@code DELETE}. */
    private static final String METHOD_DELETE = "DELETE";
    /** Verbe HTTP {@code HEAD}. */
    private static final String METHOD_HEAD = "HEAD";
    /** Verbe HTTP {@code OPTIONS}. */
    private static final String METHOD_OPTIONS = "OPTIONS";
    /** Séparateur de chemin URL. */
    private static final String SEPARATOR = "/";
    /** Délai d'attente par défaut pour la connexion et la lecture. */
    private static final int TIMEOUT_MS = 10000;
    /** Pattern pour vérifier le format d'une URL. */
    private static final Pattern URL_PATTERN = Pattern.compile("^((?<protocole>\\w+)://)?(?<host>[^:]*)(:(?<port>\\d+))?(?<path>/.*)?$");

    /** URL de base du client, normalisée au format absolu sans slash terminal. */
    private final String baseUrl;

    /** URL de base du client WebSocket, normalisée au format absolu sans slash terminal. */
    private final String baseUrlWebSocket;

    /** Sérialiseur/désérialiseur JSON utilisé pour les corps de requête et de réponse. */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Crée un client HTTP basé sur une URL de base.
     *
     * <p>Si la valeur fournie est {@code null} ou vide, le client utilise
     * {@code http://localhost}. Si l'URL ne commence pas par un schéma,
     * {@code http://} est ajouté automatiquement.</p>
     *
     * <p>Exemples :</p>
     * <pre>{@code
     * new HttpClientService("http://localhost:80");
     * new HttpClientService("localhost:8080"); // devient http://localhost:8080
     * new HttpClientService(null);               // devient http://localhost
     * }</pre>
     *
     * @param baseUrl URL de base de l'API à consommer.
     */
    public HttpClientService(String baseUrl) {
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.baseUrlWebSocket = null;
    }

    /**
     * Crée un client HTTP basé sur une URL de base.
     *
     * <p>Si la valeur fournie est {@code null} ou vide, le client utilise
     * {@code http://localhost}. Si l'URL ne commence pas par un schéma,
     * {@code http://} est ajouté automatiquement.</p>
     *
     * <p>Exemples :</p>
     * <pre>{@code
     * new HttpClientService("http://localhost:80");
     * new HttpClientService("localhost:8080"); // devient http://localhost:8080
     * new HttpClientService(null);               // devient http://localhost
     * }</pre>
     *
     * @param baseUrl URL de base de l'API à consommer.
     * @param portWebSocket port de la WebSocket pour le retour de l'api
     */
    public HttpClientService(String baseUrl, int portWebSocket) {
        this.baseUrl = normalizeBaseUrl(baseUrl);

        Matcher matcher = URL_PATTERN.matcher(this.baseUrl);
        if (matcher.matches()) {
            this.baseUrlWebSocket = "ws://" + matcher.group("host") + ":" + portWebSocket;
        } else {
            this.baseUrlWebSocket = null;
        }
    }

    /**
     * Prépare une requête HTTP {@code GET}.
     *
     * @param path chemin relatif de la ressource, par exemple {@code /users}
     *             ou {@code /users/{id}}.
     * @return un builder fluide permettant de compléter la requête.
     */
    public HttpRequestBuilder get(String path) {
        return new HttpRequestBuilder(METHOD_GET, path);
    }

    /**
     * Prépare une requête HTTP {@code POST}.
     *
     * @param path chemin relatif de la ressource, par exemple {@code /users}
     *             ou {@code /users/{id}}.
     * @return un builder fluide permettant de compléter la requête.
     */
    public HttpRequestBuilder post(String path) {
        return new HttpRequestBuilder(METHOD_POST, path);
    }

    /**
     * Prépare une requête HTTP {@code PUT}.
     *
     * @param path chemin relatif de la ressource, par exemple {@code /users}
     *             ou {@code /users/{id}}.
     * @return un builder fluide permettant de compléter la requête.
     */
    public HttpRequestBuilder put(String path) {
        return new HttpRequestBuilder(METHOD_PUT, path);
    }

    /**
     * Prépare une requête HTTP {@code DELETE}.
     *
     * @param path chemin relatif de la ressource, par exemple {@code /users}
     *             ou {@code /users/{id}}.
     * @return un builder fluide permettant de compléter la requête.
     */
    public HttpRequestBuilder delete(String path) {
        return new HttpRequestBuilder(METHOD_DELETE, path);
    }

    /**
     * Prépare une requête HTTP {@code PATCH}.
     *
     * @param path chemin relatif de la ressource, par exemple {@code /users}
     *             ou {@code /users/{id}}.
     * @return un builder fluide permettant de compléter la requête.
     */
    public HttpRequestBuilder patch(String path) {
        return new HttpRequestBuilder(METHOD_PATCH, path);
    }

    /**
     * Prépare une requête HTTP {@code HEAD}.
     *
     * @param path chemin relatif de la ressource, par exemple {@code /users}
     *             ou {@code /users/{id}}.
     * @return un builder fluide permettant de compléter la requête.
     */
    public HttpRequestBuilder head(String path) {
        return new HttpRequestBuilder(METHOD_HEAD, path);
    }

    /**
     * Prépare une requête HTTP {@code OPTIONS}.
     *
     * @param path chemin relatif de la ressource, par exemple {@code /users}
     *             ou {@code /users/{id}}.
     * @return un builder fluide permettant de compléter la requête.
     */
    public HttpRequestBuilder options(String path) {
        return new HttpRequestBuilder(METHOD_OPTIONS, path);
    }

    /**
     * Normalise l'URL de base du client.
     *
     * <p>Règles appliquées :</p>
     * <ul>
     *   <li>si {@code null} ou vide : {@code http://localhost} ;</li>
     *   <li>si le schéma est absent : ajout de {@code http://} ;</li>
     *   <li>suppression du slash terminal éventuel.</li>
     * </ul>
     *
     * @param url URL saisie par l'appelant.
     * @return URL normalisée.
     */
    private String normalizeBaseUrl(String url) {
        
        if (url == null || url.isBlank())
            return "http://localhost";
        
        String normalized = url.startsWith("http") ? url : url.startsWith("ws") ? url : "http://" + url;
        
        if (normalized.endsWith(SEPARATOR))
            normalized = normalized.substring(0, normalized.length() - 1);
        
        return normalized;
    }

    /**
     * Builder fluide représentant une requête HTTP en cours de construction.
     *
     * <p>Cette classe permet d'ajouter progressivement :</p>
     * <ul>
     *   <li>des paramètres de chemin via {@link #pathParam(String, Object)} ;</li>
     *   <li>des paramètres de query via {@link #queryParam(String, Object)} ;</li>
     *   <li>des headers via {@link #header(String, String)} ;</li>
     *   <li>un corps JSON via {@link #body(Object)} ;</li>
     *   <li>des timeouts via {@link #timeout(int)}.</li>
     * </ul>
     *
     * <p>Exemple :</p>
     * <pre>{@code
     * HttpClientService client = new HttpClientService("http://localhost:80");
     * HttpClientService.HttpResponse<User> response = client
     *         .get("/users/{id}")
     *         .pathParam("id", 42)
     *         .queryParam("verbose", true)
     *         .header("Authorization", "Bearer token")
     *         .execute(User.class);
     * }</pre>
     */
    public class HttpRequestBuilder {

        /** Méthode HTTP à exécuter. */
        private final String method;
        /** Chemin normalisé de la ressource. */
        private final String path;
        /** Valeurs à injecter dans les variables de chemin. */
        private final Map<String, String> pathParams = new HashMap<>();
        /** Paramètres de query à ajouter à l'URL. */
        private final Map<String, String> queryParams = new LinkedHashMap<>();
        /** Headers HTTP supplémentaires à envoyer. */
        private final Map<String, String> headers = new LinkedHashMap<>();
        /** Corps de la requête à sérialiser en JSON, si présent. */
        private Object body;
        /** Délai de connexion en millisecondes. */
        private int connectTimeout = TIMEOUT_MS;
        /** Délai de lecture en millisecondes. */
        private int readTimeout = TIMEOUT_MS;

        /**
         * Construit un nouveau builder de requête.
         *
         * @param method méthode HTTP à exécuter.
         * @param path chemin de la ressource.
         */
        public HttpRequestBuilder(String method, String path) {
            this.method = method;
            this.path = normalizePath(path);
        }

        /**
         * Remplace une variable de chemin au format {@code {name}}.
         *
         * <p>Exemple : {@code /users/{id}} avec {@code pathParam("id", 42)}
         * devient {@code /users/42}.</p>
         *
         * @param name nom de la variable de chemin.
         * @param value valeur à injecter.
         * @return le builder courant.
         */
        public HttpRequestBuilder pathParam(String name, Object value) {
            pathParams.put(name, value == null ? "" : value.toString());
            return this;
        }

        /**
         * Ajoute un paramètre de query à l'URL.
         *
         * <p>Exemple : {@code queryParam("name", "Alice")} produit
         * {@code ?name=Alice} après encodage.</p>
         *
         * @param name nom du paramètre.
         * @param value valeur du paramètre.
         * @return le builder courant.
         */
        public HttpRequestBuilder queryParam(String name, Object value) {
            if (value != null)
                queryParams.put(name, value.toString());
            
            return this;
        }

        /**
         * Ajoute ou remplace un header HTTP.
         *
         * @param name nom du header.
         * @param value valeur du header.
         * @return le builder courant.
         */
        public HttpRequestBuilder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        /**
         * Définit le corps de la requête.
         *
         * <p>Si la méthode HTTP supporte un body ({@code POST}, {@code PUT},
         * {@code PATCH}), l'objet fourni est sérialisé en JSON avec Jackson.</p>
         *
         * <p>Exemple :</p>
         * <pre>{@code
         * client.post("/users").body(user).execute(User.class);
         * }</pre>
         *
         * @param data objet à sérialiser en JSON.
         * @return le builder courant.
         */
        public HttpRequestBuilder body(Object data) {
            this.body = data;
            return this;
        }

        /**
         * Définit le timeout de connexion et de lecture.
         *
         * @param milliseconds durée du timeout en millisecondes.
         * @return le builder courant.
         */
        public HttpRequestBuilder timeout(int milliseconds) {
            this.connectTimeout = milliseconds;
            this.readTimeout = milliseconds;
            return this;
        }

        /**
         * Exécute la requête et tente de désérialiser le corps JSON dans le type demandé.
         *
         * <p>Si la réponse n'est pas JSON, le corps brut reste accessible via
         * {@link HttpResponse#getBodyAsString()} et {@link HttpResponse#getRawBody()}.</p>
         *
         * @param responseType type cible pour la désérialisation JSON.
         * @param <T> type de retour attendu.
         * @return réponse HTTP enrichie.
         * @throws IOException si la connexion ou la lecture réseau échoue.
         */
        public <T> HttpResponse<T> execute(Class<T> responseType) throws IOException {
            
            HttpURLConnection connection = openConnection();
            
            try {
                sendRequest(connection);
                return parseResponse(connection, responseType);
            } finally {
                connection.disconnect();
            }
        }

        /**
         * Exécute la requête sans attente de type de réponse particulier.
         *
         * @return réponse HTTP enrichie.
         * @throws IOException si la connexion ou la lecture réseau échoue.
         */
        public HttpResponse<Void> execute() throws IOException {
            return execute(Void.class);
        }

        /**
         * Exécute la requête de manière asynchrone et appelle le callback à la fin.
         *
         * @param responseType type cible pour la désérialisation JSON.
         * @param onSuccess callback appelé en cas de succès.
         * @param onError callback appelé en cas d'erreur.
         * @return un Future représentant l'exécution de la requête.
         * @param <T> type de retour attendu.
         * @throws IOException si la connexion ou la lecture réseau échoue.
         */
        public <T> HttpResponse<JobHandle> executeAsync(Class<T> responseType, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) throws IOException {

            HttpResponse<JobHandle> response = execute(JobHandle.class);

            JobHandle handle = response.getData();

            if (handle == null)
                return response;

            String wsUrl = handle.wsUrl();

            if (wsUrl != null)
                connectWebSocket(baseUrlWebSocket + wsUrl, responseType, onSuccess, onError);

            return response;
        }

        /**
         * Se connecte à un WebSocket pour recevoir les messages de progression d'une requête asynchrone.
         *
         * @param wsUrl URL du WebSocket.
         * @param onSuccess callback appelé en cas de succès.
         * @param onError callback appelé en cas d'erreur.
         * @param <T> type de retour attendu.
         */
        private <T> void connectWebSocket(String wsUrl, Class<T> type, Consumer<ResponseEntity<T>> onSuccess, Consumer<Throwable> onError) {

            try {
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();

                container.connectToServer(new Endpoint() {

                    @Override
                    public void onOpen(Session session, EndpointConfig config) {

                        session.addMessageHandler(String.class, msg -> {
                            try {
                                ObjectMapper mapper = new ObjectMapper();

                                JobResponse response = mapper.readValue(msg, JobResponse.class);

                                if (response.status() == JobStatus.DONE) {
                                    T result = mapper.convertValue(response.result(), type);
                                    onSuccess.accept(new ResponseEntity<T>()
                                            .ok()
                                            .setBody(result));
                                    session.close();
                                }

                                if (response.status() == JobStatus.FAILED) {
                                    onError.accept(new RuntimeException("Job failed"));
                                    session.close();
                                }

                            } catch (Exception e) {
                                onError.accept(e);
                            }
                        });
                    }

                }, URI.create(wsUrl));

            } catch (Exception e) {
                onError.accept(e);
            }
        }

        /**
         * Ouvre et configure la connexion HTTP sous-jacente.
         *
         * @return connexion prête à être utilisée.
         * @throws IOException en cas d'erreur réseau ou d'URL invalide.
         */
        private HttpURLConnection openConnection() throws IOException {
            
            String url = buildUrl();
            
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            setRequestMethodWithFallback(connection, method);
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setDoInput(true);

            if (METHOD_POST.equals(method) || METHOD_PUT.equals(method) || METHOD_PATCH.equals(method)) {
                connection.setDoOutput(true);
            }

            addHeaders(connection);
            
            return connection;
        }

        /**
         * Définit la méthode HTTP en gérant le cas particulier de PATCH sur
         * certaines implémentations JDK de {@link HttpURLConnection}.
         *
         * <p>Si PATCH n'est pas supporté nativement, un fallback est appliqué
         * via POST + header {@code X-HTTP-Method-Override: PATCH}.</p>
         */
        private void setRequestMethodWithFallback(HttpURLConnection connection, String requestMethod) throws ProtocolException {
            try {
                connection.setRequestMethod(requestMethod);
            } catch (ProtocolException exception) {
                if (!METHOD_PATCH.equals(requestMethod)) {
                    throw exception;
                }

                // Fallback pour JDK ne supportant pas PATCH via HttpURLConnection.
                connection.setRequestProperty("X-HTTP-Method-Override", METHOD_PATCH);
                connection.setRequestMethod(METHOD_POST);
            }
        }

        /**
         * Construit l'URL finale à appeler en combinant base URL, chemin,
         * paramètres de chemin et query string.
         *
         * @return URL complète.
         */
        private String buildUrl() {
            
            String fullUrl = baseUrl + buildPathWithParams(path, pathParams);

            if (!queryParams.isEmpty())
                fullUrl += "?" + buildQueryString(queryParams);

            return fullUrl;
        }

        /**
         * Remplace les variables de chemin par leurs valeurs encodées.
         *
         * @param path chemin source.
         * @param params variables à injecter.
         * @return chemin final.
         */
        private String buildPathWithParams(String path, Map<String, String> params) {
            
            String result = path;
            
            for (Map.Entry<String, String> entry : params.entrySet())
                result = result.replace("{" + entry.getKey() + "}", encodePathSegment(entry.getValue()));
            
            return result;
        }

        /**
         * Construit la chaîne de query string encodée en UTF-8.
         *
         * @param params paramètres à sérialiser.
         * @return query string sans le {@code ?} initial.
         */
        private String buildQueryString(Map<String, String> params) {
            
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            
            for (Map.Entry<String, String> entry : params.entrySet()) {
                
                if (!first)
                    sb.append("&");
                
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                  .append("=")
                  .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                
                first = false;
            }
            
            return sb.toString();
        }

        /**
         * Encode un segment de chemin pour être injecté dans une URL.
         *
         * @param segment segment à encoder.
         * @return segment encodé.
         */
        private String encodePathSegment(String segment) {
            return URLEncoder.encode(segment, StandardCharsets.UTF_8);
        }

        /**
         * Ajoute les headers au niveau de la connexion HTTP.
         *
         * <p>Si un body est présent sur une méthode compatible, le header
         * {@code Content-Type: application/json} est posé automatiquement.</p>
         *
         * @param connection connexion HTTP.
         */
        private void addHeaders(HttpURLConnection connection) {
            
            headers.forEach(connection::setRequestProperty);

            if (body != null && (METHOD_POST.equals(method) || METHOD_PUT.equals(method) || METHOD_PATCH.equals(method)))
                connection.setRequestProperty("Content-Type", APPLICATION_JSON);
        }

        /**
         * Sérialise le corps éventuel et l'envoie au serveur.
         *
         * @param connection connexion HTTP active.
         * @throws IOException si l'écriture du body échoue.
         */
        private void sendRequest(HttpURLConnection connection) throws IOException {
            
            if (body != null && (METHOD_POST.equals(method) || METHOD_PUT.equals(method) || METHOD_PATCH.equals(method))) {
                
                byte[] payload = objectMapper.writeValueAsBytes(body);
                
                connection.setFixedLengthStreamingMode(payload.length);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(payload);
                }
            }
        }

        /**
         * Lit et transforme la réponse HTTP.
         *
         * @param connection connexion HTTP déjà exécutée.
         * @param responseType type cible pour la désérialisation JSON.
         * @param <T> type générique du résultat.
         * @return réponse HTTP enrichie.
         * @throws IOException si la récupération du code ou des flux échoue.
         */
        private <T> HttpResponse<T> parseResponse(HttpURLConnection connection, Class<T> responseType) throws IOException {
            
            int status = connection.getResponseCode();
            String contentType = connection.getContentType();
            byte[] body = readResponseBody(connection);

            T data = null;
            if (body.length > 0 && contentType != null && contentType.toLowerCase().contains(APPLICATION_JSON)) {
                try {
                    data = objectMapper.readValue(body, responseType);
                } catch (IOException exception) {
                    // Corps invalide, data reste null
                }
            }

            return new HttpResponse<>(status, contentType, connection.getHeaderFields(), body, data);
        }

        /**
         * Lit le corps de réponse en priorisant le flux d'erreur si le serveur
         * a renvoyé un code non-2xx.
         *
         * @param connection connexion HTTP.
         * @return octets du corps, ou tableau vide si absent.
         */
        private byte[] readResponseBody(HttpURLConnection connection) {
            try (InputStream is = connection.getErrorStream() != null ? connection.getErrorStream() : connection.getInputStream()) {
                
                if (is == null)
                    return new byte[0];
                
                return is.readAllBytes();
            } catch (IOException exception) {
                return new byte[0];
            }
        }

        /**
         * Normalise un chemin relatif pour garantir un slash initial et retirer
         * un éventuel slash terminal.
         *
         * @param path chemin saisi par l'appelant.
         * @return chemin normalisé.
         */
        private String normalizePath(String path) {
            
            if (path == null || path.isBlank())
                return SEPARATOR;
                
            String normalized = path.startsWith(SEPARATOR) ? path : SEPARATOR + path;
            
            if (normalized.length() > 1 && normalized.endsWith(SEPARATOR))
                normalized = normalized.substring(0, normalized.length() - 1);
                
            return normalized;
        }
    }

    /**
     * Représente une réponse HTTP retournée par {@link HttpClientService}.
     *
     * <p>La réponse donne accès :</p>
     * <ul>
     *   <li>au code HTTP ;</li>
     *   <li>au content-type ;</li>
     *   <li>aux headers ;</li>
     *   <li>au corps brut ;</li>
     *   <li>à une version désérialisée si un type a été demandé.</li>
     * </ul>
     *
     * <p>Exemple :</p>
     * <pre>{@code
     * HttpClientService.HttpResponse<User> response = client
     *         .get("/users/42")
     *         .execute(User.class);
     *
     * if (response.isSuccess()) {
     *     User user = response.getData();
     * }
     * }</pre>
     *
     * @param <T> type de l'objet désérialisé.
     */
    @SuppressWarnings("unused")
    public static class HttpResponse<T> {

        /** Code HTTP retourné par le serveur. */
        private final int status;
        /** Content-Type renvoyé par le serveur. */
        private final String contentType;
        /** Ensemble des headers HTTP de la réponse. */
        private final Map<String, java.util.List<String>> headers;
        /** Corps brut de la réponse. */
        private final byte[] body;
        /** Corps désérialisé dans le type attendu, si possible. */
        private final T data;

        /**
         * Construit une réponse HTTP enrichie.
         *
         * @param status code HTTP.
         * @param contentType content-type renvoyé.
         * @param headers headers HTTP de réponse.
         * @param body corps brut de réponse.
         * @param data corps désérialisé.
         */
        public HttpResponse(int status, String contentType, Map<String, java.util.List<String>> headers, byte[] body, T data) {
            this.status = status;
            this.contentType = contentType;
            this.headers = headers;
            this.body = body;
            this.data = data;
        }

        /** @return code HTTP de la réponse. */
        public int getStatus() {
            return status;
        }

        /** @return content-type de la réponse, ou {@code null} s'il est absent. */
        public String getContentType() {
            return contentType;
        }

        /** @return headers HTTP de la réponse. */
        public Map<String, java.util.List<String>> getHeaders() {
            return headers;
        }

        /** @return corps brut de la réponse en octets. */
        public byte[] getRawBody() {
            return body;
        }

        /** @return objet désérialisé, si disponible. */
        public T getData() {
            return data;
        }

        /**
         * Retourne le corps brut sous forme de chaîne UTF-8.
         *
         * @return corps textuel, éventuellement vide.
         */
        public String getBodyAsString() {
            return new String(body, StandardCharsets.UTF_8);
        }

        /** @return {@code true} si le statut est dans l'intervalle 2xx. */
        public boolean isSuccess() {
            return status >= 200 && status < 300;
        }

        /** @return {@code true} si le statut est dans l'intervalle 4xx. */
        public boolean isClientError() {
            return status >= 400 && status < 500;
        }

        /** @return {@code true} si le statut est égal ou supérieur à 500. */
        public boolean isServerError() {
            return status >= 500;
        }

        /**
         * Représentation textuelle utile pour le débogage.
         *
         * @return chaîne descriptive de la réponse.
         */
        @Override
        public String toString() {
            return "HttpResponse{" +
                    "status=" + status +
                    ", contentType='" + contentType + '\'' +
                    ", bodyLength=" + body.length +
                    ", data=" + data +
                    '}';
        }
    }
}
