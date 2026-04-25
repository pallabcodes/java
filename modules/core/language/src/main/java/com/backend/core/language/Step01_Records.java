package com.backend.core.language;

/**
 * Step 01: Records — Algebraic Data Types for Java (L7 Mastery)
 *
 * CONCEPT:
 * Records are Java 21's answer to C/C++ structs. They are immutable, transparent
 * data carriers with auto-generated equals(), hashCode(), toString(), and accessors.
 *
 * FOR C/C++ ENGINEERS:
 * Think of Records as a "const struct" with automatic value-semantics comparison.
 * Unlike C structs, Records are always heap-allocated (Java has no stack-allocated
 * value types yet — Project Valhalla aims to fix this).
 *
 * L7 AWARENESS:
 * 1. COMPACT CONSTRUCTORS: Validation logic without repeating parameter assignments.
 * 2. IMMUTABILITY: All fields are final. No setters. Thread-safe by construction.
 * 3. SERIALIZATION: Records work seamlessly with Jackson/JSON — no annotations needed.
 * 4. DECONSTRUCTION: Records enable pattern matching (see Step03).
 */
public class Step01_Records {

    // ─── Basic Record: replaces 50+ lines of boilerplate POJO ───────────────
    // Equivalent to: class with private final fields, constructor, getters,
    // equals(), hashCode(), toString()
    public record Point(double x, double y) {}

    // ─── Compact Constructor: Validation without assignment boilerplate ──────
    // C analogy: Like a constructor that validates but the compiler auto-assigns.
    public record HttpStatus(int code, String reason) {
        // Compact constructor — no parameter list, assignment is implicit
        public HttpStatus {
            if (code < 100 || code > 599) {
                throw new IllegalArgumentException("HTTP status code must be 100-599, got: " + code);
            }
            // 'this.code = code' and 'this.reason = reason' are auto-generated.
            // You CANNOT reassign 'code' or 'reason' here — they are effectively final.
        }
    }

    // ─── Records with Behavior: Not just data bags ──────────────────────────
    public record Vector3D(double x, double y, double z) {
        public double magnitude() {
            return Math.sqrt(x * x + y * y + z * z);
        }

        public Vector3D normalize() {
            double mag = magnitude();
            return new Vector3D(x / mag, y / mag, z / mag);
        }

        // Static factory method — preferred over overloaded constructors
        public static Vector3D zero() {
            return new Vector3D(0, 0, 0);
        }
    }

    // ─── Generic Records: Type-safe wrappers ────────────────────────────────
    // C++ analogy: std::pair<T, U> but with named semantics
    public record Pair<A, B>(A first, B second) {}

    // ─── Record implementing an interface ───────────────────────────────────
    public sealed interface Shape permits Circle, Rectangle {}

    public record Circle(double radius) implements Shape {
        public double area() { return Math.PI * radius * radius; }
    }

    public record Rectangle(double width, double height) implements Shape {
        public double area() { return width * height; }
    }

    public static void main(String[] args) {
        System.out.println("=== Step 01: Records — Algebraic Data Types ===");

        // 1. Basic usage
        var p = new Point(3.0, 4.0);
        System.out.println("Point: " + p);                    // Auto toString()
        System.out.println("x=" + p.x() + ", y=" + p.y());    // Accessors (no 'get' prefix)

        // 2. Value equality (not reference equality)
        var p2 = new Point(3.0, 4.0);
        System.out.println("Value equality: " + p.equals(p2)); // true — like C++ operator==

        // 3. Compact constructor validation
        var ok = new HttpStatus(200, "OK");
        System.out.println("HTTP: " + ok);
        try {
            new HttpStatus(999, "Invalid");
        } catch (IllegalArgumentException e) {
            System.out.println("[VALIDATED] " + e.getMessage());
        }

        // 4. Records with behavior
        var v = new Vector3D(1, 2, 3);
        System.out.println("Magnitude: " + v.magnitude());
        System.out.println("Normalized: " + v.normalize());

        // 5. Generic records
        var pair = new Pair<>("RequestId", 42);
        System.out.println("Pair: " + pair);

        // 6. Records + Sealed interfaces (see Step02 for deep dive)
        Shape shape = new Circle(5.0);
        System.out.println("Circle area: " + ((Circle) shape).area());
    }
}
