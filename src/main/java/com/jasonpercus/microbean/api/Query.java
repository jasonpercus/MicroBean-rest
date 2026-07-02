package com.jasonpercus.microbean.api;

/*
 * Copyright (c) 2026 JasonPercus
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for more information.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marque un paramètre de méthode comme provenant de la query string HTTP.
 *
 * <p>Le nom fourni est recherché dans l'URL de la requête (ex: {@code ?name=alice})
 * puis la valeur est convertie vers le type du paramètre cible.</p>
 *
 * <p>Exemple :</p>
 * <pre>{@code
 * @Get
 * public List<User> find(@Query("name") String name) {
 *     return List.of();
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    /**
     * Définit le nom du paramètre de query à lire.
     *
     * @return le nom du paramètre dans la query string.
     */
    String value();
}
