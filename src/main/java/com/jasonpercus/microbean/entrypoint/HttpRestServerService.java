package com.jasonpercus.microbean.entrypoint;

/*
 * Copyright (c) 2026 JasonPercus
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for more information.
 */

import static com.jasonpercus.microbean.infrastructure.helpers.LogHelper.error;
import static com.jasonpercus.microbean.infrastructure.helpers.LogHelper.trace;
import static com.jasonpercus.microbean.infrastructure.helpers.LogHelper.warn;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonpercus.microbean.MicroBean;
import com.jasonpercus.microbean.api.ApplicationEntryPoint;
import com.jasonpercus.microbean.api.AsyncJob;
import com.jasonpercus.microbean.api.Body;
import com.jasonpercus.microbean.api.Controller;
import com.jasonpercus.microbean.api.EntryPointService;
import com.jasonpercus.microbean.api.Header;
import com.jasonpercus.microbean.api.LifecycleEntryPoint;
import com.jasonpercus.microbean.api.Path;
import com.jasonpercus.microbean.api.Query;
import com.jasonpercus.microbean.api.ResponseEntity;
import com.jasonpercus.microbean.api.method.Delete;
import com.jasonpercus.microbean.api.method.Get;
import com.jasonpercus.microbean.api.method.Head;
import com.jasonpercus.microbean.api.method.Options;
import com.jasonpercus.microbean.api.method.Patch;
import com.jasonpercus.microbean.api.method.Post;
import com.jasonpercus.microbean.api.method.Put;
import com.jasonpercus.microbean.infrastructure.ParameterType;
import com.jasonpercus.microbean.infrastructure.RouteDefinition;
import com.jasonpercus.microbean.infrastructure.RouteParam;
import com.jasonpercus.microbean.infrastructure.async.AsyncJobManager;
import com.jasonpercus.microbean.infrastructure.async.JobWebSocketEndpoint;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.glassfish.tyrus.server.Server;

/**
 * Point d'entrée REST HTTP de MicroBean.
 *
 * <p>Cette classe démarre un serveur HTTP embarqué, scanne les controllers
 * annotés, construit une table de routage et traite les requêtes entrantes.
 * Elle prend en charge :</p>
 * <ul>
 *   <li>la résolution de route par méthode HTTP + chemin ;</li>
 *   <li>la résolution des paramètres (`@Path`, `@Query`, `@Header`, `@Body`, AUTO) ;</li>
 *   <li>la conversion de types primitifs et wrappers ;</li>
 *   <li>la sérialisation/désérialisation JSON via Jackson ;</li>
 *   <li>la gestion d'erreurs HTTP standardisées (400/404/405/415/422/500).</li>
 * </ul>
 *
 * <p>Le service est déclaré `LONG_RUNNING` pour rester actif tant que
 * l'application MicroBean est en exécution.</p>
 */
@EntryPointService(lifecycle = LifecycleEntryPoint.LONG_RUNNING)
public class HttpRestServerService implements ApplicationEntryPoint {

    /** Type MIME JSON utilisé en entrée/sortie HTTP. */
    private static final String APPLICATION_JSON = "application/json";
    /** Séparateur de chemin d'URL. */
    private static final String SEPARATOR = "/";
    /** Verbe HTTP OPTIONS. */
    private static final String METHOD_OPTIONS = "OPTIONS";
    /** Verbe HTTP HEAD. */
    private static final String METHOD_HEAD = "HEAD";
    /** Verbe HTTP GET. */
    private static final String METHOD_GET = "GET";
    /** Verbe HTTP POST. */
    private static final String METHOD_POST = "POST";
    /** Verbe HTTP PUT. */
    private static final String METHOD_PUT = "PUT";
    /** Verbe HTTP DELETE. */
    private static final String METHOD_DELETE = "DELETE";
    /** Verbe HTTP PATCH. */
    private static final String METHOD_PATCH = "PATCH";
    /** Nom du header Content-Type. */
    private static final String CONTENT_TYPE = "Content-Type";

    /** Période en secondes avant de nettoyer les résultats non récupérés */
    private static final AtomicInteger TIME_SEC_CLEAN_PERIOD = new AtomicInteger(30);

