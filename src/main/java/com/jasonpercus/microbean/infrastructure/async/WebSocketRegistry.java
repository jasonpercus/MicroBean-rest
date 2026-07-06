package com.jasonpercus.microbean.infrastructure.async;

/*
 * Copyright (c) 2026 JasonPercus
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for more information.
 */

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.websocket.Session;

public class WebSocketRegistry {

    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public void add(String jobId, Session session) {
        sessions.put(jobId, session);
    }

    public void remove(String jobId) {
        sessions.remove(jobId);
    }

    public void send(String jobId, String message) throws IOException {
        Session session = sessions.get(jobId);

        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
            session.close();
            remove(jobId);
        }
    }
}
