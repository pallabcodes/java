package com.backend.core.language;

/**
 * Step 03: Pattern Matching — Deep Deconstruction (L7 Mastery)
 *
 * CONCEPT:
 * Pattern matching lets you test a value's shape AND extract its components
 * in a single expression. Java 21 supports instanceof patterns, record patterns,
 * nested patterns, and guarded patterns.
 *
 * FOR C/C++ ENGINEERS:
 * C has no pattern matching. C++ has std::visit for std::variant, but it's verbose.
 * Rust has `match` with destructuring — Java 21's pattern matching is the closest
 * Java gets to Rust's expressiveness.
 *
 * L7 AWARENESS:
 * 1. INSTANCEOF PATTERNS: Combine type check + cast + variable binding in one line.
 * 2. RECORD PATTERNS: Deconstruct record fields directly in the pattern.
 * 3. NESTED PATTERNS: Deconstruct deeply nested structures in one expression.
 * 4. GUARDS: Add boolean conditions to patterns using 'when'.
 */
public class Step03_PatternMatching {

    // ─── Domain types for demonstration ─────────────────────────────────────
    public sealed interface Expr permits Literal, Add, Mul, Neg {}
    public record Literal(double value) implements Expr {}
    public record Add(Expr left, Expr right) implements Expr {}
    public record Mul(Expr left, Expr right) implements Expr {}
    public record Neg(Expr operand) implements Expr {}

    // ─── 1. instanceof Pattern (Java 16+) ───────────────────────────────────
    // BEFORE: if (obj instanceof String) { String s = (String) obj; ... }
    // AFTER:  if (obj instanceof String s) { ... }
    public static String describeOld(Object obj) {
        // L7: The variable 's' is only in scope where the pattern matched
        if (obj instanceof String s && s.length() > 5) {
            return "Long string: " + s;
        } else if (obj instanceof Integer i && i > 0) {
            return "Positive int: " + i;
        } else if (obj instanceof double[] arr && arr.length > 0) {
            return "Double array of length " + arr.length;
        }
        return "Unknown: " + obj;
    }

    // ─── 2. Record Patterns (Java 21) — Deep Deconstruction ─────────────────
    // This is where Java catches up to Rust's match destructuring
    public static double evaluate(Expr expr) {
        return switch (expr) {
            case Literal(var v)          -> v;
            case Add(var l, var r)       -> evaluate(l) + evaluate(r);
            case Mul(var l, var r)       -> evaluate(l) * evaluate(r);
            case Neg(var operand)        -> -evaluate(operand);
        };
    }

    // ─── 3. Nested Patterns — Compile-time structural matching ──────────────
    // L7: The compiler flattens nested patterns into efficient bytecode.
    public static String simplify(Expr expr) {
        return switch (expr) {
            // Match: Neg(Neg(x)) → "double negation of x"
            case Neg(Neg(var inner))     -> "Double negation: simplifies to " + evaluate(inner);
            // Match: Add(Literal(0), x) → "identity add"
            case Add(Literal(var v), var r) when v == 0.0 -> "Identity: 0 + " + evaluate(r);
            // Match: Mul(Literal(1), x) → "identity mul"
            case Mul(Literal(var v), var r) when v == 1.0 -> "Identity: 1 * " + evaluate(r);
            // Match: Mul(Literal(0), _) → "zero product"
            case Mul(Literal(var v), var ignored) when v == 0.0 -> "Zero product: always 0";
            // Default evaluation
            default -> "Result: " + evaluate(expr);
        };
    }

    // ─── 4. Guarded Patterns (when clause) ──────────────────────────────────
    public sealed interface HttpResponse permits Ok, ClientError, ServerError {}
    public record Ok(String body) implements HttpResponse {}
    public record ClientError(int code, String msg) implements HttpResponse {}
    public record ServerError(int code, String msg) implements HttpResponse {}

    public static String handleResponse(HttpResponse resp) {
        return switch (resp) {
            case Ok(var body) when body.isEmpty() -> "Empty 200 response";
            case Ok(var body)                     -> "Success: " + body;
            case ClientError(var c, var m) when c == 404 -> "Not Found: " + m;
            case ClientError(var c, var m) when c == 401 -> "Auth Required: " + m;
            case ClientError(var c, var m)        -> "Client Error " + c + ": " + m;
            case ServerError(var c, var m)        -> "Server Error " + c + ": " + m;
        };
    }

    public static void main(String[] args) {
        System.out.println("=== Step 03: Pattern Matching — Deep Deconstruction ===");

        // 1. instanceof patterns
        System.out.println(describeOld("Hello, World!"));
        System.out.println(describeOld(42));
        System.out.println(describeOld(new double[]{1.0, 2.0}));

        // 2. Expression evaluation via record patterns
        // Expression: (3 + 4) * -(2)
        Expr expr = new Mul(new Add(new Literal(3), new Literal(4)), new Neg(new Literal(2)));
        System.out.println("(3 + 4) * -(2) = " + evaluate(expr));

        // 3. Nested pattern simplification
        System.out.println(simplify(new Neg(new Neg(new Literal(5)))));
        System.out.println(simplify(new Add(new Literal(0), new Literal(7))));
        System.out.println(simplify(new Mul(new Literal(0), new Literal(99))));

        // 4. Guarded patterns
        System.out.println(handleResponse(new Ok("user_data")));
        System.out.println(handleResponse(new Ok("")));
        System.out.println(handleResponse(new ClientError(404, "/api/users/999")));
        System.out.println(handleResponse(new ServerError(503, "Service Unavailable")));
    }
}