    /** Manager des jobs asynchrones pour le support WebSocket. */
    public static final AsyncJobManager ASYNC_JOB_MANAGER = new AsyncJobManager(TIME_SEC_CLEAN_PERIOD, Executors.newFixedThreadPool(10));

    /** Code HTTP 204. */
    public static final int NO_CONTENT = 204;
    /** Code HTTP 400. */
    public static final int BAD_REQUEST = 400;
    /** Code HTTP 404. */
    public static final int NOT_FOUND = 404;
    /** Code HTTP 405. */
    public static final int METHOD_NOT_ALLOWED = 405;
    /** Code HTTP 415. */
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    /** Code HTTP 422. */
    public static final int UNPROCESSABLE_ENTITY = 422;
    /** Code HTTP 500. */
    public static final int INTERNAL_SERVER_ERROR = 500;

    /** Mapper JSON utilisé pour sérialiser/désérialiser les corps HTTP. */
    private final ObjectMapper objectMapper = new ObjectMapper();
    /** Table de routage en mémoire, triée par score de spécificité. */
    private final List<RouteDefinition> routes = new ArrayList<>();

    /** Instance de serveur HTTP embarqué. */
    private HttpServer server;

    /** Détermine si le serveur WebSocket pour les jobs asynchrones est utilisé. */
    private boolean useWebSocket = false;

    /**
     * Initialise et démarre le serveur REST.
     *
     * <p>Ordre d'exécution :</p>
     * <ol>
     *   <li>résolution du port (env: {@code MICROBEAN_HTTP_PORT}) ;</li>
     *   <li>scan des controllers ;</li>
     *   <li>création du serveur et enregistrement du handler global ;</li>
     *   <li>démarrage du serveur HTTP.</li>
     * </ol>
     *
     * @param strings arguments d'entrée (non utilisés actuellement).
     * @throws Exception en cas d'erreur d'initialisation ou de démarrage.
     */
    @Override
    public void main(String[] strings) throws Exception {

        int port = getPort();
        int portWs = getPortWs();
        loadTimeSecCleanPeriod();

        scanControllers();
        buildServer(port);

        if (useWebSocket && portWs > 0) {
            JobWebSocketEndpoint.setJobManager(ASYNC_JOB_MANAGER);
            Server wsServer = new Server("localhost", portWs, "/", null, JobWebSocketEndpoint.class);
            wsServer.start();
        }

        server.start();

        trace("⚙️ Rest server started on port: " + port);
    }

    /**
     * Lit le port HTTP depuis l'environnement.
     *
     * <p>Utilise {@code MICROBEAN_HTTP_PORT} si défini, sinon retourne 80.</p>
     *
     * @return port HTTP à utiliser.
     */
    private static int getPort() {
        int port = 80;

        String microbeanHttpPort = System.getenv("MICROBEAN_HTTP_PORT");
        if (microbeanHttpPort != null) {
            try {
                port = Integer.parseInt(microbeanHttpPort);
            } catch (NumberFormatException e) {
                error("❌ Invalid port number in MICROBEAN_HTTP_PORT environment variable: " + microbeanHttpPort, e);
                throw e;
            }
        }
        return port;
    }

    /**
     * Lit le port WS depuis l'environnement.
     *
     * <p>Utilise {@code MICROBEAN_WS_PORT} si défini, sinon retourne 8080.</p>
     *
     * @return port HTTP à utiliser.
     */
    private static int getPortWs() {
        int port = 8080;

        String microbeanWsPort = System.getenv("MICROBEAN_WS_PORT");
        if (microbeanWsPort != null) {
            try {
                port = Integer.parseInt(microbeanWsPort);
            } catch (NumberFormatException e) {
                error("❌ Invalid port number in MICROBEAN_WS_PORT environment variable: " + microbeanWsPort, e);
                throw e;
            }
        }
        return port;
    }

