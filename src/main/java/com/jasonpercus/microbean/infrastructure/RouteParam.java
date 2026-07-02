package com.jasonpercus.microbean.infrastructure;

/*
 * Copyright (c) 2026 JasonPercus
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for more information.
 */

import java.util.Objects;

/**
 * Représente un paramètre attendu par une route HTTP.
 *
 * <p>Un paramètre contient :</p>
 * <ul>
 *   <li>son {@link ParameterType} (PATH, QUERY, HEADER, BODY, AUTO) ;</li>
 *   <li>son nom logique (peut être {@code null} selon le type) ;</li>
 *   <li>sa classe cible pour la conversion.</li>
 * </ul>
 *
 * @param type type de paramètre HTTP.
 * @param name nom logique du paramètre (ex: {@code id}, {@code name}).
 * @param clazz type Java cible.
 */
public record RouteParam(ParameterType type, String name, Class<?> clazz) {

    /**
     * Valide les composants obligatoires du record.
     *
     * @throws NullPointerException si {@code type} ou {@code clazz} est null.
     */
    public RouteParam {
        Objects.requireNonNull(type, "❌ type cannot be null");
        Objects.requireNonNull(clazz, "❌ clazz cannot be null");
    }
}
