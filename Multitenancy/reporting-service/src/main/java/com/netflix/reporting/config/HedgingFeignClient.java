package com.netflix.reporting.config;

import feign.Client;
import feign.Request;
import feign.Response;

import java.io.IOException;
import java.util.concurrent.*;

public class HedgingFeignClient implements Client {

    private final Client delegate;
    private final ScheduledExecutorService scheduler;
    private final long hedgeDelayMs;

    public HedgingFeignClient(Client delegate, ScheduledExecutorService scheduler, long hedgeDelayMs) {
        this.delegate = delegate;
        this.scheduler = scheduler;
        this.hedgeDelayMs = hedgeDelayMs;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        CompletableFuture<Response> primary = CompletableFuture.supplyAsync(() -> uncheckedExecute(request, options));
        CompletableFuture<Response> hedge = new CompletableFuture<>();
        ScheduledFuture<?> task = scheduler.schedule(() -> {
            if (!primary.isDone()) {
                hedge.completeAsync(() -> uncheckedExecute(request, options));
            }
        }, hedgeDelayMs, TimeUnit.MILLISECONDS);

        try {
            return CompletableFuture.anyOf(primary, hedge)
                    .thenApply(o -> (Response) o)
                    .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        } catch (ExecutionException e) {
            throw unwrap(e);
        } finally {
            task.cancel(true);
        }
    }

    private Response uncheckedExecute(Request request, Request.Options options) {
        try {
            return delegate.execute(request, options);
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }

    private IOException unwrap(ExecutionException e) {
        Throwable c = e.getCause();
        if (c instanceof CompletionException && c.getCause() instanceof IOException) {
            return (IOException) c.getCause();
        }
        if (c instanceof IOException) return (IOException) c;
        return new IOException(c);
    }
}


