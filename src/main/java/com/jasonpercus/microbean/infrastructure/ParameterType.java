package com.jasonpercus.microbean.infrastructure;

/*
 * Copyright (c) 2026 JasonPercus
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for more information.
 */

/**
 * Enumération représentant les types de paramètres applicatifs.
 * <p>
 * Cette énumération est utilisée pour identifier les différents types de
 * paramètres qui peuvent être passés à l'application MicroBean.
 * </p>
 */
public enum ParameterType {

    /**
     * Valeur extraite de l'URL : /users/{id}
     */
    PATH,

    /**
     * Paramètre de query string : ?name=foo
     */
    QUERY,

    /**
     * Header HTTP : Authorization, Content-Type, etc.
     */
    HEADER,

    /**
     * Corps de requête JSON (désérialisation via ObjectMapper)
     */
    BODY,

    /**
     * Fallback automatique (inférence basée sur le type ou le nom)
     */
    AUTO
}
