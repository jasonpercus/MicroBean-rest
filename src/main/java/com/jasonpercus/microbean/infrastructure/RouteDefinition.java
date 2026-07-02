package com.jasonpercus.microbean.infrastructure;

/*
 * Copyright (c) 2026 JasonPercus
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for more information.
 */

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Représente une route HTTP résolue à partir d'une méthode de controller.
 *
 * <p>Cette classe encapsule les informations nécessaires au routage :</p>
 * <ul>
 *   <li>la méthode HTTP (GET, POST, etc.) ;</li>
 *   <li>le pattern de chemin ;</li>
 *   <li>l'instance de controller et sa méthode cible ;</li>
 *   <li>la liste des paramètres attendus ;</li>
 *   <li>des métadonnées optimisées pour le matching de chemin.</li>
 * </ul>
 *
 * <p>Elle est immuable après construction.</p>
 */
public final class RouteDefinition {

    /** Séparateur de segments dans un chemin HTTP. */
    public static final String SEPARATOR = "/";
    /** Méthode HTTP associée à la route. */
    private final String httpMethod;
    /** Pattern de chemin normalisé (ex: {@code /users/{id}}). */
    private final String pathPattern;
    /** Instance controller qui portera l'invocation. */
    private final Object controllerInstance;
    /** Méthode Java à appeler quand la route est matchée. */
    private final Method method;
    /** Paramètres de route analysés depuis la signature de méthode. */
    private final List<RouteParam> params;
    /** Score de spécificité de la route (plus haut = plus spécifique). */
    private final int score;
    /** Segments normalisés du pattern de chemin. */
    private final List<String> patternSegments;
    /** Index des variables de chemin par nom. */
    private final Map<String, Integer> pathIndexes;

    /**
     * Construit une définition de route complète.
     *
     * @param httpMethod méthode HTTP associée.
     * @param pathPattern pattern de chemin de la route.
     * @param controllerInstance instance controller cible.
     * @param method méthode Java cible.
     * @param params liste des paramètres attendus.
     */
    public RouteDefinition(String httpMethod,
                           String pathPattern,
                           Object controllerInstance,
                           Method method,
                           List<RouteParam> params) {

        this.httpMethod = Objects.requireNonNull(httpMethod, "❌ httpMethod cannot be null");
        this.pathPattern = normalizePath(pathPattern);
        this.controllerInstance = Objects.requireNonNull(controllerInstance, "❌ controllerInstance cannot be null");
        this.method = Objects.requireNonNull(method, "❌ method cannot be null");
        this.params = List.copyOf(Objects.requireNonNull(params, "❌ params cannot be null"));
        this.patternSegments = splitSegments(this.pathPattern);
        this.pathIndexes = Collections.unmodifiableMap(buildPathIndexes(this.patternSegments));
        this.score = computeScore(this.patternSegments);
    }

    /** @return méthode HTTP de la route. */
    public String getHttpMethod() {
        return httpMethod;
    }

    /** @return pattern de chemin normalisé. */
    public String getPathPattern() {
        return pathPattern;
    }

    /** @return instance controller cible. */
    public Object getControllerInstance() {
        return controllerInstance;
    }

    /** @return méthode Java associée. */
    public Method getMethod() {
        return method;
    }

    /** @return paramètres de route analysés. */
    public List<RouteParam> getParams() {
        return params;
    }

    /** @return score de spécificité de la route. */
    public int getScore() {
        return score;
    }

    /** @return segments normalisés du pattern de route. */
    public List<String> getPatternSegments() {
        return patternSegments;
    }

    /** @return index des variables de chemin par nom. */
    public Map<String, Integer> getPathIndexes() {
        return pathIndexes;
    }

    /**
     * Vérifie si un chemin entrant correspond au pattern de la route.
     *
     * <p>Les segments variables au format {@code {name}} sont considérés comme
     * jokers et acceptent n'importe quelle valeur non vide.</p>
     *
     * @param requestPath chemin de requête entrant.
     * @return {@code true} si le chemin correspond au pattern ; sinon {@code false}.
     */
    public boolean matchesPath(String requestPath) {
        
        List<String> incoming = splitSegments(requestPath);

        if (incoming.size() != patternSegments.size())
            return false;

        for (int i = 0; i < incoming.size(); i++) {
            String expected = patternSegments.get(i);

            if (isPathVariable(expected))
                continue;

            if (!expected.equals(incoming.get(i)))
                return false;
        }

        return true;
    }

    /**
     * Extrait la valeur d'un paramètre de chemin depuis une URL entrante.
     *
     * @param name nom de la variable de chemin (ex: {@code id}).
     * @param requestPath chemin de requête entrant.
     * @return valeur extraite, ou {@code null} si la variable est absente.
     */
    public String extractPathParam(String name, String requestPath) {
        
        Integer index = pathIndexes.get(name);

        if (index == null)
            return null;

        List<String> incoming = splitSegments(requestPath);
        if (index >= incoming.size())
            return null;

        return incoming.get(index);
    }

    /**
     * Construit un index nom -> position pour les variables de chemin.
     *
     * @param segments segments normalisés de la route.
     * @return map des index de variables.
     */
    private static Map<String, Integer> buildPathIndexes(List<String> segments) {
        
        Map<String, Integer> indexes = new LinkedHashMap<>();

        for (int i = 0; i < segments.size(); i++) {
            String segment = segments.get(i);
            
            if (!isPathVariable(segment))
                continue;

            String name = segment.substring(1, segment.length() - 1);
            
            indexes.put(name, i);
        }

        return indexes;
    }

    /**
     * Calcule un score de spécificité de route.
     *
     * <p>Un segment fixe vaut 10 points, un segment variable vaut 1 point.</p>
     *
     * @param segments segments de route.
     * @return score global de spécificité.
     */
    private static int computeScore(List<String> segments) {
        
        int value = 0;
        
        for (String segment : segments)
            value += isPathVariable(segment) ? 1 : 10;
            
        return value;
    }

    /**
     * Indique si un segment est une variable de chemin.
     *
     * @param segment segment à tester.
     * @return {@code true} si le segment est du type {@code {name}}.
     */
    private static boolean isPathVariable(String segment) {
        return segment.startsWith("{") && segment.endsWith("}");
    }

    /**
     * Découpe un chemin en segments normalisés, sans segments vides.
     *
     * @param path chemin à découper.
     * @return liste immuable des segments.
     */
    private static List<String> splitSegments(String path) {
        
        String normalized = normalizePath(path);
        
        if (SEPARATOR.equals(normalized))
            return List.of();

        String[] split = normalized.substring(1).split(SEPARATOR);
        List<String> segments = new ArrayList<>(split.length);
        
        for (String segment : split) {
            
            if (!segment.isBlank())
                segments.add(segment);
        }
        
        return List.copyOf(segments);
    }

    /**
     * Normalise un chemin HTTP.
     *
     * <p>Règles :</p>
     * <ul>
     *   <li>si vide ou null -> {@code /}</li>
     *   <li>force un slash initial</li>
     *   <li>retire le slash terminal (sauf racine)</li>
     * </ul>
     *
     * @param path chemin brut.
     * @return chemin normalisé.
     */
    private static String normalizePath(String path) {
        
        if (path == null || path.isBlank())
            return SEPARATOR;

        String normalized = path.startsWith(SEPARATOR) ? path : SEPARATOR + path;
        
        if (normalized.length() > 1 && normalized.endsWith(SEPARATOR))
            normalized = normalized.substring(0, normalized.length() - 1);

        return normalized;
    }
}
