package com.jasonpercus.microbean.entrypoint;

/*
 * Copyright (c) 2026 JasonPercus
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for more information.
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.jasonpercus.microbean.api.Body;
import com.jasonpercus.microbean.api.Controller;
import com.jasonpercus.microbean.api.Header;
import com.jasonpercus.microbean.api.Path;
import com.jasonpercus.microbean.api.Query;
import com.jasonpercus.microbean.api.Status;
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
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires de {@link HttpRestServerService}.
 *
 * <p>Toutes les méthodes privées sont accessibles via reflection.
 * Les tests HTTP complets utilisent {@link MockHttpExchange} pour capturer
 * le statut et le corps de réponse sans démarrer de serveur réel.</p>
 */
@DisplayName("HttpRestServerService")
class HttpRestServerServiceTest {

    private HttpRestServerService service;
    private DummyController controller;

    @BeforeEach
    void setUp() {
        service = new HttpRestServerService();
        controller = new DummyController();
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private Method privateMethod(String name, Class<?>... paramTypes) throws Exception {
        Method m = HttpRestServerService.class.getDeclaredMethod(name, paramTypes);
        m.setAccessible(true);
        return m;
    }

    @SuppressWarnings("unchecked")
    private List<RouteDefinition> routesList() throws Exception {
        Field f = HttpRestServerService.class.getDeclaredField("routes");
        f.setAccessible(true);
        return (List<RouteDefinition>) f.get(service);
    }

    private void injectRoute(String httpMethod,
                             String pattern,
                             Method controllerMethod,
                             List<RouteParam> params) throws Exception {
        routesList().add(new RouteDefinition(httpMethod, pattern, controller, controllerMethod, params));
    }

    /**
     * Vérifie qu'un {@link InvocationTargetException} contient une {@code HttpErrorException}
     * avec le statut HTTP attendu.
     */
    @SuppressWarnings("SameParameterValue")
    private void assertHttpErrorStatus(int expectedStatus,
                                       ThrowingRunnable action) throws Exception {
        InvocationTargetException wrapper = assertThrows(
                InvocationTargetException.class,
                action::run);

        Throwable cause = wrapper.getTargetException();
        assertThat(cause.getClass().getSimpleName()).isEqualTo("HttpErrorException");

        Method statusAccessor = cause.getClass().getDeclaredMethod("status");
        statusAccessor.setAccessible(true);
        int actualStatus = (int) statusAccessor.invoke(cause);
        assertEquals(expectedStatus, actualStatus,
                "Expected HTTP status " + expectedStatus + " but got " + actualStatus);
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    // =========================================================================
    // couverture ciblée méthodes manquantes
    // =========================================================================

    @Nested
    @DisplayName("Méthodes globales manquantes")
    class MissingGlobalCoverageTests {

        @Test
        @DisplayName("getPort retourne une valeur positive")
        void getPort_returnsPositive() throws Exception {
            Method getPort = privateMethod("getPort");
            int port = (int) getPort.invoke(null);
            assertTrue(port > 0);
        }

        @Test
        @DisplayName("scanControllers lève une exception hors runtime MicroBean")
        void scanControllers_withoutContext_throws() throws Exception {
            Method scanControllers = privateMethod("scanControllers");
            assertThrows(InvocationTargetException.class, () -> scanControllers.invoke(service));
        }

        @Test
        @DisplayName("main exécute son pipeline et lève si contexte indisponible")
        void main_withoutContext_throws() {
            assertThrows(Exception.class, () -> service.main(new String[0]));
        }

        @Test
        @DisplayName("buildServer initialise le champ server")
        void buildServer_initializesServerField() throws Exception {
            Method buildServer = privateMethod("buildServer", int.class);
            buildServer.invoke(service, 0);

            Field serverField = HttpRestServerService.class.getDeclaredField("server");
            serverField.setAccessible(true);
            HttpServer server = (HttpServer) serverField.get(service);

            assertNotNull(server);
            server.stop(0);
        }

        @Test
        @DisplayName("resolveBasePath retourne la valeur de @Controller")
        void resolveBasePath_returnsControllerValue() throws Exception {
            Method resolveBasePath = privateMethod("resolveBasePath", Object.class);
            String basePath = (String) resolveBasePath.invoke(service, new AnnotatedController());
            assertEquals("/base", basePath);
        }

        @Test
        @DisplayName("parseMethod ajoute une route quand annotation HTTP présente")
        void parseMethod_addsRoute() throws Exception {
            Method parseMethod = privateMethod("parseMethod", Object.class, Method.class);
            Method handler = AnnotatedController.class.getMethod("getItem", long.class);

            parseMethod.invoke(service, new AnnotatedController(), handler);

            List<RouteDefinition> routes = routesList();
            assertThat(routes).hasSize(1);
            assertThat(routes.get(0).getHttpMethod()).isEqualTo("GET");
            assertThat(routes.get(0).getPathPattern()).isEqualTo("/base/items/{id}");
        }

        @Test
        @DisplayName("resolveHttpBinding retourne null sans annotation HTTP")
        void resolveHttpBinding_noAnnotation_returnsNull() throws Exception {
            Method resolveHttpBinding = privateMethod("resolveHttpBinding", Method.class);
            Method methodWithoutAnnotation = NonAnnotatedController.class.getMethod("noHttpAnnotation");
            Object binding = resolveHttpBinding.invoke(service, methodWithoutAnnotation);
            assertNull(binding);
        }

        @Test
        @DisplayName("resolveHttpBinding couvre tous les verbes + record HttpBinding")
        void resolveHttpBinding_allVerbs_andRecordAccessors() throws Exception {
            Method resolveHttpBinding = privateMethod("resolveHttpBinding", Method.class);

            Map<String, Method> methods = Map.of(
                    "GET", VerbAnnotatedController.class.getMethod("get"),
                    "POST", VerbAnnotatedController.class.getMethod("post"),
                    "PUT", VerbAnnotatedController.class.getMethod("put"),
                    "DELETE", VerbAnnotatedController.class.getMethod("delete"),
                    "PATCH", VerbAnnotatedController.class.getMethod("patch"),
                    "HEAD", VerbAnnotatedController.class.getMethod("head"),
                    "OPTIONS", VerbAnnotatedController.class.getMethod("options")
            );

            for (Map.Entry<String, Method> entry : methods.entrySet()) {
                Object binding = resolveHttpBinding.invoke(service, entry.getValue());
                assertNotNull(binding);

                Method httpMethod = binding.getClass().getDeclaredMethod("httpMethod");
                Method path = binding.getClass().getDeclaredMethod("path");

                assertEquals(entry.getKey(), httpMethod.invoke(binding));
                assertEquals("/x", path.invoke(binding));
            }
        }
    }

    @Nested
    @DisplayName("autoResolve et firstOrNull")
    class AutoResolveAndFirstOrNullTests {

        @Test
        @DisplayName("firstOrNull retourne null sur liste vide")
        void firstOrNull_empty_returnsNull() throws Exception {
            Method firstOrNull = privateMethod("firstOrNull", List.class);
            assertNull(firstOrNull.invoke(service, List.of()));
        }

        @Test
        @DisplayName("firstOrNull retourne le premier élément")
        void firstOrNull_returnsFirst() throws Exception {
            Method firstOrNull = privateMethod("firstOrNull", List.class);
            assertEquals("a", firstOrNull.invoke(service, List.of("a", "b")));
        }

        @Test
        @DisplayName("autoResolve simple type → query convertie")
        void autoResolve_simpleType_usesQuery() throws Exception {
            Method autoResolve = privateMethod("autoResolve", Map.class, byte[].class, HttpExchange.class, Class.class, String.class);
            MockHttpExchange exchange = new MockHttpExchange("GET", "/test", null, null);

            Object value = autoResolve.invoke(service,
                    Map.of("age", List.of("42")),
                    new byte[0],
                    exchange,
                    int.class,
                    "age");

            assertEquals(42, value);
        }

        @Test
        @DisplayName("autoResolve type complexe → body JSON")
        void autoResolve_complexType_usesBody() throws Exception {
            Method autoResolve = privateMethod("autoResolve", Map.class, byte[].class, HttpExchange.class, Class.class, String.class);
            MockHttpExchange exchange = new MockHttpExchange(
                    "POST",
                    "/test",
                    "{\"name\":\"alice\"}".getBytes(StandardCharsets.UTF_8),
                    Map.of("Content-Type", "application/json"));

            Object value = autoResolve.invoke(service,
                    Map.of(),
                    "{\"name\":\"alice\"}".getBytes(StandardCharsets.UTF_8),
                    exchange,
                    SamplePayload.class,
                    "payload");

            assertInstanceOf(SamplePayload.class, value);
            assertEquals("alice", ((SamplePayload) value).name);
        }
    }

    // =========================================================================
    // normalizePath
    // =========================================================================

    @Nested
    @DisplayName("normalizePath")
    class NormalizePathTests {

        private Method m;

        @BeforeEach
        void setUp() throws Exception {
            m = privateMethod("normalizePath", String.class);
        }

        private String call(String path) throws Exception {
            return (String) m.invoke(service, path);
        }

        @Test
        @DisplayName("null → /")
        void nullInput_returnsSlash() throws Exception {
            assertEquals("/", call(null));
        }

        @Test
        @DisplayName("blank → /")
        void blankInput_returnsSlash() throws Exception {
            assertEquals("/", call("   "));
        }

        @Test
        @DisplayName("/ conservé tel quel")
        void rootSlash_returnsSlash() throws Exception {
            assertEquals("/", call("/"));
        }

        @Test
        @DisplayName("sans slash initial → slash ajouté")
        void missingLeadingSlash_added() throws Exception {
            assertEquals("/users", call("users"));
        }

        @Test
        @DisplayName("slash terminal supprimé")
        void trailingSlash_removed() throws Exception {
            assertEquals("/users", call("/users/"));
        }

        @Test
        @DisplayName("chemin normal inchangé")
        void normalPath_unchanged() throws Exception {
            assertEquals("/users/42", call("/users/42"));
        }

        @Test
        @DisplayName("chemin sans slash initial + slash terminal")
        void noLeadingSlash_withTrailingSlash() throws Exception {
            assertEquals("/users/42", call("users/42/"));
        }
    }

    // =========================================================================
    // parseQuery
    // =========================================================================

    @Nested
    @DisplayName("parseQuery")
    class ParseQueryTests {

        private Method m;

        @BeforeEach
        void setUp() throws Exception {
            m = privateMethod("parseQuery", String.class);
        }

        @SuppressWarnings("unchecked")
        private Map<String, List<String>> call(String raw) throws Exception {
            return (Map<String, List<String>>) m.invoke(service, raw);
        }

        @Test
        @DisplayName("null → map vide")
        void nullInput_returnsEmptyMap() throws Exception {
            assertThat(call(null)).isEmpty();
        }

        @Test
        @DisplayName("blank → map vide")
        void blankInput_returnsEmptyMap() throws Exception {
            assertThat(call("  ")).isEmpty();
        }

        @Test
        @DisplayName("paramètre unique parsé")
        void singleParam_parsed() throws Exception {
            assertThat(call("name=Alice").get("name")).containsExactly("Alice");
        }

        @Test
        @DisplayName("paramètres multiples tous parsés")
        void multipleParams_allParsed() throws Exception {
            Map<String, List<String>> result = call("a=1&b=2");
            assertThat(result.get("a")).containsExactly("1");
            assertThat(result.get("b")).containsExactly("2");
        }

        @Test
        @DisplayName("clé multi-valeurs agrégée")
        void multiValueKey_aggregated() throws Exception {
            assertThat(call("tag=a&tag=b").get("tag")).containsExactly("a", "b");
        }

        @Test
        @DisplayName("paramètre sans valeur → chaîne vide")
        void paramWithoutValue_returnsEmpty() throws Exception {
            assertThat(call("key").get("key")).containsExactly("");
        }

        @Test
        @DisplayName("URL encodé → décodé")
        void urlEncoded_decoded() throws Exception {
            assertThat(call("q=hello+world").get("q")).containsExactly("hello world");
        }

        @Test
        @DisplayName("valeur contenant '=' → split sur premier '='")
        void valueWithEquals_splitOnFirst() throws Exception {
            assertThat(call("token=a=b=c").get("token")).containsExactly("a=b=c");
        }
    }

    // =========================================================================
    // convert
    // =========================================================================

    @Nested
    @DisplayName("convert")
    class ConvertTests {

        private Method m;

        @BeforeEach
        void setUp() throws Exception {
            m = privateMethod("convert", String.class, Class.class, String.class);
        }

        private Object call(String value, Class<?> type) throws Exception {
            return m.invoke(service, value, type, "param");
        }

        @Test
        @DisplayName("String → retourné tel quel")
        void string_returnsAsIs() throws Exception {
            assertEquals("hello", call("hello", String.class));
        }

        @Test
        @DisplayName("int primitif parsé")
        void intPrimitive_parsed() throws Exception {
            assertEquals(42, call("42", int.class));
        }

        @Test
        @DisplayName("Integer wrapper parsé")
        void intWrapper_parsed() throws Exception {
            assertEquals(42, call("42", Integer.class));
        }

        @Test
        @DisplayName("long primitif parsé")
        void longPrimitive_parsed() throws Exception {
            assertEquals(100L, call("100", long.class));
        }

        @Test
        @DisplayName("Long wrapper parsé")
        void longWrapper_parsed() throws Exception {
            assertEquals(100L, call("100", Long.class));
        }

        @Test
        @DisplayName("double parsé")
        void double_parsed() throws Exception {
            assertEquals(1.5, call("1.5", double.class));
        }

        @Test
        @DisplayName("float parsé")
        void float_parsed() throws Exception {
            assertEquals(1.5f, call("1.5", float.class));
        }

        @Test
        @DisplayName("short parsé")
        void short_parsed() throws Exception {
            assertEquals((short) 5, call("5", short.class));
        }

        @Test
        @DisplayName("byte parsé")
        void byte_parsed() throws Exception {
            assertEquals((byte) 1, call("1", byte.class));
        }

        @Test
        @DisplayName("boolean true (insensible à la casse)")
        void booleanTrue_returnsTrue() throws Exception {
            assertEquals(Boolean.TRUE, call("true", boolean.class));
            assertEquals(Boolean.TRUE, call("TRUE", boolean.class));
            assertEquals(Boolean.TRUE, call("True", Boolean.class));
        }

        @Test
        @DisplayName("boolean false")
        void booleanFalse_returnsFalse() throws Exception {
            assertEquals(Boolean.FALSE, call("false", Boolean.class));
        }

        @Test
        @DisplayName("valeur booléenne invalide → 400")
        void invalidBoolean_throwsHttpError400() {
            assertHttpError400(() -> call("yep", boolean.class));
        }

        @Test
        @DisplayName("nombre invalide → 400")
        void invalidNumber_throwsHttpError400() {
            assertHttpError400(() -> call("notanumber", int.class));
        }

        @Test
        @DisplayName("null pour primitif → 400 missing_parameter")
        void nullForPrimitive_throwsHttpError400() {
            assertHttpError400(() -> call(null, int.class));
        }

        @Test
        @DisplayName("null pour wrapper → null")
        void nullForWrapper_returnsNull() throws Exception {
            assertNull(call(null, Integer.class));
        }

        @Test
        @DisplayName("blank pour wrapper → null")
        void blankForWrapper_returnsNull() throws Exception {
            assertNull(call("   ", Long.class));
        }

        private void assertHttpError400(ThrowingRunnable action) {
            try {
                assertHttpErrorStatus(400, action);
            } catch (Exception e) {
                throw new AssertionError("assertHttpError400 failed: " + e.getMessage(), e);
            }
        }
    }

    // =========================================================================
    // isSimpleType
    // =========================================================================

    @Nested
    @DisplayName("isSimpleType")
    class IsSimpleTypeTests {

        private Method m;

        @BeforeEach
        void setUp() throws Exception {
            m = privateMethod("isSimpleType", Class.class);
        }

        private boolean call(Class<?> type) throws Exception {
            return (boolean) m.invoke(service, type);
        }

        @Test void string_true() throws Exception { assertTrue(call(String.class)); }
        @Test void intPrimitive_true() throws Exception { assertTrue(call(int.class)); }
        @Test void intWrapper_true() throws Exception { assertTrue(call(Integer.class)); }
        @Test void longPrimitive_true() throws Exception { assertTrue(call(long.class)); }
        @Test void longWrapper_true() throws Exception { assertTrue(call(Long.class)); }
        @Test void booleanPrimitive_true() throws Exception { assertTrue(call(boolean.class)); }
        @Test void doublePrimitive_true() throws Exception { assertTrue(call(double.class)); }
        @Test void floatPrimitive_true() throws Exception { assertTrue(call(float.class)); }
        @Test void listClass_false() throws Exception { assertFalse(call(List.class)); }
        @Test void mapClass_false() throws Exception { assertFalse(call(Map.class)); }
        @Test void objectClass_false() throws Exception { assertFalse(call(Object.class)); }
    }

    // =========================================================================
    // withImplicitMethods
    // =========================================================================

    @Nested
    @DisplayName("withImplicitMethods")
    class WithImplicitMethodsTests {

        private Method m;

        @BeforeEach
        void setUp() throws Exception {
            m = privateMethod("withImplicitMethods", Set.class);
        }

        @SuppressWarnings("unchecked")
        private Set<String> call(String... methods) throws Exception {
            Set<String> input = new LinkedHashSet<>(List.of(methods));
            return (Set<String>) m.invoke(service, input);
        }

        @Test
        @DisplayName("GET → HEAD et OPTIONS ajoutés")
        void withGet_addsHeadAndOptions() throws Exception {
            Set<String> result = call("GET");
            assertThat(result).contains("HEAD", "OPTIONS", "GET");
        }

        @Test
        @DisplayName("POST → seulement OPTIONS ajouté")
        void withPost_onlyAddsOptions() throws Exception {
            Set<String> result = call("POST");
            assertThat(result).contains("POST", "OPTIONS");
            assertThat(result).doesNotContain("HEAD");
        }

        @Test
        @DisplayName("vide → seulement OPTIONS")
        void empty_onlyOptions() throws Exception {
            Set<String> result = call();
            assertThat(result).containsExactly("OPTIONS");
        }

        @Test
        @DisplayName("GET + POST → HEAD et OPTIONS ajoutés")
        void getAndPost_headAndOptionsAdded() throws Exception {
            Set<String> result = call("GET", "POST");
            assertThat(result).containsAll(List.of("GET", "POST", "HEAD", "OPTIONS"));
        }
    }

    // =========================================================================
    // validateRouteUniqueness
    // =========================================================================

    @Nested
    @DisplayName("validateRouteUniqueness")
    class ValidateRouteUniquenessTests {

        private Method m;

        @BeforeEach
        void setUp() throws Exception {
            m = privateMethod("validateRouteUniqueness");
        }

        private void call() throws Exception {
            m.invoke(service);
        }

        @Test
        @DisplayName("routes distinctes → pas d'exception")
        void distinctRoutes_noException() throws Exception {
            Method ping = DummyController.class.getMethod("ping");
            injectRoute("GET", "/a", ping, List.of());
            injectRoute("GET", "/b", ping, List.of());
            assertDoesNotThrow(this::call);
        }

        @Test
        @DisplayName("routes dupliquées → IllegalStateException")
        void duplicateRoutes_throwsIllegalState() throws Exception {
            Method ping = DummyController.class.getMethod("ping");
            injectRoute("GET", "/a", ping, List.of());
            injectRoute("GET", "/a", ping, List.of());

            InvocationTargetException ex = assertThrows(InvocationTargetException.class, this::call);
            assertInstanceOf(IllegalStateException.class, ex.getTargetException());
            assertThat(ex.getTargetException().getMessage()).contains("Duplicate route");
        }
    }

    // =========================================================================
    // analyzeParameters
    // =========================================================================

    @Nested
    @DisplayName("analyzeParameters")
    class AnalyzeParametersTests {

        private Method m;

        @BeforeEach
        void setUp() throws Exception {
            m = privateMethod("analyzeParameters", Method.class);
        }

        @SuppressWarnings("unchecked")
        private List<RouteParam> call(Method method) throws Exception {
            return (List<RouteParam>) m.invoke(service, method);
        }

        @Test
        @DisplayName("@Path → ParameterType.PATH avec bon nom")
        void pathAnnotation_createsPathParam() throws Exception {
            Method cm = ParamTestController.class.getMethod("pathParam", long.class);
            List<RouteParam> params = call(cm);
            assertThat(params).hasSize(1);
            assertThat(params.get(0).type()).isEqualTo(ParameterType.PATH);
            assertThat(params.get(0).name()).isEqualTo("id");
            assertThat(params.get(0).clazz()).isEqualTo(long.class);
        }

        @Test
        @DisplayName("@Query → ParameterType.QUERY avec bon nom")
        void queryAnnotation_createsQueryParam() throws Exception {
            Method cm = ParamTestController.class.getMethod("queryParam", String.class);
            List<RouteParam> params = call(cm);
            assertThat(params).hasSize(1);
            assertThat(params.get(0).type()).isEqualTo(ParameterType.QUERY);
            assertThat(params.get(0).name()).isEqualTo("name");
        }

        @Test
        @DisplayName("@Header → ParameterType.HEADER avec bon nom")
        void headerAnnotation_createsHeaderParam() throws Exception {
            Method cm = ParamTestController.class.getMethod("headerParam", String.class);
            List<RouteParam> params = call(cm);
            assertThat(params).hasSize(1);
            assertThat(params.get(0).type()).isEqualTo(ParameterType.HEADER);
            assertThat(params.get(0).name()).isEqualTo("Authorization");
        }

        @Test
        @DisplayName("@Body → ParameterType.BODY avec name null")
        void bodyAnnotation_createsBodyParam() throws Exception {
            Method cm = ParamTestController.class.getMethod("bodyParam", Map.class);
            List<RouteParam> params = call(cm);
            assertThat(params).hasSize(1);
            assertThat(params.get(0).type()).isEqualTo(ParameterType.BODY);
            assertNull(params.get(0).name());
        }

        @Test
        @DisplayName("sans annotation → ParameterType.AUTO")
        void noAnnotation_createsAutoParam() throws Exception {
            Method cm = ParamTestController.class.getMethod("autoParam", String.class);
            List<RouteParam> params = call(cm);
            assertThat(params).hasSize(1);
            assertThat(params.get(0).type()).isEqualTo(ParameterType.AUTO);
        }

        @Test
        @DisplayName("paramètres multiples → tous créés dans l'ordre")
        void multipleParams_allCreatedInOrder() throws Exception {
            Method cm = ParamTestController.class.getMethod("multipleParams", long.class, String.class);
            List<RouteParam> params = call(cm);
            assertThat(params).hasSize(2);
            assertThat(params.get(0).type()).isEqualTo(ParameterType.PATH);
            assertThat(params.get(1).type()).isEqualTo(ParameterType.QUERY);
        }
    }

    // =========================================================================
    // resolveRoute
    // =========================================================================

    @Nested
    @DisplayName("resolveRoute")
    class ResolveRouteTests {

        private Method m;

        @BeforeEach
        void setUp() throws Exception {
            m = privateMethod("resolveRoute", String.class, String.class);
            Method ping = DummyController.class.getMethod("ping");
            Method create = DummyController.class.getMethod("create", Map.class);
            injectRoute("GET", "/ping", ping, List.of());
            injectRoute("POST", "/items", create, List.of(
                    new RouteParam(ParameterType.BODY, null, Map.class)));
        }

        private Object call(String method, String path) throws Exception {
            return m.invoke(service, method, path);
        }

        private Object getRoute(Object resolution) throws Exception {
            Method r = resolution.getClass().getDeclaredMethod("route");
            return r.invoke(resolution);
        }

        @SuppressWarnings("unchecked")
        private Set<String> getAllowed(Object resolution) throws Exception {
            Method am = resolution.getClass().getDeclaredMethod("allowedMethods");
            return (Set<String>) am.invoke(resolution);
        }

        @Test
        @DisplayName("GET /ping → route trouvée")
        void exactGetMatch_returnsRoute() throws Exception {
            assertNotNull(getRoute(call("GET", "/ping")));
        }

        @Test
        @DisplayName("POST /items → route trouvée")
        void exactPostMatch_returnsRoute() throws Exception {
            assertNotNull(getRoute(call("POST", "/items")));
        }

        @Test
        @DisplayName("HEAD sur route GET → utilise le handler GET")
        void headOnGetRoute_returnsGetHandler() throws Exception {
            assertNotNull(getRoute(call("HEAD", "/ping")));
        }

        @Test
        @DisplayName("chemin inconnu → route null, OPTIONS dans Allow")
        void unknownPath_nullRouteWithOptions() throws Exception {
            Object resolution = call("GET", "/unknown");
            assertNull(getRoute(resolution));
            assertThat(getAllowed(resolution)).contains("OPTIONS");
        }

        @Test
        @DisplayName("méthode interdite sur route connue → route null avec Allow renseigné")
        void wrongMethod_nullRouteWithAllow() throws Exception {
            Object resolution = call("PUT", "/ping");
            assertNull(getRoute(resolution));
            Set<String> allowed = getAllowed(resolution);
            assertThat(allowed).contains("GET", "HEAD", "OPTIONS");
        }

        @Test
        @DisplayName("OPTIONS sur route connue → route null avec Allow renseigné")
        void optionsOnKnownPath_allowedMethodsPresent() throws Exception {
            Object resolution = call("OPTIONS", "/ping");
            assertNull(getRoute(resolution));
            assertThat(getAllowed(resolution)).contains("GET", "HEAD", "OPTIONS");
        }
    }

    // =========================================================================
    // buildArguments branches QUERY / HEADER / AUTO
    // =========================================================================

    @Nested
    @DisplayName("buildArguments branches manquantes")
    class BuildArgumentsBranchTests {

        private Method buildArguments;

        @BeforeEach
        void setUp() throws Exception {
            buildArguments = privateMethod("buildArguments", RouteDefinition.class, HttpExchange.class, String.class);
        }

        @Test
        @DisplayName("branche QUERY couverte")
        void buildArguments_queryBranch() throws Exception {
            Method method = QueryHeaderAutoController.class.getMethod("queryOnly", String.class);
            RouteDefinition route = new RouteDefinition(
                    "GET",
                    "/users",
                    new QueryHeaderAutoController(),
                    method,
                    List.of(new RouteParam(ParameterType.QUERY, "name", String.class))
            );

            MockHttpExchange exchange = new MockHttpExchange("GET", "/users?name=alice", null, null);

            Object[] args = (Object[]) buildArguments.invoke(service, route, exchange, "/users");
            assertThat(args).containsExactly("alice");
        }

        @Test
        @DisplayName("branche HEADER couverte")
        void buildArguments_headerBranch() throws Exception {
            Method method = QueryHeaderAutoController.class.getMethod("headerOnly", String.class);
            RouteDefinition route = new RouteDefinition(
                    "GET",
                    "/users",
                    new QueryHeaderAutoController(),
                    method,
                    List.of(new RouteParam(ParameterType.HEADER, "Authorization", String.class))
            );

            MockHttpExchange exchange = new MockHttpExchange(
                    "GET",
                    "/users",
                    null,
                    Map.of("Authorization", "Bearer abc")
            );

            Object[] args = (Object[]) buildArguments.invoke(service, route, exchange, "/users");
            assertThat(args).containsExactly("Bearer abc");
        }

        @Test
        @DisplayName("branche AUTO simple type (query)")
        void buildArguments_autoSimpleBranch() throws Exception {
            Method method = QueryHeaderAutoController.class.getMethod("autoInt", int.class);
            RouteDefinition route = new RouteDefinition(
                    "GET",
                    "/users",
                    new QueryHeaderAutoController(),
                    method,
                    List.of(new RouteParam(ParameterType.AUTO, "age", int.class))
            );

            MockHttpExchange exchange = new MockHttpExchange("GET", "/users?age=32", null, null);

            Object[] args = (Object[]) buildArguments.invoke(service, route, exchange, "/users");
            assertThat(args).containsExactly(32);
        }

        @Test
        @DisplayName("branche AUTO type complexe (body)")
        void buildArguments_autoComplexBranch() throws Exception {
            Method method = QueryHeaderAutoController.class.getMethod("autoBody", SamplePayload.class);
            RouteDefinition route = new RouteDefinition(
                    "POST",
                    "/users",
                    new QueryHeaderAutoController(),
                    method,
                    List.of(new RouteParam(ParameterType.AUTO, "payload", SamplePayload.class))
            );

            byte[] body = "{\"name\":\"bob\"}".getBytes(StandardCharsets.UTF_8);
            MockHttpExchange exchange = new MockHttpExchange(
                    "POST",
                    "/users",
                    body,
                    Map.of("Content-Type", "application/json")
            );

            Object[] args = (Object[]) buildArguments.invoke(service, route, exchange, "/users");
            assertThat(args).hasSize(1);
            assertInstanceOf(SamplePayload.class, args[0]);
            assertEquals("bob", ((SamplePayload) args[0]).name);
        }
    }

    // =========================================================================
    // invokeController catch
    // =========================================================================

    @Nested
    @DisplayName("invokeController")
    class InvokeControllerCatchTests {

        @Test
        @DisplayName("exception contrôleur -> HttpErrorException 500")
        void invokeController_whenControllerThrows_mapsTo500() throws Exception {
            Method invokeController = privateMethod("invokeController", RouteDefinition.class, Object[].class);

            Method method = ThrowingController.class.getMethod("alwaysFail");
            RouteDefinition route = new RouteDefinition("GET", "/fail", new ThrowingController(), method, List.of());

            assertHttpErrorStatus(500, () -> invokeController.invoke(service, route, new Object[0]));
        }
    }

    // =========================================================================
    // handle (intégration pipeline complet)
    // =========================================================================

    @Nested
    @DisplayName("handle – pipeline complet")
    class HandleTests {

        private Method handleMethod;

        @BeforeEach
        void setUp() throws Exception {
            handleMethod = privateMethod("handle", HttpExchange.class);

            Method ping = DummyController.class.getMethod("ping");
            Method getById = DummyController.class.getMethod("getById", long.class);
            Method create = DummyController.class.getMethod("create", Map.class);
            Method returnsNull = DummyController.class.getMethod("returnsNull");

            injectRoute("GET", "/ping", ping, List.of());
            injectRoute("GET", "/items/{id}", getById,
                    List.of(new RouteParam(ParameterType.PATH, "id", long.class)));
            injectRoute("POST", "/items", create,
                    List.of(new RouteParam(ParameterType.BODY, null, Map.class)));
            injectRoute("GET", "/null", returnsNull, List.of());
        }

        private void invoke(HttpExchange exchange) throws Exception {
            handleMethod.invoke(service, exchange);
        }

        @Test
        @DisplayName("GET /ping → 200 avec corps JSON")
        void get_returns200WithBody() throws Exception {
            MockHttpExchange exchange = new MockHttpExchange("GET", "/ping", null, null);
            invoke(exchange);
            assertThat(exchange.getCapturedStatus()).isEqualTo(200);
            assertThat(exchange.getCapturedBody()).contains("pong");
        }

        @Test
        @DisplayName("GET /items/{id} → 200 avec valeur du path param")
        void getWithPathParam_returns200() throws Exception {
            MockHttpExchange exchange = new MockHttpExchange("GET", "/items/42", null, null);
            invoke(exchange);
            assertThat(exchange.getCapturedStatus()).isEqualTo(200);
            assertThat(exchange.getCapturedBody()).contains("42");
        }

        @Test
        @DisplayName("POST /items avec body JSON → 201")
        void postWithBody_returns201() throws Exception {
            byte[] body = "{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8);
            MockHttpExchange exchange = new MockHttpExchange(
                    "POST", "/items", body, Map.of("Content-Type", "application/json"));
            invoke(exchange);
            assertThat(exchange.getCapturedStatus()).isEqualTo(201);
        }

        @Test
        @DisplayName("résultat null → 204 No Content")
        void nullResult_returns204() throws Exception {
            MockHttpExchange exchange = new MockHttpExchange("GET", "/null", null, null);
            invoke(exchange);
            assertThat(exchange.getCapturedStatus()).isEqualTo(204);
            assertThat(exchange.getCapturedBody()).isEmpty();
        }

        @Test
        @DisplayName("chemin inconnu → 405 (OPTIONS toujours autorisé)")
        void unknownPath_returns405() throws Exception {
            MockHttpExchange exchange = new MockHttpExchange("GET", "/unknown", null, null);
            invoke(exchange);
            // Note: 405 car withImplicitMethods() ajoute toujours OPTIONS,
            // donc allowedMethods n'est jamais vide et la branche 404 n'est pas atteinte.
            assertThat(exchange.getCapturedStatus()).isEqualTo(405);
        }

        @Test
        @DisplayName("méthode interdite sur chemin connu → 405 avec header Allow")
        void wrongMethod_returns405WithAllow() throws Exception {
            MockHttpExchange exchange = new MockHttpExchange("PUT", "/ping", null, null);
            invoke(exchange);
            assertThat(exchange.getCapturedStatus()).isEqualTo(405);
            String allow = exchange.getResponseHeaders().getFirst("Allow");
            assertThat(allow).isNotNull().contains("GET");
        }

        @Test
        @DisplayName("OPTIONS sur chemin connu → 204 avec header Allow")
        void options_returns204WithAllow() throws Exception {
            MockHttpExchange exchange = new MockHttpExchange("OPTIONS", "/ping", null, null);
            invoke(exchange);
            assertThat(exchange.getCapturedStatus()).isEqualTo(204);
            String allow = exchange.getResponseHeaders().getFirst("Allow");
            assertThat(allow).isNotNull().contains("GET").contains("OPTIONS");
        }

        @Test
        @DisplayName("HEAD /ping → 200 sans corps")
        void head_returns200WithoutBody() throws Exception {
            MockHttpExchange exchange = new MockHttpExchange("HEAD", "/ping", null, null);
            invoke(exchange);
            assertThat(exchange.getCapturedStatus()).isEqualTo(200);
            assertThat(exchange.getCapturedBody()).isEmpty();
        }

        @Test
        @DisplayName("Content-Type non JSON avec body → 415")
        void wrongContentType_returns415() throws Exception {
            byte[] body = "{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8);
            MockHttpExchange exchange = new MockHttpExchange(
                    "POST", "/items", body, Map.of("Content-Type", "text/plain"));
            invoke(exchange);
            assertThat(exchange.getCapturedStatus()).isEqualTo(415);
        }
    }

    // =========================================================================
    // DummyController – contrôleur de test
    // =========================================================================

    static class DummyController {

        public String ping() {
            return "pong";
        }

        public long getById(long id) {
            return id;
        }

        @Status(201)
        @SuppressWarnings("unused")
        public String create(@Body Map<String, Object> body) {
            return "created";
        }

        public Object returnsNull() {
            return null;
        }
    }

    // =========================================================================
    // ParamTestController – pour les tests d'analyzeParameters
    // =========================================================================

    @SuppressWarnings("unused")
    static class ParamTestController {

        public void pathParam(@Path("id") long id) {}

        public void queryParam(@Query("name") String name) {}

        public void headerParam(@Header("Authorization") String auth) {}

        public void bodyParam(@Body Map<String, Object> body) {}

        public void autoParam(String x) {}

        public void multipleParams(@Path("id") long id, @Query("name") String name) {}
    }

    @Controller("/base")
    static class AnnotatedController {

        @Get("/items/{id}")
        public String getItem(@Path("id") long id) {
            return "item-" + id;
        }
    }

    static class NonAnnotatedController {
        public void noHttpAnnotation() {}
    }

    static class VerbAnnotatedController {

        @Get("/x")
        public void get() {}

        @Post("/x")
        public void post() {}

        @Put("/x")
        public void put() {}

        @Delete("/x")
        public void delete() {}

        @Patch("/x")
        public void patch() {}

        @Head("/x")
        public void head() {}

        @Options("/x")
        public void options() {}
    }

    @SuppressWarnings("unused")
    static class QueryHeaderAutoController {

        public String queryOnly(@Query("name") String name) {
            return name;
        }

        public String headerOnly(@Header("Authorization") String authorization) {
            return authorization;
        }

        public int autoInt(int age) {
            return age;
        }

        public SamplePayload autoBody(SamplePayload payload) {
            return payload;
        }
    }

    static class ThrowingController {
        public String alwaysFail() {
            throw new IllegalStateException("boom");
        }
    }

    static class SamplePayload {
        public String name;
    }

    // =========================================================================
    // MockHttpExchange – simulation de HttpExchange sans serveur réel
    // =========================================================================

    static class MockHttpExchange extends HttpExchange {

        private final String requestMethod;
        private final URI requestURI;
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();
        private final InputStream requestBodyStream;

        private int capturedStatus = -1;
        private final ByteArrayOutputStream responseBodyCapture = new ByteArrayOutputStream();

        MockHttpExchange(String method,
                         String path,
                         byte[] body,
                         Map<String, String> headers) throws URISyntaxException {
            this.requestMethod = method;
            this.requestURI = new URI(path);
            this.requestBodyStream = body != null
                    ? new ByteArrayInputStream(body)
                    : new ByteArrayInputStream(new byte[0]);
            if (headers != null) {
                headers.forEach(requestHeaders::add);
            }
        }

        // ---- Méthodes capturées ----

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) {
            this.capturedStatus = rCode;
        }

        @Override
        public OutputStream getResponseBody() {
            return responseBodyCapture;
        }

        @Override
        public InputStream getRequestBody() {
            return requestBodyStream;
        }

        // ---- Accesseurs essentiels ----

        @Override
        public Headers getRequestHeaders() {
            return requestHeaders;
        }

        @Override
        public Headers getResponseHeaders() {
            return responseHeaders;
        }

        @Override
        public URI getRequestURI() {
            return requestURI;
        }

        @Override
        public String getRequestMethod() {
            return requestMethod;
        }

        @Override
        public int getResponseCode() {
            return capturedStatus;
        }

        // ---- Méthodes abstraites sans implémentation nécessaire ----

        @Override
        public HttpContext getHttpContext() {
            return null;
        }

        @Override
        public void close() {}

        @Override
        public InetSocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return null;
        }

        @Override
        public String getProtocol() {
            return "HTTP/1.1";
        }

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @Override
        public void setAttribute(String name, Object value) {}

        @Override
        public void setStreams(InputStream i, OutputStream o) {}

        @Override
        public HttpPrincipal getPrincipal() {
            return null;
        }

        // ---- Helpers de vérification ----

        int getCapturedStatus() {
            return capturedStatus;
        }

        String getCapturedBody() {
            return responseBodyCapture.toString(StandardCharsets.UTF_8);
        }
    }
}
