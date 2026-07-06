package com.jasonpercus.microbean.infrastructure.async;

/*
 * Copyright (c) 2026 JasonPercus
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for more information.
 */

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonpercus.microbean.api.ResponseEntity;

public class AsyncJobManager {

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<UUID, JobHolder> jobs = new ConcurrentHashMap<>();

    private final WebSocketRegistry wsRegistry = new WebSocketRegistry();
    private final ObjectMapper mapper = new ObjectMapper();

    private final boolean useWebSocket =
            Boolean.parseBoolean(System.getenv().getOrDefault("USE_WEBSOCKET_ASYNC_REQUEST", "true"));

    public AsyncJobManager(AtomicInteger timeSecCleanPeriod, ExecutorService executor) {
        this.executor = executor;

        int period = timeSecCleanPeriod.get();

        // nettoyage toutes les 30 secondes
        scheduler.scheduleAtFixedRate(
                this::clearJobs,
                period,
                period,
                TimeUnit.SECONDS
        );
    }

    public void shutdown() {
        scheduler.shutdown();
        executor.shutdown();
    }

    // =========================
    // START JOB (GENERIC)
    // =========================
    public <R> ResponseEntity<JobHandle> startJob(Supplier<R> task) {

        UUID id = UUID.randomUUID();

        JobHolder job = new JobHolder();
        jobs.put(id, job);

        String wsUrl = useWebSocket ? "/ws/jobs/" + id : null;

        Future<?> future = executor.submit(() -> {
            try {
                job.setStatus(JobStatus.RUNNING);
                notify(id, job);

                R result = task.get();

                job.setResult(result);
                job.setStatus(JobStatus.DONE);
                job.setCompletedAt(System.currentTimeMillis());

                notify(id, job);

            } catch (Exception e) {
                job.setError(e);
                job.setStatus(JobStatus.FAILED);
                job.setCompletedAt(System.currentTimeMillis());

                notify(id, job);
            }
        });

        job.setFuture(future);

        return new ResponseEntity<JobHandle>()
                .setBody(new JobHandle(id, wsUrl))
                .accepted();
    }

    // =========================
    // GET JOB
    // =========================
    public ResponseEntity<JobResponse> getJob(UUID id) {

        JobHolder job = jobs.get(id);

        if (job == null) {
            return new ResponseEntity<JobResponse>()
                    .setBody(null)
                    .notFound();
        }

        JobResponse response = new JobResponse(job.getStatus(), job.getResult(), job.getError());

        if (job.getStatus() != JobStatus.PENDING && job.getStatus() != JobStatus.RUNNING)
            jobs.remove(id);

        return new ResponseEntity<JobResponse>()
                .setBody(response)
                .ok();
    }

    // =========================
    // CLEAR FINISHED JOBS
    // =========================
    public void clearJobs() {

        long now = System.currentTimeMillis();

        jobs.entrySet().removeIf(e -> {
            JobHolder job = e.getValue();

            boolean finished =
                    job.getStatus() == JobStatus.DONE ||
                            job.getStatus() == JobStatus.FAILED ||
                            job.getStatus() == JobStatus.CANCELLED;

            return finished && (now - job.getCompletedAt() > 40_000);
        });
    }

    // =========================
    // CLEAR ALL
    // =========================
    public void clearAllJobs() {

        for (JobHolder job : jobs.values()) {
            Future<?> f = job.getFuture();
            if (f != null) f.cancel(true);

            job.setStatus(JobStatus.CANCELLED);
        }

        jobs.clear();
    }

    // =========================
    // WS NOTIFY
    // =========================
    private void notify(UUID id, JobHolder job) {
        if (!useWebSocket) return;

        try {
            String json = mapper.writeValueAsString(
                    new JobResponse(job.getStatus(), job.getResult(), job.getError())
            );

            wsRegistry.send(id.toString(), json);

        } catch (Exception ignored) {}
    }

    public WebSocketRegistry getWsRegistry() {
        return wsRegistry;
    }
}
