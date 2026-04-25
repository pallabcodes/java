package com.backend.core.language;

/**
 * Step 02: Sealed Classes — Exhaustive Type Hierarchies (L7 Mastery)
 *
 * CONCEPT:
 * Sealed classes restrict which classes can extend them. The compiler knows
 * ALL possible subtypes at compile time, enabling exhaustive pattern matching.
 *
 * FOR C/C++ ENGINEERS:
 * This is Java's equivalent of a tagged union / discriminated union / std::variant.
 * In C: you'd use an enum tag + union. In Rust: enum with data. In Java 21: sealed + records.
 *
 * L7 AWARENESS:
 * 1. PERMITS: Explicitly lists allowed subtypes — compiler enforces completeness.
 * 2. EXHAUSTIVENESS: Switch over sealed types is checked at compile time.
 * 3. COMPOSITION: Sealed + Records = Algebraic Data Types (Sum Types).
 * 4. MIGRATION PATH: Replaces the Visitor pattern in most cases.
 */
public class Step02_SealedClasses {

    // ─── Tagged Union: The C way (what we're replacing) ─────────────────────
    // In C:   enum JsonTag { STRING, NUMBER, BOOL, NULL_VAL, ARRAY, OBJECT };
    //         struct JsonValue { enum JsonTag tag; union { ... } data; };
    //
    // In Java 21: sealed interface + record subtypes

    public sealed interface JsonValue permits
            JsonString, JsonNumber, JsonBool, JsonNull, JsonArray {
        // No methods required — the hierarchy IS the contract
    }

    public record JsonString(String value)       implements JsonValue {}
    public record JsonNumber(double value)       implements JsonValue {}
    public record JsonBool(boolean value)        implements JsonValue {}
    public record JsonNull()                     implements JsonValue {}
    public record JsonArray(java.util.List<JsonValue> elements) implements JsonValue {}

    // ─── Exhaustive processing (replaces Visitor pattern) ───────────────────
    public static String stringify(JsonValue value) {
        // L7: The compiler verifies ALL cases are handled.
        // If you add a new permitted type and forget to handle it here,
        // you get a COMPILE ERROR — not a runtime NPE.
        return switch (value) {
            case JsonString s  -> "\"" + s.value() + "\"";
            case JsonNumber n  -> String.valueOf(n.value());
            case JsonBool b    -> String.valueOf(b.value());
            case JsonNull ignored -> "null";
            case JsonArray a   -> "[" + a.elements().stream()
                    .map(Step02_SealedClasses::stringify)
                    .reduce((x, y) -> x + ", " + y)
                    .orElse("") + "]";
        };
    }

    // ─── Real-world example: API Response handling ──────────────────────────
    public sealed interface ApiResult<T> permits ApiResult.Success, ApiResult.Failure {
        record Success<T>(T data, int statusCode) implements ApiResult<T> {}
        record Failure<T>(String error, int statusCode) implements ApiResult<T> {}
    }

    public static <T> String handleResult(ApiResult<T> result) {
        return switch (result) {
            case ApiResult.Success<T> s -> "OK(" + s.statusCode() + "): " + s.data();
            case ApiResult.Failure<T> f -> "ERR(" + f.statusCode() + "): " + f.error();
        };
    }

    public static void main(String[] args) {
        System.out.println("=== Step 02: Sealed Classes — Exhaustive Type Hierarchies ===");

        // 1. Build a JSON structure using our sealed hierarchy
        var json = new JsonArray(java.util.List.of(
                new JsonString("hello"),
                new JsonNumber(42),
                new JsonBool(true),
                new JsonNull()
        ));
        System.out.println("JSON: " + stringify(json));

        // 2. API Result pattern
        ApiResult<String> success = new ApiResult.Success<>("user_data", 200);
        ApiResult<String> failure = new ApiResult.Failure<>("not found", 404);
        System.out.println(handleResult(success));
        System.out.println(handleResult(failure));

        // L7 KEY INSIGHT:
        System.out.println("\n[L7] Sealed classes eliminate the 'default' anti-pattern.");
        System.out.println("[L7] Adding a new variant forces handling everywhere — zero silent bugs.");
        System.out.println("[L7] This is why Rust enums and C++ std::variant are popular — Java now has parity.");
    }
}