    /**
     * Charge la période en secondes des nettoyages
     *
     * <p>Utilise {@code MICROBEAN_TIME_SEC_CLEAN_PERIOD} si défini.</p>
     */
    private static void loadTimeSecCleanPeriod() {
        String sec = System.getenv("MICROBEAN_TIME_SEC_CLEAN_PERIOD");

        int period;

        if (sec != null) {
            try {
                period = Integer.parseInt(sec);

                if (period >= 5) {
                    TIME_SEC_CLEAN_PERIOD.set(period);
                }
            } catch (NumberFormatException e) {
                error("❌ Invalid period number in MICROBEAN_TIME_SEC_CLEAN_PERIOD environment variable: " + sec, e);
                throw e;
            }
        }
    }

    /**
     * Scanne les beans `@Controller` et construit la table de routage.
     *
     * <p>Les routes sont triées par score décroissant (plus spécifique d'abord)
     * puis validées pour détecter les doublons exacts (méthode + chemin).</p>
     */
    private void scanControllers() {

        Collection<Object> controllers = MicroBean.getContext().getBeansByAnnotation(Controller.class);

        for (Object controller : controllers) {

            Class<?> clazz = controller.getClass();

            for (Method method : clazz.getDeclaredMethods())
                parseMethod(controller, method);
        }

        routes.sort(Comparator.comparingInt(RouteDefinition::getScore).reversed());

        validateRouteUniqueness();
    }

    /**
     * Tente de convertir une méthode Java en définition de route HTTP.
     *
     * @param controller instance de controller.
     * @param method méthode candidate.
     */
    private void parseMethod(Object controller, Method method) {

        if (!method.getReturnType().equals(ResponseEntity.class)) {

            if (method.isAnnotationPresent(Get.class) ||
                method.isAnnotationPresent(Post.class) ||
                method.isAnnotationPresent(Put.class) ||
                method.isAnnotationPresent(Delete.class) ||
                method.isAnnotationPresent(Patch.class) ||
                method.isAnnotationPresent(Options.class) ||
                method.isAnnotationPresent(Head.class)) {
                warn("Method " + method.getName() + " in controller " + controller.getClass().getName()
                        + " must return ResponseEntity<R> to be exposed as a REST endpoint.");
            }
            return;
        }

        useWebSocket |= method.isAnnotationPresent(AsyncJob.class);

        HttpBinding binding = resolveHttpBinding(method);
        if (binding == null)
            return;

        String fullPath = normalizePath(resolveBasePath(controller) + binding.path());

        RouteDefinition route = new RouteDefinition(
                binding.httpMethod(),
                fullPath,
                controller,
                method,
                analyzeParameters(method));

        routes.add(route);
    }

    /**
     * Résout le chemin de base d'un controller.
     *
     * @param controller instance controller.
     * @return chemin de base ou chaîne vide si absent.
     */
    private String resolveBasePath(Object controller) {

        Controller annotation = controller.getClass().getAnnotation(Controller.class);

        return Optional.ofNullable(annotation)
                .map(Controller::value)
                .orElse("");
    }

    /**
     * Analyse la signature d'une méthode pour construire ses paramètres de route.
     *
     * @param method méthode cible.
     * @return liste ordonnée des paramètres attendus.
     */
    private List<RouteParam> analyzeParameters(Method method) {

        List<RouteParam> params = new ArrayList<>();

        for (Parameter parameter : method.getParameters()) {

            if (parameter.isAnnotationPresent(Path.class))
                createPathRouteParam(parameter, params);

            else if (parameter.isAnnotationPresent(Query.class))
                createQueryRouteParam(parameter, params);

            else if (parameter.isAnnotationPresent(Header.class))
                createHeaderRouteParam(parameter, params);

            else if (parameter.isAnnotationPresent(Body.class))
                createBodyRouteParam(parameter, params);

            else
                createAutoRouteParam(parameter, params);
        }

        return params;
    }

    /**
     * Crée un paramètre de route pour un paramètre annoté `@Path`.
     *
     * @param parameter paramètre Java.
     * @param params liste des paramètres de route à compléter.
     */
    private void createPathRouteParam(Parameter parameter, List<RouteParam> params) {
        Path path = parameter.getAnnotation(Path.class);
        params.add(new RouteParam(ParameterType.PATH, path.value(), parameter.getType()));
    }

