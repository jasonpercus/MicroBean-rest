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
 * Marque un paramètre de méthode comme provenant du corps de la requête HTTP.
 *
 * <p>L'infrastructure REST utilise cette annotation pour désérialiser le body
 * (généralement JSON) vers le type du paramètre ciblé.</p>
 *
 * <p>Exemple :</p>
 * <pre>{@code
 * @Post
 * public User create(@Body User user) {
 *     return user;
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Body {

}
