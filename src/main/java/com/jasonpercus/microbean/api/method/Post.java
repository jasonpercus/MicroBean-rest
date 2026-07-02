package com.jasonpercus.microbean.api.method;

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
 * Marque une méthode de controller comme gestionnaire d'une requête HTTP {@code POST}.
 *
 * <p>L'annotation est retenue à l'exécution ({@link RetentionPolicy#RUNTIME}) afin
 * d'être lue par l'infrastructure REST MicroBean lors du scan des controllers.</p>
 *
 * <p>Le chemin peut être défini via {@link #value()} :</p>
 * <ul>
 *   <li>vide ({@code ""}) : la méthode répond sur le chemin de base du controller ;</li>
 *   <li>non vide (ex: {@code "/create"}, {@code "/{id}"}) : sous-chemin relatif au controller.</li>
 * </ul>
 *
 * <p>Exemple :</p>
 * <pre>{@code
 * @Controller("/users")
 * public class UserController {
 *
 *     @Post
 *     public User create(@Body User user) {
 *         return user;
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Post {

    /**
     * Définit le chemin relatif associé à la route HTTP {@code POST}.
     *
     * <p>La valeur est concaténée avec le chemin de base défini sur l'annotation
     * {@code @Controller} de la classe.</p>
     *
     * @return le chemin relatif de la route ; chaîne vide par défaut.
     */
    String value() default "";
}