    /**
     * Crée un paramètre de route pour un paramètre annoté `@Query`.
     *
     * @param parameter paramètre Java.
     * @param params liste des paramètres de route à compléter.
     */
    private void createQueryRouteParam(Parameter parameter, List<RouteParam> params) {
        Query query = parameter.getAnnotation(Query.class);
        params.add(new RouteParam(ParameterType.QUERY, query.value(), parameter.getType()));
    }

    /**
     * Crée un paramètre de route pour un paramètre annoté `@Header`.
     *
     * @param parameter paramètre Java.
     * @param params liste des paramètres de route à compléter.
     */
    private void createHeaderRouteParam(Parameter parameter, List<RouteParam> params) {
        Header header = parameter.getAnnotation(Header.class);
        params.add(new RouteParam(ParameterType.HEADER, header.value(), parameter.getType()));
    }

    /**
     * Crée un paramètre de route pour un paramètre annoté `@Body`.
     *
     * @param parameter paramètre Java.
     * @param params liste des paramètres de route à compléter.
     */
    private void createBodyRouteParam(Parameter parameter, List<RouteParam> params) {
        params.add(new RouteParam(ParameterType.BODY, null, parameter.getType()));
    }

    /**
     * Crée un paramètre de route pour un paramètre sans annotation spécifique.
     *
     * <p>Le type du paramètre est utilisé pour déterminer son rôle :</p>
     * <ul>
     *   <li>{@code HttpRequest} : injection de la requête HTTP complète ;</li>
     *   <li>{@code HttpResponse} : injection de la réponse HTTP complète ;</li>
     *   <li>autres types : injection automatique depuis le corps JSON.</li>
     * </ul>
     *
     * @param parameter paramètre Java.
     * @param params liste des paramètres de route à compléter.
     */
    private void createAutoRouteParam(Parameter parameter, List<RouteParam> params) {
        params.add(new RouteParam(ParameterType.AUTO, parameter.getName(), parameter.getType()));
    }

    /**
     * Construit le serveur HTTP et enregistre le handler racine.
     *
     * @param port port d'écoute.
     * @throws IOException si la création du serveur échoue.
     */
    private void buildServer(int port) throws IOException {

        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext(SEPARATOR, exchange -> {

            try {
                handle(exchange);
            } catch (Exception e) {
                error("❌ Error handling request: " + e.getMessage(), e);
                if (!exchange.getResponseHeaders().containsKey(CONTENT_TYPE))
                    writeError(exchange, INTERNAL_SERVER_ERROR, "internal_error", "Unexpected internal error");
            }
        });
    }

    /**
     * Point d'entrée de traitement d'une requête HTTP.
     *
     * <p>Pipeline :</p>
     * <ol>
     *   <li>normalisation méthode/chemin ;</li>
     *   <li>résolution de route ;</li>
     *   <li>construction des arguments ;</li>
     *   <li>invocation controller ;</li>
     *   <li>écriture de la réponse ou d'une erreur HTTP.</li>
     * </ol>
     *
     * @param exchange échange HTTP courant.
     * @throws Exception en cas d'erreur non transformée en `HttpErrorException`.
     */
    private void handle(HttpExchange exchange) throws Exception {

        String method = exchange.getRequestMethod();
        String path = normalizePath(exchange.getRequestURI().getPath());

        try {
            RouteResolution resolution = resolveRoute(method, path);

            if (resolution.route() == null) {
                if (METHOD_OPTIONS.equals(method) && !resolution.allowedMethods().isEmpty()) {
                    writeOptionsResponse(exchange, resolution.allowedMethods());
                    return;
                }

                if (resolution.allowedMethods().isEmpty()) {
                    exchange.sendResponseHeaders(NOT_FOUND, -1);
                    return;
                }

                exchange.getResponseHeaders().add("Allow", String.join(", ", resolution.allowedMethods()));
                exchange.sendResponseHeaders(METHOD_NOT_ALLOWED, -1);
                return;
            }

            RouteDefinition route = resolution.route();
            trace("[" + method + "] " + path + " ➠ " + route.getMethod().getName());

            Object[] args = buildArguments(route, exchange, path);
            ResponseEntity<?> result = invokeController(route, args);

            writeResponse(exchange, result, METHOD_HEAD.equals(method));
        } catch (HttpErrorException exception) {
            writeError(exchange, exception.status(), exception.code(), exception.getMessage());
        }
    }

