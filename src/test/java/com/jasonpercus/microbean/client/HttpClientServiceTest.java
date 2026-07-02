package com.jasonpercus.microbean.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("HttpClientService")
class HttpClientServiceTest {

    private static HttpServer server;
    private static String baseUrl;

    @BeforeAll
    static void beforeAll() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/ok", exchange -> {
            byte[] body = "{\"value\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(body);
            }
        });

        server.createContext("/echo", exchange -> {
            byte[] input = exchange.getRequestBody().readAllBytes();
            if (input.length == 0) {
                input = "{}".getBytes(StandardCharsets.UTF_8);
            }
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, input.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(input);
            }
        });

        server.createContext("/text", exchange -> {
            byte[] body = "plain text".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(body);
            }
        });

        server.createContext("/invalid-json", exchange -> {
            byte[] body = "not-json".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(body);
            }
        });

        server.createContext("/status/404", exchange -> {
            byte[] body = "{\"error\":\"not found\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(404, body.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(body);
            }
        });

        server.createContext("/inspect", HttpClientServiceTest::handleInspect);

        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterAll
    static void afterAll() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static void handleInspect(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getRawQuery();
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        String method = exchange.getRequestMethod();

        String body = "{\"method\":\"" + method + "\",\"query\":\"" + (query == null ? "" : query) +
                "\",\"auth\":\"" + (auth == null ? "" : auth) + "\"}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private HttpClientService client() {
        return new HttpClientService(baseUrl);
    }

    private Object privateField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private Object privateMethod(Object target, String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    @SuppressWarnings("unchecked")
    private HttpClientService.HttpResponse<Map<String, Object>> executeAsMap(HttpClientService.HttpRequestBuilder builder) throws Exception {
        return (HttpClientService.HttpResponse<Map<String, Object>>) (HttpClientService.HttpResponse<?>) builder.execute(Map.class);
    }

    @Nested
    class ConstructionTests {

        @Test
        void normalizeBaseUrl_nullFallsBackToLocalhost() throws Exception {
            HttpClientService c = new HttpClientService(null);
            assertEquals("http://localhost", privateField(c, "baseUrl"));
        }

        @Test
        void normalizeBaseUrl_addsProtocolAndTrimsTrailingSlash() throws Exception {
            HttpClientService c = new HttpClientService("localhost:8080/");
            assertEquals("http://localhost:8080", privateField(c, "baseUrl"));
        }

        @Test
        void httpVerbFactories_returnBuilder() {
            HttpClientService c = client();
            assertNotNull(c.get("/a"));
            assertNotNull(c.post("/a"));
            assertNotNull(c.put("/a"));
            assertNotNull(c.delete("/a"));
            assertNotNull(c.patch("/a"));
            assertNotNull(c.head("/a"));
            assertNotNull(c.options("/a"));
        }
    }

    @Nested
    class BuilderStateTests {

        @Test
        void fluentMethods_mutateStateAndReturnSameBuilder() throws Exception {
            HttpClientService.HttpRequestBuilder builder = client().post("users/{id}/")
                    .pathParam("id", 42)
                    .queryParam("name", "Alice")
                    .header("Authorization", "Bearer abc")
                    .body(Map.of("k", "v"))
                    .timeout(3210);

            assertSame(builder, builder.pathParam("x", "y"));
            assertSame(builder, builder.queryParam("q", "z"));
            assertSame(builder, builder.header("h", "v"));
            assertSame(builder, builder.body(Map.of()));
            assertSame(builder, builder.timeout(5000));

            assertEquals("POST", privateField(builder, "method"));
            assertEquals("/users/{id}", privateField(builder, "path"));
            assertEquals(5000, privateField(builder, "connectTimeout"));
            assertEquals(5000, privateField(builder, "readTimeout"));

            @SuppressWarnings("unchecked")
            Map<String, String> pathParams = (Map<String, String>) privateField(builder, "pathParams");
            @SuppressWarnings("unchecked")
            Map<String, String> queryParams = (Map<String, String>) privateField(builder, "queryParams");
            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) privateField(builder, "headers");

            assertThat(pathParams).containsEntry("id", "42");
            assertThat(queryParams).containsEntry("name", "Alice");
            assertThat(headers).containsEntry("Authorization", "Bearer abc");
            assertNotNull(privateField(builder, "body"));
        }

        @Test
        void queryParam_nullValue_isIgnored() throws Exception {
            HttpClientService.HttpRequestBuilder builder = client().get("/a").queryParam("x", null);
            @SuppressWarnings("unchecked")
            Map<String, String> queryParams = (Map<String, String>) privateField(builder, "queryParams");
            assertThat(queryParams).isEmpty();
        }

        @Test
        void pathParam_nullValue_becomesEmptyString() throws Exception {
            HttpClientService.HttpRequestBuilder builder = client().get("/a/{id}").pathParam("id", null);
            @SuppressWarnings("unchecked")
            Map<String, String> pathParams = (Map<String, String>) privateField(builder, "pathParams");
            assertThat(pathParams).containsEntry("id", "");
        }
    }

    @Nested
    class PrivateHelpersTests {

        @Test
        void buildUrl_replacesPathParamsAndAppendsQuery() throws Exception {
            HttpClientService.HttpRequestBuilder builder = client().get("/users/{id}")
                    .pathParam("id", "john doe")
                    .queryParam("q", "a&b");

            String url = (String) privateMethod(builder, "buildUrl", new Class[]{}, new Object[]{});
            assertThat(url).contains("/users/john+doe");
            assertThat(url).contains("q=a%26b");
        }

        @Test
        void buildQueryString_encodesPairs() throws Exception {
            HttpClientService.HttpRequestBuilder builder = client().get("/x");
            Map<String, String> params = Map.of("name", "John Doe", "token", "a=b");
            String query = (String) privateMethod(builder,
                    "buildQueryString",
                    new Class[]{Map.class},
                    new Object[]{params});

            assertThat(query).contains("name=John+Doe");
            assertThat(query).contains("token=a%3Db");
        }

        @Test
        void buildPathWithParams_replacesPlaceholder() throws Exception {
            HttpClientService.HttpRequestBuilder builder = client().get("/x");
            String path = (String) privateMethod(builder,
                    "buildPathWithParams",
                    new Class[]{String.class, Map.class},
                    new Object[]{"/items/{id}", Map.of("id", "A B")});
            assertEquals("/items/A+B", path);
        }

        @Test
        void normalizePath_rulesApplied() throws Exception {
            HttpClientService.HttpRequestBuilder b = client().get("x");
            String root = (String) privateMethod(b, "normalizePath", new Class[]{String.class}, new Object[]{null});
            String noLeading = (String) privateMethod(b, "normalizePath", new Class[]{String.class}, new Object[]{"users"});
            String trailing = (String) privateMethod(b, "normalizePath", new Class[]{String.class}, new Object[]{"/users/"});

            assertEquals("/", root);
            assertEquals("/users", noLeading);
            assertEquals("/users", trailing);
        }
    }

    @Nested
    class ExecuteIntegrationTests {

        @Test
        void execute_getJson_successAndDataParsed() throws Exception {
            HttpClientService.HttpResponse<Map<String, Object>> response = executeAsMap(client().get("/ok"));

            assertEquals(200, response.getStatus());
            assertThat(response.getContentType()).contains("application/json");
            assertThat(response.getData()).containsEntry("value", "ok");
            assertTrue(response.isSuccess());
            assertFalse(response.isClientError());
            assertFalse(response.isServerError());
        }

        @Test
        void execute_postJson_setsContentTypeAndBodySent() throws Exception {
            Map<String, Object> payload = Map.of("id", 1, "name", "bob");

            HttpClientService.HttpResponse<Map<String, Object>> response = executeAsMap(client()
                    .post("/echo")
                    .header("Authorization", "Bearer token")
                    .body(payload));

            assertEquals(200, response.getStatus());
            assertThat(response.getData()).containsEntry("name", "bob");
        }

        @Test
        void execute_putAndPatch_coverWritableVerbs() throws Exception {
            HttpClientService.HttpResponse<Map<String, Object>> put = executeAsMap(client().put("/echo").body(Map.of("x", 1)));
            HttpClientService.HttpResponse<Map<String, Object>> patch = executeAsMap(client().patch("/echo").body(Map.of("y", 2)));

            assertEquals(200, put.getStatus());
            assertEquals(200, patch.getStatus());
            assertThat(put.getData()).containsEntry("x", 1);
            assertThat(patch.getData()).containsEntry("y", 2);
        }

        @Test
        void execute_headAndOptions_covered() throws Exception {
            HttpClientService.HttpResponse<Void> head = client().head("/ok").execute();
            HttpClientService.HttpResponse<Void> options = client().options("/ok").execute();

            assertTrue(head.getStatus() >= 200);
            assertTrue(options.getStatus() >= 200);
        }

        @Test
        void execute_nonJsonResponse_dataStaysNull() throws Exception {
            HttpClientService.HttpResponse<Map<String, Object>> response = executeAsMap(client().get("/text"));

            assertEquals(200, response.getStatus());
            assertNull(response.getData());
            assertThat(response.getBodyAsString()).isEqualTo("plain text");
        }

        @Test
        void execute_invalidJson_dataStaysNullButBodyAvailable() throws Exception {
            HttpClientService.HttpResponse<Map<String, Object>> response = executeAsMap(client().get("/invalid-json"));

            assertEquals(200, response.getStatus());
            assertNull(response.getData());
            assertThat(response.getBodyAsString()).isEqualTo("not-json");
        }

        @Test
        void execute_404_marksClientError() throws Exception {
            HttpClientService.HttpResponse<Map<String, Object>> response = executeAsMap(client().get("/status/404"));

            assertEquals(404, response.getStatus());
            assertFalse(response.isSuccess());
            assertTrue(response.isClientError());
            assertFalse(response.isServerError());
            assertThat(response.getBodyAsString()).contains("not found");
        }

        @Test
        void execute_withPathAndQueryAndHeader_allApplied() throws Exception {
            HttpClientService.HttpResponse<Map<String, Object>> response = executeAsMap(client()
                    .get("/inspect/{id}")
                    .pathParam("id", "42")
                    .queryParam("q", "hello world")
                    .header("Authorization", "Bearer test"));

            assertEquals(200, response.getStatus());
            assertThat(response.getData()).containsEntry("method", "GET");
            assertThat(response.getData().get("query").toString()).contains("q=hello+world");
            assertThat(response.getData()).containsEntry("auth", "Bearer test");
        }
    }

    @Nested
    class HttpResponseTests {

        @Test
        void httpResponse_gettersAndHelpersCovered() {
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("X-Test", List.of("ok"));
            byte[] body = "hello".getBytes(StandardCharsets.UTF_8);

            HttpClientService.HttpResponse<String> response =
                    new HttpClientService.HttpResponse<>(500, "application/json", headers, body, "DATA");

            assertEquals(500, response.getStatus());
            assertEquals("application/json", response.getContentType());
            assertSame(headers, response.getHeaders());
            assertEquals("hello", response.getBodyAsString());
            assertThat(response.getRawBody()).isEqualTo(body);
            assertEquals("DATA", response.getData());

            assertFalse(response.isSuccess());
            assertFalse(response.isClientError());
            assertTrue(response.isServerError());
            assertThat(response.toString()).contains("status=500");
        }
    }
}




