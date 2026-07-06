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
 * Indique que la méthode annotée est un travail asynchrone qui peut être exécuté en arrière-plan.
 * Cette annotation est utilisée pour marquer les méthodes de contrôleur qui déclenchent des tâches longues ou intensives en ressources,
 * permettant ainsi à l'infrastructure de gérer ces tâches de manière asynchrone.
 * HttpRestServerService pourra ainsi lancer un server WebSocket pour notifier le client de l'avancement du travail.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AsyncJob {

}
