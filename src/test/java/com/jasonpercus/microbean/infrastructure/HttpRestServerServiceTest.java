package com.jasonpercus.microbean.infrastructure;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import com.jasonpercus.microbean.entrypoint.HttpRestServerService;

class HttpRestServerServiceTest {

    public static void main(String[] args) throws Exception {
        shouldParseQuerySafelyAndDecodeValues();
        shouldRejectInvalidBooleanValues();
        shouldRejectMissingPrimitiveParameter();
        System.out.println("RestServerServiceTest: OK");
    }

    @SuppressWarnings("unchecked")
    private static void shouldParseQuerySafelyAndDecodeValues() throws Exception {
        HttpRestServerService service = new HttpRestServerService();
        Method parseQuery = HttpRestServerService.class.getDeclaredMethod("parseQuery", String.class);
        parseQuery.setAccessible(true);

        Map<String, List<String>> query = (Map<String, List<String>>) parseQuery.invoke(service, "name=John+Doe&token=a=b=c&empty");

        check("John Doe".equals(query.get("name").get(0)), "name should be URL decoded");
        check("a=b=c".equals(query.get("token").get(0)), "token should preserve '=' characters");
        check("".equals(query.get("empty").get(0)), "empty key should produce empty value");
    }

    private static void shouldRejectInvalidBooleanValues() throws Exception {
        HttpRestServerService service = new HttpRestServerService();
        Method convert = HttpRestServerService.class.getDeclaredMethod("convert", String.class, Class.class, String.class);
        convert.setAccessible(true);

        InvocationTargetException exception = expectInvocationTargetException(
                () -> convert.invoke(service, "not-boolean", boolean.class, "active"));

        check(exception.getTargetException() instanceof RuntimeException, "Expected runtime exception");
        check("Invalid value for parameter: active".equals(exception.getTargetException().getMessage()),
                "Expected invalid parameter message for boolean conversion");
    }

    private static void shouldRejectMissingPrimitiveParameter() throws Exception {
        HttpRestServerService service = new HttpRestServerService();
        Method convert = HttpRestServerService.class.getDeclaredMethod("convert", String.class, Class.class, String.class);
        convert.setAccessible(true);

        InvocationTargetException exception = expectInvocationTargetException(
                () -> convert.invoke(service, null, long.class, "id"));

        check("Missing required parameter: id".equals(exception.getTargetException().getMessage()),
                "Expected missing parameter message for primitive type");
    }

    private static InvocationTargetException expectInvocationTargetException(ThrowingRunnable runnable) throws Exception {
        try {
            runnable.run();
            throw new IllegalStateException("Expected InvocationTargetException");
        } catch (InvocationTargetException exception) {
            return exception;
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}


