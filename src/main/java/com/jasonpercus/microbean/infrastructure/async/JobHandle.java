package com.jasonpercus.microbean.infrastructure.async;

/*
 * Copyright (c) 2026 JasonPercus
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for more information.
 */

import java.util.UUID;

public record JobHandle(
        UUID id,
        String wsUrl
) {}
