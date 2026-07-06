package com.jasonpercus.microbean.infrastructure.async;

/*
 * Copyright (c) 2026 JasonPercus
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for more information.
 */

import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/jobs/{jobId}")
public class JobWebSocketEndpoint {

    private static AsyncJobManager jobManager;

    public static void setJobManager(AsyncJobManager manager) {
        jobManager = manager;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("jobId") String jobId) {
        jobManager.getWsRegistry().add(jobId, session);
    }

    @OnClose
    public void onClose(@PathParam("jobId") String jobId) {
        jobManager.getWsRegistry().remove(jobId);
    }
}
