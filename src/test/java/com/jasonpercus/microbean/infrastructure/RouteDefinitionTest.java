package com.jasonpercus.microbean.infrastructure;

import java.lang.reflect.Method;
import java.util.List;

class RouteDefinitionTest {

    public static void main(String[] args) throws Exception {
        shouldMatchStaticAndDynamicSegments();
        shouldGiveHigherScoreToStaticRoutes();
        System.out.println("RouteDefinitionTest: OK");
    }

    private static void shouldMatchStaticAndDynamicSegments() throws Exception {
        Method method = DummyController.class.getDeclaredMethod("getUser", long.class);

        RouteDefinition route = new RouteDefinition(
                "GET",
                "/users/{id}",
                new DummyController(),
                method,
                List.of(new RouteParam(ParameterType.PATH, "id", long.class)));

        check(route.matchesPath("/users/42"), "Path '/users/42' should match");
        check(route.matchesPath("/users/42/"), "Path '/users/42/' should match");
        check(!route.matchesPath("/users"), "Path '/users' should not match");
        check(!route.matchesPath("/users/42/profile"), "Path '/users/42/profile' should not match");
        check("42".equals(route.extractPathParam("id", "/users/42")), "Path variable 'id' should be extracted");
        check(route.extractPathParam("unknown", "/users/42") == null, "Unknown path variable should be null");
    }

    private static void shouldGiveHigherScoreToStaticRoutes() throws Exception {
        Method method = DummyController.class.getDeclaredMethod("getUser", long.class);

        RouteDefinition staticRoute = new RouteDefinition("GET", "/users/all", new DummyController(), method, List.of());
        RouteDefinition dynamicRoute = new RouteDefinition("GET", "/users/{id}", new DummyController(), method, List.of());

        check(staticRoute.getScore() > dynamicRoute.getScore(), "Static route score should be higher than dynamic route score");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static class DummyController {

        @SuppressWarnings("unused")
        public Object getUser(long id) {
            return id;
        }
    }
}


