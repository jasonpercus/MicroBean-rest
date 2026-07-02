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
 * Définit explicitement le code de statut HTTP d'une réponse de méthode controller.
 *
 * <p>Lorsqu'elle est présente, cette annotation surcharge le statut par défaut
 * utilisé par l'infrastructure REST pour la méthode ciblée.</p>
 *
 * <p>Exemple :</p>
 * <pre>{@code
 * @Post
 * @Status(201)
 * public User create(@Body User user) {
 *     return user;
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Status {

    /**
     * Définit le code HTTP à retourner (ex: 200, 201, 204, 400, 404).
     *
     * @return le code de statut HTTP.
     */
    int value();
}
