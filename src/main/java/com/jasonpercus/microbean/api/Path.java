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
 * Marque un paramètre de méthode comme provenant d'une variable de chemin URL.
 *
 * <p>Le nom fourni doit correspondre à une variable déclarée dans la route
 * (ex: {@code /users/{id}}). La valeur extraite est ensuite convertie vers
 * le type du paramètre ciblé.</p>
 *
 * <p>Exemple :</p>
 * <pre>{@code
 * @Get("/{id}")
 * public User findById(@Path("id") long id) {
 *     return null;
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Path {

    /**
     * Définit le nom de la variable de chemin à lire.
     *
     * @return le nom de la variable de chemin.
     */
    String value();
}