    /**
     * Résout la meilleure route candidate pour une méthode + chemin.
     *
     * <p>Gère également le fallback implicite `HEAD` sur route `GET`.</p>
     *
     * @param method méthode HTTP entrante.
     * @param path chemin normalisé entrant.
     * @return résultat contenant route trouvée et méthodes autorisées.
     */
    private RouteResolution resolveRoute(String method, String path) {

        Set<String> allowedMethods = new LinkedHashSet<>();

        for (RouteDefinition route : routes) {
            if (!route.matchesPath(path))
                continue;

            allowedMethods.add(route.getHttpMethod());

            if (route.getHttpMethod().equals(method))
                return new RouteResolution(route, withImplicitMethods(allowedMethods));

            if (METHOD_HEAD.equals(method) && METHOD_GET.equals(route.getHttpMethod()))
                return new RouteResolution(route, withImplicitMethods(allowedMethods));
        }

        return new RouteResolution(null, withImplicitMethods(allowedMethods));
    }

    /**
     * Complète la liste des méthodes autorisées implicites.
     *
     * <p>Règles :</p>
     * <ul>
     *   <li>si `GET` est autorisé, `HEAD` l'est aussi ;</li>
     *   <li>`OPTIONS` est toujours ajouté.</li>
     * </ul>
     *
     * @param allowedMethods méthodes déjà détectées.
     * @return set enrichi des méthodes autorisées.
     */
    private Set<String> withImplicitMethods(Set<String> allowedMethods) {

        Set<String> allMethods = new LinkedHashSet<>(allowedMethods);

        if (allMethods.contains(METHOD_GET))
            allMethods.add(METHOD_HEAD);

        allMethods.add(METHOD_OPTIONS);
        return allMethods;
    }

    /**
     * Construit les arguments Java à injecter lors de l'appel controller.
     *
     * @param route route résolue.
     * @param exchange échange HTTP courant.
     * @param requestPath chemin de requête normalisé.
     * @return tableau d'arguments ordonné selon la signature de méthode.
     * @throws Exception en cas d'erreur de conversion/résolution.
     */
    private Object[] buildArguments(RouteDefinition route, HttpExchange exchange, String requestPath) throws Exception {

        byte[] bodyBytes = null;

        Map<String, List<String>> queryValues = parseQuery(exchange.getRequestURI().getRawQuery());

        Object[] args = new Object[route.getParams().size()];

        for (int i = 0; i < route.getParams().size(); i++) {

            RouteParam param = route.getParams().get(i);

            switch (param.type()) {

                case PATH -> args[i] = convert(
                        decode(route.extractPathParam(param.name(), requestPath)),
                        param.clazz(),
                        param.name());

                case QUERY -> args[i] = convert(
                        firstOrNull(queryValues.get(param.name())),
                        param.clazz(),
                        param.name());

                case HEADER -> args[i] = convert(
                        exchange.getRequestHeaders().getFirst(param.name()),
                        param.clazz(),
                        param.name());

                case BODY -> {
                    bodyBytes = ensureBodyCached(bodyBytes, exchange);
                    args[i] = deserializeBody(bodyBytes, exchange, param.clazz());
                }

                case AUTO -> {
                    bodyBytes = ensureBodyCached(bodyBytes, exchange);
                    args[i] = autoResolve(queryValues, bodyBytes, exchange, param.clazz(), param.name());
                }
            }
        }

        return args;
    }

