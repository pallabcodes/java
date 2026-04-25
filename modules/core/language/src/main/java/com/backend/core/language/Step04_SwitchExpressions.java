package com.backend.core.language;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Step 04: Switch Expressions — Exhaustive Control Flow (L7 Mastery)
 *
 * CONCEPT:
 * Java 21 switch is an EXPRESSION (returns a value), supports pattern matching,
 * and enforces exhaustiveness for sealed types and enums.
 *
 * FOR C/C++ ENGINEERS:
 * C switch has fall-through bugs, no pattern matching, and no exhaustiveness.
 * Java 21 switch fixes ALL of these. Arrow syntax (->) has no fall-through.
 * Expression form requires a value for every branch — the compiler enforces this.
 *
 * L7 AWARENESS:
 * 1. ARROW SYNTAX: No fall-through. No break needed. Each case is an expression.
 * 2. EXHAUSTIVENESS: Compiler error if you miss a case (for enums and sealed types).
 * 3. YIELD: For multi-line branches that need to return a value.
 * 4. NULL HANDLING: switch can now handle null cases explicitly (Java 21).
 * 5. PATTERN GUARDS: 'when' clause for conditional matching within a case.
 */
public class Step04_SwitchExpressions {

    // ─── Enum for demonstration ─────────────────────────────────────────────
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    // ─── 1. Switch as an Expression (replaces if-else chains) ───────────────
    public static int priorityToSla(Priority p) {
        // L7: This is an EXPRESSION — it returns a value.
        // The compiler enforces that ALL enum values are handled.
        return switch (p) {
            case LOW      -> 72;
            case MEDIUM   -> 24;
            case HIGH     -> 4;
            case CRITICAL -> 1;
            // No 'default' needed — compiler knows enum is exhaustive
        };
    }

    // ─── 2. yield for multi-line branches ───────────────────────────────────
    public static String classifyDay(DayOfWeek day) {
        return switch (day) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> {
                // Multi-line logic — use 'yield' to return the value
                var label = day.name().substring(0, 3);
                yield "Weekday (" + label + ")";
            }
            case SATURDAY, SUNDAY -> "Weekend";
        };
    }

    // ─── 3. Pattern Matching in Switch (the big Java 21 feature) ────────────
    // C++ equivalent: std::visit on std::variant — but far more readable
    public static String formatValue(Object obj) {
        return switch (obj) {
            case null           -> "null";
            case Integer i      -> "int: " + i;
            case Long l         -> "long: " + l;
            case Double d       -> "double: " + String.format("%.2f", d);
            case String s when s.isEmpty() -> "empty string";
            case String s       -> "string: \"" + s + "\" (len=" + s.length() + ")";
            case int[] arr      -> "int[] of length " + arr.length;
            default             -> obj.getClass().getSimpleName() + ": " + obj;
        };
    }

    // ─── 4. Sealed type + switch = compile-time safety ──────────────────────
    public sealed interface Command {
        record Start(String serviceName) implements Command {}
        record Stop(String serviceName, boolean graceful) implements Command {}
        record Scale(String serviceName, int replicas) implements Command {}
    }

    public static String executeCommand(Command cmd) {
        // L7: If you add a new Command variant, this switch FAILS TO COMPILE
        // until you handle the new case. Zero runtime surprises.
        return switch (cmd) {
            case Command.Start(var name)              -> "Starting " + name;
            case Command.Stop(var name, var graceful)  -> (graceful ? "Gracefully stopping " : "Force stopping ") + name;
            case Command.Scale(var name, var n) when n > 10 -> "Warning: Scaling " + name + " to " + n + " (high!)";
            case Command.Scale(var name, var n)        -> "Scaling " + name + " to " + n + " replicas";
        };
    }

    public static void main(String[] args) {
        System.out.println("=== Step 04: Switch Expressions — Exhaustive Control Flow ===");

        // 1. Expression switch
        System.out.println("CRITICAL SLA: " + priorityToSla(Priority.CRITICAL) + "h");
        System.out.println("LOW SLA: " + priorityToSla(Priority.LOW) + "h");

        // 2. Multi-line with yield
        System.out.println(classifyDay(DayOfWeek.WEDNESDAY));
        System.out.println(classifyDay(DayOfWeek.SATURDAY));

        // 3. Pattern matching switch (handles Object, null, guards)
        System.out.println(formatValue(42));
        System.out.println(formatValue("Hello"));
        System.out.println(formatValue(""));
        System.out.println(formatValue(null));
        System.out.println(formatValue(3.14159));

        // 4. Sealed types in switch
        System.out.println(executeCommand(new Command.Start("api-gateway")));
        System.out.println(executeCommand(new Command.Stop("legacy-service", true)));
        System.out.println(executeCommand(new Command.Scale("worker-pool", 50)));
        System.out.println(executeCommand(new Command.Scale("cache", 3)));
    }
}
