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
 * Marque une classe comme controller REST MicroBean.
 *
 * <p>Cette annotation est une spécialisation de {@link Service} et permet de
 * déclarer un bean applicatif exposant des routes HTTP via les annotations
 * de méthode ({@code @Get}, {@code @Post}, etc.).</p>
 *
 * <p>Elle est retenue à l'exécution ({@link RetentionPolicy#RUNTIME}) afin
 * d'être détectée pendant le scan du contexte IoC.</p>
 *
 * <p>Exemple :</p>
 * <pre>{@code
 * @Controller("/users")
 * public class UserController {
 * }
 * }</pre>
 */
@Service
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {

    /**
     * Définit un nom explicite pour le bean controller.
     *
     * <p>Si vide, le nom peut être déduit par le conteneur selon sa stratégie
     * interne de nommage.</p>
     *
     * @return le nom du bean controller ; chaîne vide par défaut.
     */
    String name() default "";

    /**
     * Définit le chemin de base du controller.
     *
     * <p>Ce chemin est préfixé à toutes les routes déclarées sur les méthodes
     * de la classe.</p>
     *
     * @return le chemin de base ; chaîne vide par défaut.
     */
    String value() default "";
}
