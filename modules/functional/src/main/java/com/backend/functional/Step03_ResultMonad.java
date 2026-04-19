package com.backend.functional;

import java.util.function.Function;

/**
 * Step 03: The Result Monad (Railway Oriented Programming)
 * 
 * L5 Principles:
 * 1. Monad: A container for a value that handles its own context (success/failure).
 * 2. Short-circuiting: If an operation fails, subsequent steps are skipped.
 * 3. Functional Error Handling: Avoids expensive and messy try-catch blocks.
 */
public class Step03_ResultMonad {

    // Simple sealed interface for Result (L5 approach)
    public interface Result<T, E> {
        boolean isSuccess();
        T getSuccess();
        E getFailure();

        static <T, E> Result<T, E> success(T value) { return new Success<>(value); }
        static <T, E> Result<T, E> failure(E error) { return new Failure<>(error); }

        default <R> Result<R, E> map(Function<T, R> mapper) {
            return isSuccess() ? success(mapper.apply(getSuccess())) : failure(getFailure());
        }

        default <R> Result<R, E> flatMap(Function<T, Result<R, E>> mapper) {
            return isSuccess() ? mapper.apply(getSuccess()) : failure(getFailure());
        }
    }

    record Success<T, E>(T value) implements Result<T, E> {
        public boolean isSuccess() { return true; }
        public T getSuccess() { return value; }
        public E getFailure() { throw new RuntimeException("No failure in Success"); }
    }

    record Failure<T, E>(E error) implements Result<T, E> {
        public boolean isSuccess() { return false; }
        public T getSuccess() { throw new RuntimeException("No success in Failure"); }
        public E getFailure() { return error; }
    }

    // GMeet Scenario
    public record Participant(String id, boolean hasCameraOn) {}

    public static void main(String[] args) {
        System.out.println("=== Step 03: The Result Monad ===");

        Result<String, String> joinAttempt = joinMeeting("MTG-123", "USER-99");

        // Chain operations: If join fails, 'record' and 'notify' are skipped.
        Result<String, String> finalPipelineResult = joinAttempt
                .map(id -> "Logged join for: " + id)
                .flatMap(log -> logToCloud(log));

        if (finalPipelineResult.isSuccess()) {
            System.out.println("Success: " + finalPipelineResult.getSuccess());
        } else {
            System.err.println("Failure Pipeline Stopped At: " + finalPipelineResult.getFailure());
        }
    }

    private static Result<String, String> joinMeeting(String mtgId, String userId) {
        // Logic: Meeting MTG-123 is full
        if (mtgId.equals("MTG-123")) {
            return Result.failure("MEETING_FULL");
        }
        return Result.success(userId);
    }

    private static Result<String, String> logToCloud(String message) {
        System.out.println("Processing log: " + message);
        return Result.success("CLOUD_SYNC_OK");
    }
}
