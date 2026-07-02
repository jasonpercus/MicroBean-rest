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
 * Marque un paramètre de méthode comme provenant d'un header HTTP.
 *
 * <p>La valeur du header est lue à partir de la requête entrante puis convertie
 * vers le type du paramètre cible selon les règles de conversion du serveur.</p>
 *
 * <p>Exemple :</p>
 * <pre>{@code
 * @Get("/secure")
 * public String secure(@Header("Authorization") String authorization) {
 *     return "ok";
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Header {

    /**
     * Définit le nom du header à lire dans la requête HTTP.
     *
     * @return le nom du header attendu.
     */
    String value();
}