    /**
     * Decode un paramètre de chemin encodé en URL.
     *
     * @param value valeur encodée.
     * @return valeur décodée.
     */
    private String decode(String value) {

        if (value == null)
            return null;

        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private byte[] ensureBodyCached(byte[] currentBytes, HttpExchange exchange) throws IOException {

        if (currentBytes != null)
            return currentBytes;

        try (InputStream body = exchange.getRequestBody()) {

            if (body == null)
                return new byte[0];

            return body.readAllBytes();
        }
    }

    /**
     * Désérialise le corps JSON vers une classe cible.
     *
     * @param bodyBytes contenu brut du corps HTTP.
     * @param exchange échange HTTP courant.
     * @param clazz classe cible.
     * @param <T> type cible.
     * @return objet désérialisé, ou `null` si corps vide.
     */
    private <T> T deserializeBody(byte[] bodyBytes, HttpExchange exchange, Class<T> clazz) {

        if (bodyBytes == null || bodyBytes.length == 0)
            return null;

        validateJsonContentType(exchange);

        try {
            return objectMapper.readValue(bodyBytes, clazz);
        } catch (IOException exception) {
            throw new HttpErrorException(BAD_REQUEST, "invalid_json", "Malformed JSON body", exception);
        }
    }

    /**
     * Vérifie que le Content-Type est bien `application/json` pour les corps JSON. Sinon, lève une exception HTTP 415.
     *
     * @param exchange échange HTTP courant.
     */
    private void validateJsonContentType(HttpExchange exchange) {

        String contentType = exchange.getRequestHeaders().getFirst(CONTENT_TYPE);

        if (contentType == null || !contentType.toLowerCase().contains(APPLICATION_JSON))
            throw new HttpErrorException(UNSUPPORTED_MEDIA_TYPE, "unsupported_media_type", "Body content type must be application/json");
    }

    /**
     * Résout automatiquement un paramètre de type `AUTO`.
     *
     * <p>Règles :</p>
     * <ul>
     *   <li>si le type cible est `String`, on retourne la première valeur query param ;</li>
     *   <li>si le type cible est un type primitif ou wrapper, on retourne la première valeur query param convertie ;</li>
     *   <li>si le corps HTTP est présent, on désérialise le JSON vers le type cible ;</li>
     *   <li>sinon, on retourne `null`.</li>
     * </ul>
     *
     * @param queryValues map des query params.
     * @param bodyBytes contenu brut du corps HTTP.
     * @param exchange échange HTTP courant.
     * @param type classe cible.
     * @param name nom du paramètre.
     * @return valeur résolue.
     */
    private Object autoResolve(Map<String, List<String>> queryValues,
                               byte[] bodyBytes,
                               HttpExchange exchange,
                               Class<?> type,
                               String name) {

        if (isSimpleType(type))
            return convert(firstOrNull(queryValues.get(name)), type, name);

        return deserializeBody(bodyBytes, exchange, type);
    }

    /**
     * Vérifie si un type est considéré comme "simple" (chaîne, nombre, booléen).
     *
     * @param type type à vérifier.
     * @return `true` si simple, `false` sinon.
     */
    private boolean isSimpleType(Class<?> type) {
        return type == String.class
                || type == Integer.class || type == int.class
                || type == Long.class || type == long.class
                || type == Boolean.class || type == boolean.class
                || type == Double.class || type == double.class
                || type == Float.class || type == float.class
                || type == Short.class || type == short.class
                || type == Byte.class || type == byte.class;
    }

    /**
     * Convertit une valeur String vers un type Java supporté.
     *
     * @param value valeur textuelle source.
     * @param type type cible.
     * @param paramName nom logique du paramètre.
     * @return valeur convertie.
     */
    private Object convert(String value, Class<?> type, String paramName) {

        if (value == null || value.isBlank()) {

            if (type.isPrimitive())
                throw new HttpErrorException(BAD_REQUEST, "missing_parameter", "Missing required parameter: " + paramName);

            return null;
        }

        try {
            if (type == String.class)
                return value;

            if (type == UUID.class)
                return UUID.fromString(value);

            if (type == Integer.class || type == int.class)
                return Integer.parseInt(value);

            if (type == Long.class || type == long.class)
                return Long.parseLong(value);

            if (type == Double.class || type == double.class)
                return Double.parseDouble(value);

            if (type == Float.class || type == float.class)
                return Float.parseFloat(value);

            if (type == Short.class || type == short.class)
                return Short.parseShort(value);

            if (type == Byte.class || type == byte.class)
                return Byte.parseByte(value);

            if (type == Boolean.class || type == boolean.class) {

                if ("true".equalsIgnoreCase(value))
                    return true;

                if ("false".equalsIgnoreCase(value))
                    return false;

                throw new IllegalArgumentException("❌ Boolean value must be true or false");
            }
        } catch (RuntimeException exception) {
            throw new HttpErrorException(BAD_REQUEST, "invalid_parameter", "Invalid value for parameter: " + paramName, exception);
        }

        throw new HttpErrorException(UNPROCESSABLE_ENTITY, "unsupported_type", "Unsupported parameter type: " + type.getName());
    }

    /**
     * Serialise un objet Java en JSON (byte[]).
     *
     * @param object objet Java à sérialiser.
     * @return tableau d'octets JSON.
     * @throws IOException en cas d'erreur de sérialisation.
     */
    private byte[] serialize(Object object) throws IOException {

        if (object == null)
            return new byte[0];

        return objectMapper.writeValueAsBytes(object);
    }

    /**
     * Ecrit une réponse HTTP standard (succès).
     *
     * @param exchange échange HTTP courant.
     * @param responseEntity résultat de méthode controller.
     * @param headRequest indique si la requête initiale est de type HEAD.
     * @throws Exception en cas d'erreur de sérialisation/écriture.
     */
    private void writeResponse(HttpExchange exchange, ResponseEntity<?> responseEntity, boolean headRequest) throws Exception {

        if (responseEntity == null) {
            exchange.sendResponseHeaders(NOT_FOUND, -1);

            trace(" ⤷ Response: %d (-1 bytes)".formatted(NOT_FOUND));
            return;
        }

        Object body = responseEntity.getBody();
        int status = responseEntity.getCode() < 100 ? NOT_FOUND : responseEntity.getCode();

        if (body == null) {

            exchange.sendResponseHeaders(status, -1);

            trace(" ⤷ Response: %d (-1 bytes)".formatted(status));
            return;
        }

        byte[] bytes = serialize(body);

        exchange.getResponseHeaders().add(CONTENT_TYPE, APPLICATION_JSON);
        exchange.sendResponseHeaders(status, bytes.length);

        if (!headRequest) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        trace(" ⤷ Response: " + status + " (" + bytes.length + " bytes)");
    }

    /**
     * Ecrit une réponse d'erreur JSON standardisée.
     *
     * @param exchange échange HTTP courant.
     * @param status code HTTP.
     * @param code code d'erreur fonctionnel.
     * @param message message d'erreur.
     * @throws IOException en cas d'erreur d'écriture.
     */
    private void writeError(HttpExchange exchange, int status, String code, String message) throws IOException {

        Map<String, Object> payload = Map.of(
                "code", code,
                "message", message,
                "status", status);

        byte[] bytes = serialize(payload);

        exchange.getResponseHeaders().set(CONTENT_TYPE, APPLICATION_JSON);
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    /**
     * Ecrit une réponse sans corps (204 No Content).
     *
     * @param exchange échange HTTP courant.
     * @param allowedMethods méthodes HTTP autorisées.
     * @throws IOException en cas d'erreur d'écriture.
     */
    private void writeOptionsResponse(HttpExchange exchange, Set<String> allowedMethods) throws IOException {
        exchange.getResponseHeaders().add("Allow", String.join(", ", allowedMethods));
        exchange.sendResponseHeaders(NO_CONTENT, -1);
    }

    /**
     * Invoque la méthode controller cible et transforme les erreurs.
     *
     * @param route route résolue.
     * @param args arguments construits.
     * @return résultat de la méthode controller.
     * @throws Exception erreur d'invocation encapsulée.
     */
    private ResponseEntity<?> invokeController(RouteDefinition route, Object[] args) throws Exception {
        try {
            return (ResponseEntity<?>) route.getMethod().invoke(route.getControllerInstance(), args);
        } catch (InvocationTargetException invocationTargetException) {

            Throwable cause = invocationTargetException.getTargetException();

            if (cause instanceof HttpErrorException httpErrorException)
                throw httpErrorException;

            throw new HttpErrorException(INTERNAL_SERVER_ERROR, "controller_error", "Controller execution failed", cause);
        }
    }

    /**
     * Vérifie l'unicité des routes `(méthode HTTP + chemin)`.
     *
     * @throws IllegalStateException en cas de collision de route.
     */
    private void validateRouteUniqueness() {

        Set<String> keys = new LinkedHashSet<>();

        for (RouteDefinition route : routes) {

            String key = route.getHttpMethod() + " " + route.getPathPattern();

            if (keys.contains(key))
                throw new IllegalStateException("❌ Duplicate route detected: " + key);

            keys.add(key);
        }
    }

    /**
     * Parse les éléments d'une query string en une map clé/valeur.
     *
     * @param rawQuery query string brute.
     * @return map clé/valeur.
     */
    private Map<String, List<String>> parseQuery(String rawQuery) {

        Map<String, List<String>> queryValues = new LinkedHashMap<>();

        if (rawQuery == null || rawQuery.isBlank())
            return queryValues;

        String[] pairs = rawQuery.split("&");

        for (String pair : pairs) {

            if (pair.isBlank())
                continue;

            String[] keyValue = pair.split("=", 2);
            String key = decode(keyValue[0]);
            String value = keyValue.length == 2 ? decode(keyValue[1]) : "";

            queryValues.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
        }

        return queryValues;
    }

    /**
     * Renvoie la première valeur d'une liste de valeurs ou null si la liste est vide ou nulle.
     *
     * @param values liste de valeurs.
     * @return première valeur ou null.
     */
    private String firstOrNull(List<String> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    /**
     * Normalise un chemin en supprimant les slashs redondants et les slashs de fin.
     *
     * @param path chemin à normaliser.
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

    /**
     * Résout l'annotation HTTP portée par une méthode controller.
     *
     * @param method méthode à inspecter.
     * @return binding HTTP correspondant, ou `null` si aucune annotation HTTP.
     */
    private HttpBinding resolveHttpBinding(Method method) {

        Get get = method.getAnnotation(Get.class);
        if (get != null)
            return new HttpBinding(METHOD_GET, get.value());

        Post post = method.getAnnotation(Post.class);
        if (post != null)
            return new HttpBinding(METHOD_POST, post.value());

        Put put = method.getAnnotation(Put.class);
        if (put != null)
            return new HttpBinding(METHOD_PUT, put.value());

        Delete delete = method.getAnnotation(Delete.class);
        if (delete != null)
            return new HttpBinding(METHOD_DELETE, delete.value());

        Patch patch = method.getAnnotation(Patch.class);
        if (patch != null)
            return new HttpBinding(METHOD_PATCH, patch.value());

        Head head = method.getAnnotation(Head.class);
        if (head != null)
            return new HttpBinding(METHOD_HEAD, head.value());

        Options options = method.getAnnotation(Options.class);
        if (options != null)
            return new HttpBinding(METHOD_OPTIONS, options.value());

        return null;
    }

    /** Résultat de résolution de route (route + méthodes autorisées). */
    private record RouteResolution(RouteDefinition route, Set<String> allowedMethods) {

    }

    /** Binding simple entre méthode HTTP et chemin d'annotation. */
    private record HttpBinding(String httpMethod, String path) {

    }

    /** Exception interne utilisée pour transporter un statut/code HTTP métier. */
    private static class HttpErrorException extends RuntimeException {

        /** Code HTTP. */
        private final int status;

        /** Code d'erreur fonctionnel. */
        private final String code;

        /**
         * Construit une exception métier.
         *
         * @param status code HTTP.
         * @param code code d'erreur fonctionnel.
         * @param message message d'erreur.
         */
        private HttpErrorException(int status, String code, String message) {
            super(message);
            this.status = status;
            this.code = code;
        }

        /**
         * Construit une exception métier avec cause.
         *
         * @param status code HTTP.
         * @param code code d'erreur fonctionnel.
         * @param message message d'erreur.
         * @param cause cause de l'exception.
         */
        private HttpErrorException(int status, String code, String message, Throwable cause) {
            super(message, cause);
            this.status = status;
            this.code = code;
        }

        /**
         * Renvoie le code HTTP.
         *
         * @return code HTTP.
         */
        private int status() {
            return status;
        }

        /**
         * Renvoie le code d'erreur fonctionnel.
         *
         * @return code d'erreur fonctionnel.
         */
        private String code() {
            return code;
        }
    }
}
